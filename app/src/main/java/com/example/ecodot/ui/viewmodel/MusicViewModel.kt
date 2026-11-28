package com.example.ecodot.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionToken
import android.content.ComponentName
import com.example.ecodot.data.local.database.EcoDotDatabase
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.data.local.entities.UserProfile
import com.example.ecodot.data.local.entities.PlaybackHistoryWithTrack
import com.example.ecodot.data.local.entities.RecentSearchItem
import com.example.ecodot.data.local.entities.FollowedArtist
import com.example.ecodot.data.repository.MusicRepository
import com.example.ecodot.playback.EcoDotSessionService
import com.example.ecodot.data.local.prefs.VideoQuality
import android.net.Uri
import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import android.os.Build
import android.provider.MediaStore
import android.content.ContentValues
import java.io.File
import androidx.core.net.toUri
import androidx.media3.common.C
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import androidx.room.Room
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import android.widget.Toast
import android.util.Log
import androidx.media3.common.MediaMetadata
import com.example.ecodot.data.remote.LrcLibResponse
import com.example.ecodot.data.remote.VideoInfo
import androidx.compose.ui.graphics.Color
import android.os.Bundle
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.palette.graphics.Palette
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C as MediaC
import com.example.ecodot.util.NetworkMonitor
import com.example.ecodot.util.NetworkState

data class LyricLine(val timeMs: Long, val text: String)

typealias Artist = com.example.ecodot.domain.model.Artist
typealias Album = com.example.ecodot.domain.model.Album
typealias Genre = com.example.ecodot.domain.model.Genre

enum class HomeTab {
    Home, Library, Search
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class MusicViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "MusicViewModel"

    private val database = EcoDotDatabase.getInstance(application)

    private val repository = MusicRepository(
        application, 
        database.trackDao(), 
        database.historyDao(),
        database.profileDao(),
        database.playlistDao(),
        database.recentSearchDao(),
        database.followedArtistDao()
    )
    
    private val lyricsRepository = com.example.ecodot.data.remote.LyricsRepository()

    // ── App In-App Updater ──
    val appUpdateManager = com.example.ecodot.util.AppUpdateManager(application)
    val updateStatus: StateFlow<com.example.ecodot.util.UpdateStatus> = appUpdateManager.status

    fun checkForUpdates(force: Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) {
            appUpdateManager.checkForUpdates(force = force)
            _lastUpdateCheckTime.value = prefsManager.lastUpdateCheckTime
        }
    }

    fun downloadUpdate(info: com.example.ecodot.util.AppUpdateInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            appUpdateManager.downloadUpdate(info)
        }
    }

    fun installUpdate(apkFile: java.io.File): Boolean {
        return appUpdateManager.installApk(apkFile)
    }

    fun dismissUpdate() {
        appUpdateManager.dismissUpdate()
    }

    // ── Sleep Timer State ──
    private val _sleepTimerRemaining = MutableStateFlow<Long?>(null) // in seconds, or 0 for End of Track, or null for none
    val sleepTimerRemaining: StateFlow<Long?> = _sleepTimerRemaining.asStateFlow()
    private var sleepTimerSyncJob: Job? = null

    init {
        startSleepTimerSync()
    }

    private fun startSleepTimerSync() {
        sleepTimerSyncJob?.cancel()
        sleepTimerSyncJob = viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val prefs = context.getSharedPreferences("sleep_timer_prefs", Context.MODE_PRIVATE)
            while (isActive) {
                val isEndOfTrack = prefs.getBoolean("sleep_timer_end_of_track", false)
                if (isEndOfTrack) {
                    _sleepTimerRemaining.value = 0L // 0 indicates End of Track
                } else {
                    val endTimestamp = prefs.getLong("sleep_timer_end_timestamp", 0L)
                    val now = System.currentTimeMillis()
                    if (endTimestamp > now) {
                        _sleepTimerRemaining.value = (endTimestamp - now) / 1000
                    } else {
                        _sleepTimerRemaining.value = null
                    }
                }
                delay(1000)
            }
        }
    }

    fun setSleepTimer(minutes: Int) {
        val controller = _controller.value ?: return
        val args = android.os.Bundle().apply {
            putInt("minutes", minutes)
            putBoolean("endOfTrack", false)
        }
        controller.sendCustomCommand(
            androidx.media3.session.SessionCommand("SET_SLEEP_TIMER", android.os.Bundle.EMPTY),
            args
        )
    }

    fun setSleepTimerAtEndOfTrack() {
        val controller = _controller.value ?: return
        val args = android.os.Bundle().apply {
            putBoolean("endOfTrack", true)
        }
        controller.sendCustomCommand(
            androidx.media3.session.SessionCommand("SET_SLEEP_TIMER", android.os.Bundle.EMPTY),
            args
        )
    }

    fun cancelSleepTimer() {
        val controller = _controller.value ?: return
        controller.sendCustomCommand(
            androidx.media3.session.SessionCommand("CANCEL_SLEEP_TIMER", android.os.Bundle.EMPTY),
            android.os.Bundle.EMPTY
        )
    }
    
    private val prefsManager = com.example.ecodot.data.local.prefs.UserPreferencesManager(application)
    
    private val _audioQuality = MutableStateFlow(prefsManager.audioQuality)
    val audioQuality: StateFlow<com.example.ecodot.data.local.prefs.AudioQuality> = _audioQuality.asStateFlow()

    fun setAudioQuality(quality: com.example.ecodot.data.local.prefs.AudioQuality) {
        prefsManager.audioQuality = quality
        _audioQuality.value = quality
        sendCustomCommand(
            EcoDotSessionService.ACTION_CLEAR_URL_CACHE,
            Bundle.EMPTY
        )
    }

    private val _videoQuality = MutableStateFlow(prefsManager.videoQuality)
    val videoQuality: StateFlow<com.example.ecodot.data.local.prefs.VideoQuality> = _videoQuality.asStateFlow()

    fun setVideoQuality(quality: com.example.ecodot.data.local.prefs.VideoQuality) {
        prefsManager.videoQuality = quality
        _videoQuality.value = quality
    }

    fun reloadCurrentTrack() {
        val track = _currentTrack.value ?: return
        val controller = _controller.value ?: return
        val currentPosition = controller.currentPosition
        val currentPlayWhenReady = controller.playWhenReady
        
        if (track.isYouTube && !isDownloaded(track.id)) {
            val mediaItem = buildMediaItem(track)
            val currentIndex = controller.currentMediaItemIndex
            if (currentIndex != androidx.media3.common.C.INDEX_UNSET) {
                controller.replaceMediaItem(currentIndex, mediaItem)
                controller.seekTo(currentIndex, currentPosition)
                controller.playWhenReady = currentPlayWhenReady
            }
        }
    }

    fun downloadTrack(track: Track) {
        viewModelScope.launch(Dispatchers.IO) {
            val url = repository.getYouTubeStreamUrl(track.id, prefsManager.audioQuality)
            if (url != null) {
                val context = getApplication<Application>().applicationContext
                val request = DownloadManager.Request(Uri.parse(url))
                    .setTitle(track.title)
                    .setDescription("Downloading ${track.title} by ${track.artist}")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_MUSIC, "${track.id}.m4a")
                
                val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val downloadId = downloadManager.enqueue(request)
                
                // Track progress
                viewModelScope.launch(Dispatchers.IO) {
                    var downloading = true
                    while (downloading && isActive) {
                        val query = DownloadManager.Query().setFilterById(downloadId)
                        val cursor = downloadManager.query(query)
                        if (cursor != null && cursor.moveToFirst()) {
                            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                            if (statusIndex != -1) {
                                val status = cursor.getInt(statusIndex)
                                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                    downloading = false
                                    _downloadProgress.value = _downloadProgress.value.toMutableMap().apply { remove(track.id) }
                                    // Update track path in the database to play locally
                                    val localFile = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "${track.id}.m4a")
                                    viewModelScope.launch(Dispatchers.IO) {
                                        val dbTrack = database.trackDao().getTrackById(track.id)
                                        if (dbTrack != null) {
                                            val updatedTrack = dbTrack.copy(
                                                path = localFile.absolutePath,
                                                isYouTube = false
                                            )
                                            repository.saveTrack(updatedTrack)
                                        }
                                    }
                                } else if (status == DownloadManager.STATUS_FAILED) {
                                    downloading = false
                                    _downloadProgress.value = _downloadProgress.value.toMutableMap().apply { remove(track.id) }
                                } else {
                                    val bytesDownloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                                    val bytesTotalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                                    if (bytesDownloadedIndex != -1 && bytesTotalIndex != -1) {
                                        val bytesDownloaded = cursor.getInt(bytesDownloadedIndex)
                                        val bytesTotal = cursor.getInt(bytesTotalIndex)
                                        if (bytesTotal > 0) {
                                            val progress = (bytesDownloaded.toFloat() / bytesTotal.toFloat())
                                            _downloadProgress.value = _downloadProgress.value.toMutableMap().apply { put(track.id, progress) }
                                        }
                                    }
                                }
                            }
                            cursor.close()
                        } else {
                            downloading = false
                            _downloadProgress.value = _downloadProgress.value.toMutableMap().apply { remove(track.id) }
                        }
                        delay(500) // Poll every 500ms
                    }
                }
                
                // Add to a "Downloads" playlist so we have metadata in the Library
                var downloadsPlaylist = repository.allPlaylists.firstOrNull()?.find { it.name == "Downloads" }
                if (downloadsPlaylist == null) {
                    val id = repository.createPlaylist("Downloads", "Offline tracks")
                    downloadsPlaylist = repository.allPlaylists.firstOrNull()?.find { it.id == id }
                }
                downloadsPlaylist?.let { playlist ->
                    repository.addTrackToPlaylist(playlist.id, track)
                }
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Downloading ${track.title}...", Toast.LENGTH_SHORT).show()
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(getApplication(), "Failed to get download link", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private val _offlineMode = MutableStateFlow(prefsManager.offlineMode)
    val offlineMode: StateFlow<Boolean> = _offlineMode.asStateFlow()

    fun setOfflineMode(enabled: Boolean) {
        prefsManager.offlineMode = enabled
        _offlineMode.value = enabled
    }
    
    private val _killServiceOnExit = MutableStateFlow(prefsManager.killServiceOnExit)
    val killServiceOnExit: StateFlow<Boolean> = _killServiceOnExit.asStateFlow()
    
    fun setKillServiceOnExit(enabled: Boolean) {
        prefsManager.killServiceOnExit = enabled
        _killServiceOnExit.value = enabled
    }
    
    private val _crossfadeEnabled = MutableStateFlow(prefsManager.crossfadeEnabled)
    val crossfadeEnabled: StateFlow<Boolean> = _crossfadeEnabled.asStateFlow()
    
    fun setCrossfadeEnabled(enabled: Boolean) {
        prefsManager.crossfadeEnabled = enabled
        _crossfadeEnabled.value = enabled
    }
    
    private val _crossfadeDuration = MutableStateFlow(prefsManager.crossfadeDuration)
    val crossfadeDuration: StateFlow<Int> = _crossfadeDuration.asStateFlow()

    fun setCrossfadeDuration(duration: Int) {
        prefsManager.crossfadeDuration = duration
        _crossfadeDuration.value = duration
    }

    private val _crossfadeSkipAlbum = MutableStateFlow(prefsManager.crossfadeSkipAlbum)
    val crossfadeSkipAlbum: StateFlow<Boolean> = _crossfadeSkipAlbum.asStateFlow()

    fun setCrossfadeSkipAlbum(enabled: Boolean) {
        prefsManager.crossfadeSkipAlbum = enabled
        _crossfadeSkipAlbum.value = enabled
    }

    private val _crossfadeDjMode = MutableStateFlow(prefsManager.crossfadeDjMode)
    val crossfadeDjMode: StateFlow<Boolean> = _crossfadeDjMode.asStateFlow()

    fun setCrossfadeDjMode(enabled: Boolean) {
        prefsManager.crossfadeDjMode = enabled
        _crossfadeDjMode.value = enabled
    }

    private val _isCrossfading = MutableStateFlow(false)
    val isCrossfading: StateFlow<Boolean> = _isCrossfading.asStateFlow()

    private val _normalizeVolume = MutableStateFlow(prefsManager.normalizeVolume)
    val normalizeVolume: StateFlow<Boolean> = _normalizeVolume.asStateFlow()

    fun setNormalizeVolume(enabled: Boolean) {
        prefsManager.normalizeVolume = enabled
        _normalizeVolume.value = enabled
        sendCustomCommand(
            EcoDotSessionService.ACTION_SET_NORMALIZE_VOLUME,
            Bundle().apply { putBoolean("enabled", enabled) }
        )
    }

    private val _skipSilent = MutableStateFlow(prefsManager.skipSilent)
    val skipSilent: StateFlow<Boolean> = _skipSilent.asStateFlow()

    fun setSkipSilent(enabled: Boolean) {
        prefsManager.skipSilent = enabled
        _skipSilent.value = enabled
        sendCustomCommand(
            EcoDotSessionService.ACTION_SET_SKIP_SILENCE,
            Bundle().apply { putBoolean("enabled", enabled) }
        )
    }

    private val _sponsorBlockEnabled = MutableStateFlow(prefsManager.sponsorBlockEnabled)
    val sponsorBlockEnabled: StateFlow<Boolean> = _sponsorBlockEnabled.asStateFlow()

    fun setSponsorBlockEnabled(enabled: Boolean) {
        prefsManager.sponsorBlockEnabled = enabled
        _sponsorBlockEnabled.value = enabled
        sendCustomCommand(EcoDotSessionService.ACTION_REFRESH_SPONSOR_SEGMENTS, Bundle.EMPTY)
    }

    private val _sponsorBlockCategories = MutableStateFlow(prefsManager.sponsorBlockCategories)
    val sponsorBlockCategories: StateFlow<Set<String>> = _sponsorBlockCategories.asStateFlow()

    fun setSponsorBlockCategories(categories: Set<String>) {
        prefsManager.sponsorBlockCategories = categories
        _sponsorBlockCategories.value = categories
        sendCustomCommand(EcoDotSessionService.ACTION_REFRESH_SPONSOR_SEGMENTS, Bundle.EMPTY)
    }

    private val _themeMode = MutableStateFlow(prefsManager.themeMode)
    val themeMode: StateFlow<com.example.ecodot.data.local.prefs.ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: com.example.ecodot.data.local.prefs.ThemeMode) {
        prefsManager.themeMode = mode
        _themeMode.value = mode
    }

    private val _themeColorSource = MutableStateFlow(prefsManager.themeColorSource)
    val themeColorSource: StateFlow<com.example.ecodot.data.local.prefs.ThemeColorSource> = _themeColorSource.asStateFlow()

    fun setThemeColorSource(source: com.example.ecodot.data.local.prefs.ThemeColorSource) {
        prefsManager.themeColorSource = source
        _themeColorSource.value = source
    }

    private val _customThemeColorHex = MutableStateFlow(prefsManager.customThemeColorHex)
    val customThemeColorHex: StateFlow<String> = _customThemeColorHex.asStateFlow()

    fun setCustomThemeColorHex(hex: String) {
        val cleaned = hex.removePrefix("#").uppercase()
        val argb = if (cleaned.length == 6) "FF$cleaned" else cleaned.padStart(8, 'F').take(8)
        prefsManager.customThemeColorHex = argb
        _customThemeColorHex.value = argb
    }

    private val _liquidGlassEnabled = MutableStateFlow(prefsManager.liquidGlassEnabled)
    val liquidGlassEnabled: StateFlow<Boolean> = _liquidGlassEnabled.asStateFlow()

    fun setLiquidGlassEnabled(enabled: Boolean) {
        prefsManager.liquidGlassEnabled = enabled
        _liquidGlassEnabled.value = enabled
    }

    private val _autoCheckUpdates = MutableStateFlow(prefsManager.autoCheckUpdates)
    val autoCheckUpdates: StateFlow<Boolean> = _autoCheckUpdates.asStateFlow()

    fun setAutoCheckUpdates(enabled: Boolean) {
        prefsManager.autoCheckUpdates = enabled
        _autoCheckUpdates.value = enabled
    }

    private val _lastUpdateCheckTime = MutableStateFlow(prefsManager.lastUpdateCheckTime)
    val lastUpdateCheckTime: StateFlow<Long> = _lastUpdateCheckTime.asStateFlow()

    private val _cacheLimitMb = MutableStateFlow(prefsManager.cacheLimitMb)
    val cacheLimitMb: StateFlow<Long> = _cacheLimitMb.asStateFlow()

    fun setCacheLimitMb(limitMb: Long) {
        prefsManager.cacheLimitMb = limitMb
        _cacheLimitMb.value = limitMb
    }

    private val _storageStats = MutableStateFlow(com.example.ecodot.playback.StorageStats())
    val storageStats: StateFlow<com.example.ecodot.playback.StorageStats> = _storageStats.asStateFlow()

    private val _autoCanvasEnabled = MutableStateFlow(prefsManager.autoCanvasEnabled)
    val autoCanvasEnabled: StateFlow<Boolean> = _autoCanvasEnabled.asStateFlow()

    fun setAutoCanvasEnabled(enabled: Boolean) {
        prefsManager.autoCanvasEnabled = enabled
        _autoCanvasEnabled.value = enabled
    }

    private val _lockscreenLyricsEnabled = MutableStateFlow(prefsManager.lockscreenLyricsEnabled)
    val lockscreenLyricsEnabled: StateFlow<Boolean> = _lockscreenLyricsEnabled.asStateFlow()

    fun setLockscreenLyricsEnabled(enabled: Boolean) {
        prefsManager.lockscreenLyricsEnabled = enabled
        _lockscreenLyricsEnabled.value = enabled
    }

    private val _dataSaver = MutableStateFlow(prefsManager.dataSaver)
    val dataSaver: StateFlow<Boolean> = _dataSaver.asStateFlow()

    fun setDataSaver(enabled: Boolean) {
        prefsManager.dataSaver = enabled
        _dataSaver.value = enabled
    }

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks.asStateFlow()

    private val _cacheSize = MutableStateFlow(0L)
    val cacheSize: StateFlow<Long> = _cacheSize.asStateFlow()

    val cachedTrackIds: StateFlow<Set<String>> = prefsManager.cachedTrackIdsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, prefsManager.cachedTrackIds)

    val cachedTracks: StateFlow<List<Track>> = combine(repository.allTracks, cachedTrackIds) { all, ids ->
        val trackMap = all.associateBy { it.id }
        ids.map { id ->
            trackMap[id] 
                ?: _currentTrack.value?.takeIf { it.id == id } 
                ?: _playbackQueue.value.find { it.id == id }
                ?: _suggestedTracks.value.find { it.id == id }
                ?: Track(
                    id = id,
                    title = "Cached Track",
                    artist = "YouTube Music",
                    album = "Cache",
                    duration = 0L,
                    path = "yt://$id",
                    isYouTube = true
                )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateCacheSize() {
        viewModelScope.launch(Dispatchers.IO) {
            val stats = com.example.ecodot.playback.CacheManager.getStorageStats(getApplication())
            _storageStats.value = stats
            _cacheSize.value = stats.playerCacheBytes
        }
    }

    fun markTrackAsCached(trackId: String) {
        prefsManager.addCachedTrackId(trackId)
        updateCacheSize()
    }

    fun removeTrackFromCache(trackId: String) {
        com.example.ecodot.playback.CacheManager.removeCachedSong(getApplication(), trackId)
        prefsManager.removeCachedTrackId(trackId)
        updateCacheSize()
    }

    fun clearCache() {
        viewModelScope.launch(Dispatchers.IO) {
            com.example.ecodot.playback.CacheManager.clearCache(getApplication())
            prefsManager.clearCachedTrackIds()
            updateCacheSize()
        }
    }

    fun clearThumbnailCache() {
        viewModelScope.launch(Dispatchers.IO) {
            com.example.ecodot.playback.CacheManager.clearThumbnailCache(getApplication())
            updateCacheSize()
        }
    }

    fun clearDownloadsCache() {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            downloadsDir?.listFiles()?.forEach { file ->
                if (file.isFile) file.delete()
            }
            val downloadsPlaylist = repository.allPlaylists.firstOrNull()?.find { it.name == "Downloads" }
            if (downloadsPlaylist != null) {
                val tracks = repository.getTracksInPlaylist(downloadsPlaylist.id).firstOrNull().orEmpty()
                tracks.forEach { t ->
                    repository.removeTrackFromPlaylist(downloadsPlaylist.id, t.id)
                }
            }
            updateCacheSize()
        }
    }

    private val _searchResults = MutableStateFlow<List<Track>>(emptyList())
    val searchResults: StateFlow<List<Track>> = _searchResults.asStateFlow()

    val downloadedTracks: StateFlow<List<Track>> = repository.allPlaylists
        .map { playlists -> playlists.find { it.name == "Downloads" } }
        .flatMapLatest { playlist ->
            if (playlist != null) repository.getTracksInPlaylist(playlist.id)
            else flowOf(emptyList<Track>())
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val followedArtists: StateFlow<List<FollowedArtist>> = repository.followedArtists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleFollowArtist(artistId: String, name: String, imageUrl: String?) {
        viewModelScope.launch {
            val isFollowed = repository.isArtistFollowed(artistId).first()
            if (isFollowed) {
                repository.unfollowArtist(artistId)
            } else {
                repository.followArtist(artistId, name, imageUrl)
            }
        }
    }

    fun isArtistFollowed(artistId: String): kotlinx.coroutines.flow.Flow<Boolean> {
        return repository.isArtistFollowed(artistId)
    }

    private val _downloadProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val downloadProgress: StateFlow<Map<String, Float>> = _downloadProgress.asStateFlow()

    fun isDownloaded(trackId: String): Boolean {
        val context = getApplication<Application>().applicationContext
        val localFile = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "$trackId.m4a")
        return localFile.exists()
    }

    fun deleteDownload(track: Track) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            val localFile = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "${track.id}.m4a")
            if (localFile.exists()) {
                localFile.delete()
            }
            val downloadsPlaylist = repository.allPlaylists.firstOrNull()?.find { it.name == "Downloads" }
            if (downloadsPlaylist != null) {
                repository.removeTrackFromPlaylist(downloadsPlaylist.id, track.id)
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Removed from Downloads", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Currently active YouTube queue (so we don't mix local and YT tracks randomly)
    private val _youtubeQueue = MutableStateFlow<List<Track>>(emptyList())
    private val _searchVideos = MutableStateFlow<List<Track>>(emptyList())
    val searchVideos: StateFlow<List<Track>> = _searchVideos.asStateFlow()

    private val _searchAlbums = MutableStateFlow<List<Album>>(emptyList())
    val searchAlbums: StateFlow<List<Album>> = _searchAlbums.asStateFlow()

    private val _searchArtists = MutableStateFlow<List<Artist>>(emptyList())
    val searchArtists: StateFlow<List<Artist>> = _searchArtists.asStateFlow()

    private val _searchPlaylists = MutableStateFlow<List<com.example.ecodot.data.remote.HomeMediaItem>>(emptyList())
    val searchPlaylists: StateFlow<List<com.example.ecodot.data.remote.HomeMediaItem>> = _searchPlaylists.asStateFlow()

    private val _searchFeaturedPlaylists = MutableStateFlow<List<com.example.ecodot.data.remote.HomeMediaItem>>(emptyList())
    val searchFeaturedPlaylists: StateFlow<List<com.example.ecodot.data.remote.HomeMediaItem>> = _searchFeaturedPlaylists.asStateFlow()

    private val _searchPodcasts = MutableStateFlow<List<com.example.ecodot.data.remote.HomeMediaItem>>(emptyList())
    val searchPodcasts: StateFlow<List<com.example.ecodot.data.remote.HomeMediaItem>> = _searchPodcasts.asStateFlow()

    private val _moodCategoryArtwork = MutableStateFlow<Map<String, String>>(emptyMap())
    val moodCategoryArtwork: StateFlow<Map<String, String>> = _moodCategoryArtwork.asStateFlow()

    private val requestedMoodArtwork = java.util.Collections.synchronizedSet(mutableSetOf<String>())

    val searchHistory: StateFlow<List<RecentSearchItem>> = repository.recentSearches
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _searchSuggestions = MutableStateFlow<List<String>>(emptyList())
    val searchSuggestions: StateFlow<List<String>> = _searchSuggestions.asStateFlow()

    private val _instantSearchItems = MutableStateFlow<List<InstantSearchItem>>(emptyList())
    val instantSearchItems: StateFlow<List<InstantSearchItem>> = _instantSearchItems.asStateFlow()

    private val _videoInfo = MutableStateFlow<VideoInfo?>(null)
    val videoInfo: StateFlow<VideoInfo?> = _videoInfo.asStateFlow()

    private val _isVideoInfoLoading = MutableStateFlow(false)
    val isVideoInfoLoading: StateFlow<Boolean> = _isVideoInfoLoading.asStateFlow()

    private val _homeSections = MutableStateFlow<List<com.example.ecodot.data.remote.HomeSection>>(emptyList())
    val homeSections: StateFlow<List<com.example.ecodot.data.remote.HomeSection>> = _homeSections.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchFocusEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val searchFocusEvent: SharedFlow<Unit> = _searchFocusEvent.asSharedFlow()

    fun triggerSearchFocus() {
        _searchFocusEvent.tryEmit(Unit)
    }

    private val _homeResetEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val homeResetEvent: SharedFlow<Unit> = _homeResetEvent.asSharedFlow()

    fun triggerHomeReset() {
        _homeResetEvent.tryEmit(Unit)
    }

    private val _libraryResetEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val libraryResetEvent: SharedFlow<Unit> = _libraryResetEvent.asSharedFlow()

    fun triggerLibraryReset() {
        _libraryResetEvent.tryEmit(Unit)
    }

    private val _currentTab = MutableStateFlow(HomeTab.Home)
    val currentTab: StateFlow<HomeTab> = _currentTab.asStateFlow()

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private val _controller = MutableStateFlow<MediaController?>(null)
    val controller: StateFlow<MediaController?> = _controller.asStateFlow()

    private var loadTrackJob: Job? = null
    private var fetchRelatedJob: Job? = null
    private var activePlaylistJob: Job? = null
    private var positionUpdaterJob: Job? = null

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isVideoMode = MutableStateFlow(false)
    val isVideoMode: StateFlow<Boolean> = _isVideoMode.asStateFlow()

    @Volatile private var stickyVideoMode: Boolean = false

    private val _videoAspectRatio = MutableStateFlow(16f / 9f)
    val videoAspectRatio: StateFlow<Float> = _videoAspectRatio.asStateFlow()

    data class ActiveStreamTelemetry(
        val bitrateBps: Int = 0,
        val sampleRateHz: Int = 0,
        val codec: String? = null,
        val mimeType: String? = null,
        val itag: String? = null
    )

    private val _activeStreamLabel = MutableStateFlow<String?>(null)
    val activeStreamLabel: StateFlow<String?> = _activeStreamLabel.asStateFlow()

    private val _activeStreamTelemetry = MutableStateFlow(ActiveStreamTelemetry())
    val activeStreamTelemetry: StateFlow<ActiveStreamTelemetry> = _activeStreamTelemetry.asStateFlow()

    /** IDs appended automatically by Endless/Radio mode so explicit Add Queue inserts ahead of them. */
    private val autoGeneratedTrackIds = java.util.Collections.synchronizedSet(mutableSetOf<String>())
    private val queueMutationMutex = kotlinx.coroutines.sync.Mutex()

    private fun invalidateStagedNextCrossfade() {
        sendCustomCommand(EcoDotSessionService.ACTION_INVALIDATE_STAGED_NEXT, Bundle.EMPTY)
    }

    private fun resolveActiveQueueIndex(queueSize: Int, controller: MediaController?): Int {
        if (queueSize <= 0) return -1
        val ctrlIdx = controller?.currentMediaItemIndex ?: C.INDEX_UNSET
        val vmIdx = _currentQueueIndex.value
        val resolved = when {
            ctrlIdx != C.INDEX_UNSET && ctrlIdx in 0 until queueSize -> ctrlIdx
            vmIdx in 0 until queueSize -> vmIdx
            else -> -1
        }
        if (resolved >= 0 && resolved != vmIdx) {
            setIndex(resolved)
        }
        return resolved
    }

    /**
     * Inserts user-queued tracks after any explicitly queued upcoming tracks,
     * right before auto-generated Endless/Radio tracks.
     */
    private fun findUserQueueInsertionIndex(queue: List<Track>, activeIdx: Int): Int {
        if (queue.isEmpty()) return 0
        var idx = (activeIdx + 1).coerceIn(0, queue.size)
        while (idx < queue.size && queue[idx].id !in autoGeneratedTrackIds) {
            idx++
        }
        return idx
    }

    fun findRemovableQueueIndex(trackId: String): Int {
        val queue = _playbackQueue.value
        if (queue.isEmpty()) return -1
        val activeIdx = resolveActiveQueueIndex(queue.size, _controller.value)
        if (activeIdx in queue.indices) {
            val upcomingIdx = ((activeIdx + 1) until queue.size).firstOrNull { queue[it].id == trackId }
            if (upcomingIdx != null) return upcomingIdx
            if (queue[activeIdx].id == trackId) return activeIdx
        }
        return queue.indexOfFirst { it.id == trackId }
    }

    fun isTrackInQueue(trackId: String): Boolean = _playbackQueue.value.any { it.id == trackId }

    private fun resolveOriginalActiveIndex(
        originalQueue: List<Track>,
        activeIdx: Int,
        activeQueue: List<Track>
    ): Int {
        val currentId = activeQueue.getOrNull(activeIdx)?.id ?: _currentTrack.value?.id
        if (currentId != null) {
            val found = originalQueue.indexOfFirst { it.id == currentId }
            if (found >= 0) return found
        }
        return activeIdx.coerceIn(-1, originalQueue.size - 1)
    }

    fun removeTrackFromQueue(track: Track) {
        val idx = findRemovableQueueIndex(track.id)
        if (idx >= 0) {
            val targetTrack = _playbackQueue.value.getOrNull(idx) ?: track
            removeFromQueue(idx, targetTrack)
        } else {
            Toast.makeText(getApplication(), "Track is not in the queue", Toast.LENGTH_SHORT).show()
        }
    }

    fun ensureVideoInfoLoaded(videoId: String, forceRefresh: Boolean = false) {
        if (videoId.isBlank()) return
        if (forceRefresh || _videoInfo.value?.videoId != videoId) {
            fetchVideoInfo(videoId)
        }
    }

    @Volatile private var lastTransitionedMediaId: String? = null
    @Volatile private var pendingExplicitPlayTrackId: String? = null

    private fun isSquareAlbumArtUrl(artUri: String?): Boolean {
        val url = artUri?.lowercase().orEmpty()
        return url.contains("googleusercontent.com")
    }

    private fun isGenericOrVideoPlaceholderAlbum(album: String): Boolean {
        val a = album.trim().lowercase()
        return a.isEmpty() ||
            a == "youtube video" ||
            a == "video" ||
            a == "music video" ||
            a == "video chart" ||
            a == "up next" ||
            a == "unknown" ||
            a == "cached" ||
            a == "cache" ||
            a == "youtube music" ||
            a == "top result" ||
            a.contains("view") ||
            Regex("""^[\d.,]+\s*[kmb]?\s*likes?$""").matches(a)
    }

    fun isTrackVideo(track: Track?): Boolean {
        if (track == null) return false
        val path = track.path.lowercase()
        if (path.startsWith("ytvideo://") || path.contains("ytvideo")) return true

        val album = track.album.trim().lowercase()
        if (album == "youtube video" ||
            album == "video" ||
            album == "music video" ||
            album == "video chart"
        ) return true

        val isSquareArt = isSquareAlbumArtUrl(track.albumArtUri)
        if (track.isYouTube && !isSquareArt) {
            if (album.contains("view") || Regex("""\b\d[\d.,]*\s*[kmb]?\s*likes?\b""").containsMatchIn(album)) {
                return true
            }
            val art = track.albumArtUri?.lowercase().orEmpty()
            val isYtImgThumb = art.contains("i.ytimg.com/vi/")
            val hasPlaceholderAlbum = album.isEmpty() ||
                album == "up next" ||
                album == "unknown" ||
                album == "cached" ||
                album == "cache" ||
                album == "youtube music" ||
                album == "top result"
            if (isYtImgThumb && hasPlaceholderAlbum) {
                return true
            }
        }

        val title = track.title.lowercase()
        return title.contains("official music video") ||
               title.contains("official video") ||
               title.contains("(music video)") ||
               title.contains("[music video]") ||
               title.contains("[mv]") ||
               title.contains("(mv)") ||
               title.contains("lyrical video") ||
               title.contains("lyric video")
    }

    /**
     * Resolves an incoming track against any persisted Room DB track and caller video intent.
     * Ensures explicit video launches upgrade stale DB placeholders and persist `ytvideo://`,
     * while library/recents/cached/history launches inherit the persisted video status.
     */
    private fun resolveTrackForPlayback(
        incoming: Track,
        dbTrack: Track?,
        explicitIsVideo: Boolean
    ): Pair<Track, Boolean> {
        val callerExplicitlyVideo = explicitIsVideo ||
            incoming.path.startsWith("ytvideo://", ignoreCase = true) ||
            incoming.album.equals("YouTube Video", ignoreCase = true) ||
            incoming.album.equals("Music Video", ignoreCase = true) ||
            incoming.album.equals("Video Chart", ignoreCase = true) ||
            incoming.album.equals("Video", ignoreCase = true)

        val callerIsExplicitSong = !explicitIsVideo &&
            incoming.isYouTube &&
            !incoming.path.startsWith("ytvideo://", ignoreCase = true) &&
            !isGenericOrVideoPlaceholderAlbum(incoming.album) &&
            isSquareAlbumArtUrl(incoming.albumArtUri)

        val base = when {
            dbTrack == null -> incoming
            callerExplicitlyVideo -> {
                val upgradedAlbum = when {
                    !isGenericOrVideoPlaceholderAlbum(dbTrack.album) -> dbTrack.album
                    !isGenericOrVideoPlaceholderAlbum(incoming.album) -> incoming.album
                    else -> "YouTube Video"
                }
                dbTrack.copy(
                    title = incoming.title.ifBlank { dbTrack.title },
                    artist = incoming.artist.takeIf { it.isNotBlank() && it != "Unknown" && it != "Unknown Artist" } ?: dbTrack.artist,
                    album = upgradedAlbum,
                    path = if (incoming.isYouTube) "ytvideo://${incoming.id}" else dbTrack.path,
                    albumArtUri = incoming.albumArtUri ?: dbTrack.albumArtUri,
                    duration = if (dbTrack.duration > 0) dbTrack.duration else incoming.duration,
                    isExplicit = dbTrack.isExplicit || incoming.isExplicit
                )
            }
            isGenericOrVideoPlaceholderAlbum(dbTrack.album) && !isGenericOrVideoPlaceholderAlbum(incoming.album) -> {
                dbTrack.copy(
                    title = incoming.title.ifBlank { dbTrack.title },
                    artist = incoming.artist.takeIf { it.isNotBlank() && it != "Unknown" && it != "Unknown Artist" } ?: dbTrack.artist,
                    album = incoming.album,
                    albumArtUri = incoming.albumArtUri ?: dbTrack.albumArtUri,
                    path = if (incoming.path.isNotBlank()) incoming.path else dbTrack.path
                )
            }
            else -> dbTrack
        }

        val shouldBeVideo = when {
            callerExplicitlyVideo -> true
            callerIsExplicitSong -> false
            else -> isTrackVideo(base)
        }

        val upgradedArt = com.example.ecodot.data.remote.YouTubeParser.getHighResUrl(
            base.albumArtUri ?: incoming.albumArtUri,
            isWide = shouldBeVideo
        )
        val finalTrack = if (shouldBeVideo && base.isYouTube && !base.path.startsWith("ytvideo://", ignoreCase = true)) {
            val cleanAlbum = if (isGenericOrVideoPlaceholderAlbum(base.album)) "YouTube Video" else base.album
            base.copy(path = "ytvideo://${base.id}", album = cleanAlbum, albumArtUri = upgradedArt ?: base.albumArtUri)
        } else if (upgradedArt != null && upgradedArt != base.albumArtUri) {
            base.copy(albumArtUri = upgradedArt)
        } else {
            base
        }

        return Pair(finalTrack, shouldBeVideo)
    }

    fun toggleVideoMode() {
        _isVideoMode.value = !_isVideoMode.value
    }

    private val _isCanvasBufferingTimeout = MutableStateFlow(false)
    val isCanvasBufferingTimeout: StateFlow<Boolean> = _isCanvasBufferingTimeout.asStateFlow()

    private val networkMonitor = NetworkMonitor(application)

    val networkState: StateFlow<NetworkState> = combine(
        networkMonitor.networkState,
        _isCanvasBufferingTimeout
    ) { monitorState, bufferingTimeout ->
        NetworkState(
            isConnected = monitorState.isConnected,
            isPoorConnection = monitorState.isPoorConnection || bufferingTimeout
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NetworkState(isConnected = true, isPoorConnection = false)
    )

    private var canvasBufferingJob: Job? = null

    private val _isCanvasEnabledByUser = MutableStateFlow(false)
    val isCanvasEnabledByUser: StateFlow<Boolean> = _isCanvasEnabledByUser.asStateFlow()

    private val _canvasPlayer = MutableStateFlow<ExoPlayer?>(null)
    val canvasPlayer: StateFlow<ExoPlayer?> = _canvasPlayer.asStateFlow()

    /** Call this to start/replace the canvas for the current track. */
    @androidx.media3.common.util.UnstableApi
    fun setupCanvasPlayer(context: Context, url: String) {
        if (!_isCanvasEnabledByUser.value) {
            Log.d("MusicViewModel", "setupCanvasPlayer: Suppressed canvas because user has not explicitly enabled canvas")
            releaseCanvasPlayer()
            return
        }
        if (url == "DISABLED") {
            releaseCanvasPlayer()
            return
        }
        if (networkState.value.isPoorConnection) {
            Log.w("MusicViewModel", "setupCanvasPlayer: Suppressed canvas load due to poor/offline network connection")
            return
        }
        viewModelScope.launch(Dispatchers.Main) {
            canvasBufferingJob?.cancel()
            _isCanvasBufferingTimeout.value = false
            releaseCanvasPlayer()

            // Resolve the final playable URL on the IO thread
            val finalUrl: String? = withContext(Dispatchers.IO) {
                when {
                    // ytvideo:// — resolve each song's own music video stream
                    url.startsWith("ytvideo://") -> {
                        val videoId = url.removePrefix("ytvideo://")
                        Log.e("MusicViewModel", "setupCanvasPlayer: resolving YouTube video stream for: $videoId")
                        repository.getYouTubeStreamUrl(
                            videoId = videoId,
                            quality = prefsManager.audioQuality,
                            videoQuality = VideoQuality.HIGH, // Canvas always uses high resolution
                            isVideo = true
                        )
                    }
                    // Legacy blocked URLs — intercept and clean DB
                    url.contains("mixkit.co") || url.contains("commondatastorage.googleapis.com") -> {
                        Log.e("MusicViewModel", "setupCanvasPlayer: intercepting legacy/blocked URL: $url")
                        val cleanUrl = "https://www.w3schools.com/html/mov_bbb.mp4"
                        _currentTrack.value?.let { track ->
                            if (track.canvasUrl == url) setCanvasUrl(track, cleanUrl)
                        }
                        cleanUrl
                    }
                    // Any other HTTPS URL — pass through as-is
                    else -> url
                }
            }

            if (finalUrl == null) {
                Log.e("MusicViewModel", "setupCanvasPlayer: could not resolve stream URL for $url — canvas skipped")
                return@launch
            }

            Log.e("MusicViewModel", "setupCanvasPlayer: initializing ExoPlayer with URL: $finalUrl")

            // Use Chrome User-Agent to prevent 403 Forbidden issues (hotlink protection / bot detection)
            val userAgent = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
            val dataSourceFactory = androidx.media3.datasource.DefaultHttpDataSource.Factory()
                .setUserAgent(userAgent)
                .setAllowCrossProtocolRedirects(true)
                .setConnectTimeoutMs(15_000)
                .setReadTimeoutMs(15_000)
            val mediaSourceFactory = androidx.media3.exoplayer.source.DefaultMediaSourceFactory(context)
                .setDataSourceFactory(dataSourceFactory)

            // Canvas is a short looping clip — keep buffers small to save RAM
            val canvasLoadControl = androidx.media3.exoplayer.DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    5_000,  // Min buffer (5s — enough to start a short loop)
                    10_000, // Max buffer (10s — canvas loops are usually <30s)
                    1_000,  // Playback start threshold
                    2_000   // Rebuffer threshold
                )
                .build()

            val player = ExoPlayer.Builder(context)
                .setMediaSourceFactory(mediaSourceFactory)
                .setLoadControl(canvasLoadControl)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(MediaC.AUDIO_CONTENT_TYPE_MOVIE)
                        .setUsage(MediaC.USAGE_MEDIA)
                        .build(),
                    false // do NOT request audio focus
                )
                .build().apply {
                    volume = 0f                          // fully muted — audio comes from main player
                    repeatMode = Player.REPEAT_MODE_ONE  // loop forever
                    playWhenReady = _isPlaying.value
                    setMediaItem(MediaItem.fromUri(finalUrl))
                    addListener(object : Player.Listener {
                        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                            Log.e("MusicViewModel", "Canvas ExoPlayer Error: ${error.message} for URL: $finalUrl", error)
                            viewModelScope.launch(Dispatchers.Main) {
                                _isCanvasBufferingTimeout.value = true
                                releaseCanvasPlayer()
                            }
                        }
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            Log.d("MusicViewModel", "Canvas ExoPlayer State: $playbackState")
                            if (playbackState == Player.STATE_BUFFERING) {
                                canvasBufferingJob?.cancel()
                                canvasBufferingJob = viewModelScope.launch(Dispatchers.Main) {
                                    delay(10_000) // Extended to 10s — HLS/DASH manifests take longer to parse
                                    Log.w("MusicViewModel", "Canvas buffering timeout after 10s. Auto-disabling canvas.")
                                    _isCanvasBufferingTimeout.value = true
                                    releaseCanvasPlayer()
                                }
                            } else if (playbackState == Player.STATE_READY) {
                                canvasBufferingJob?.cancel()
                                _isCanvasBufferingTimeout.value = false
                            }
                        }
                    })
                    prepare()
                }
            _canvasPlayer.value = player
        }
    }

    /** Pause / resume the canvas when the main player play-state changes. */
    fun syncCanvasPlayback(isPlaying: Boolean) {
        _canvasPlayer.value?.playWhenReady = isPlaying
    }

    /** Store a canvas URL on a track (persisted to Room). */
    fun setCanvasUrl(track: Track, url: String?) {
        viewModelScope.launch {
            val updated = track.copy(canvasUrl = if (url.isNullOrBlank()) null else url)
            database.trackDao().upsertTrack(updated)
            if (_currentTrack.value?.id == track.id) {
                _currentTrack.value = updated
                if (updated.canvasUrl.isNullOrBlank() || updated.canvasUrl == "DISABLED" || !_isCanvasEnabledByUser.value) {
                    releaseCanvasPlayer()
                } else {
                    setupCanvasPlayer(getApplication(), updated.canvasUrl)
                }
            }
        }
    }

    /** Search for a fan-uploaded Spotify Canvas YouTube Short and set it. */
    fun enableCanvasForTrack(track: Track) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val query = "${track.artist} ${track.title} spotify canvas"
                Log.d("MusicViewModel", "Searching for canvas short with query: $query")
                val results = repository.searchYouTubeCategorized(query, "Videos")
                
                // Try to find a short video (often first result is a short)
                val shortVideo = results.songs.firstOrNull { it.id.isNotEmpty() }
                
                val canvasUrl = if (shortVideo != null) {
                    Log.d("MusicViewModel", "Found canvas short videoId: ${shortVideo.id}")
                    "ytvideo://${shortVideo.id}"
                } else {
                    Log.w("MusicViewModel", "No canvas short found, falling back to track's video")
                    if (track.isYouTube) "ytvideo://${track.id}"
                    else "https://www.w3schools.com/html/mov_bbb.mp4"
                }
                
                setCanvasUrl(track, canvasUrl)
            } catch (e: Exception) {
                Log.e("MusicViewModel", "Canvas search failed", e)
                val fallbackUrl = if (track.isYouTube) "ytvideo://${track.id}" else "https://www.w3schools.com/html/mov_bbb.mp4"
                setCanvasUrl(track, fallbackUrl)
            }
        }
    }

    fun enableCanvasForCurrentTrack(context: Context) {
        val track = _currentTrack.value ?: return
        _isCanvasEnabledByUser.value = true
        val url = track.canvasUrl
        if (!url.isNullOrEmpty() && url != "DISABLED") {
            setupCanvasPlayer(context, url)
        } else {
            enableCanvasForTrack(track)
        }
    }

    fun disableCanvas() {
        _isCanvasEnabledByUser.value = false
        releaseCanvasPlayer()
    }

    fun resetCanvasForNewTrack() {
        _isCanvasEnabledByUser.value = false
        releaseCanvasPlayer()
    }

    fun releaseCanvasPlayer() {
        canvasBufferingJob?.cancel()
        _canvasPlayer.value?.release()
        _canvasPlayer.value = null
    }

    // ── Canvas lifecycle: release when NowPlayingScreen is not visible ──
    private val _isNowPlayingVisible = MutableStateFlow(false)

    fun setNowPlayingVisible(visible: Boolean) {
        _isNowPlayingVisible.value = visible
        if (!visible) {
            releaseCanvasPlayer()
        }
    }

    // Tracks whether any track has been loaded (for mini player visibility)
    private val _hasTrackLoaded = MutableStateFlow(false)
    val hasTrackLoaded: StateFlow<Boolean> = _hasTrackLoaded.asStateFlow()

    private val _position = MutableStateFlow(0L)
    val position: StateFlow<Long> = _position.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _shuffleMode = MutableStateFlow(false)
    val shuffleMode: StateFlow<Boolean> = _shuffleMode.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _playbackQueue = MutableStateFlow<List<Track>>(emptyList())
    val playbackQueue: StateFlow<List<Track>> = _playbackQueue.asStateFlow()

    private val _originalPlaybackQueue = MutableStateFlow<List<Track>>(emptyList())

    // Internal queue index — mirrors to SharedPlaybackQueue so the service can read it directly
    private val _currentQueueIndex = MutableStateFlow(-1)
    val currentQueueIndex: StateFlow<Int> = _currentQueueIndex.asStateFlow()

    /** Write queue */
    private fun setQueue(list: List<Track>) {
        _playbackQueue.value = list
    }

    /** Write index */
    private fun setIndex(idx: Int) {
        _currentQueueIndex.value = idx
    }

    private val _endlessMode = MutableStateFlow(true)
    val endlessMode: StateFlow<Boolean> = _endlessMode.asStateFlow()

    private val _lyrics = MutableStateFlow<LrcLibResponse?>(null)
    val lyrics: StateFlow<LrcLibResponse?> = _lyrics.asStateFlow()

    private val _syncedLyrics = MutableStateFlow<List<LyricLine>>(emptyList())
    val syncedLyrics: StateFlow<List<LyricLine>> = _syncedLyrics.asStateFlow()

    private val _isLyricsLoading = MutableStateFlow(false)
    val isLyricsLoading: StateFlow<Boolean> = _isLyricsLoading.asStateFlow()

    private val _dominantColor = MutableStateFlow(Color(0xFF191C19)) // Default background
    val dominantColor: StateFlow<Color> = _dominantColor.asStateFlow()

    private val _accentColor = MutableStateFlow(Color(0xFF79DA9F)) // Default primary
    val accentColor: StateFlow<Color> = _accentColor.asStateFlow()

    val allAlbums: StateFlow<List<Album>> = combine(
        _tracks,
        repository.getAllPlaylistTrackIds()
    ) { tracks, playlistTrackIds ->
        val savedTrackIds = playlistTrackIds.toSet()
        val savedTracks = tracks.filter { it.id in savedTrackIds }
        savedTracks.filter { it.album.isNotBlank() }.groupBy { it.album }.map { (title, albumTracks) ->
            Album(
                id = albumTracks.first().albumId.takeIf { !it.isNullOrBlank() } ?: title,
                title = title,
                artist = albumTracks.first().artist,
                artUri = albumTracks.first().albumArtUri,
                tracks = albumTracks
            )
        }.sortedBy { it.title }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allArtists: StateFlow<List<Artist>> = combine(
        _tracks,
        repository.getAllPlaylistTrackIds()
    ) { tracks, playlistTrackIds ->
        val savedTrackIds = playlistTrackIds.toSet()
        val savedTracks = tracks.filter { it.id in savedTrackIds }
        savedTracks.filter { it.artist.isNotBlank() }.groupBy { it.artist }.map { (name, artistTracks) ->
            val firstTrack = artistTracks.first()
            Artist(
                id = firstTrack.artistId.takeIf { !it.isNullOrBlank() } ?: name,
                name = name,
                imageUrl = firstTrack.albumArtUri
            )
        }.sortedBy { it.name }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allGenres: StateFlow<List<Genre>> = _tracks.map { tracks ->
        tracks.groupBy { it.genre ?: "Unknown" }.map { (name, genreTracks) ->
            Genre(name = name, tracks = genreTracks)
        }.sortedBy { it.name }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Track loading state to prevent double-tap issues
    private val _isLoadingTrack = MutableStateFlow(false)
    val isLoadingTrack: StateFlow<Boolean> = _isLoadingTrack.asStateFlow()

    private val _playbackSource = MutableStateFlow("Library")
    val playbackSource: StateFlow<String> = _playbackSource.asStateFlow()

    // Glassmorphism and Theme settings
    private val _blurIntensity = MutableStateFlow(60f)
    val blurIntensity: StateFlow<Float> = _blurIntensity.asStateFlow()

// Equalizer & FX state
    private val _eqEnabled = MutableStateFlow(prefsManager.equalizerEnabled)
    val eqEnabled: StateFlow<Boolean> = _eqEnabled.asStateFlow()

    private val initialEqBands: Map<Int, Int> = run {
        val savedPreset = com.example.ecodot.domain.model.DEFAULT_EQ_PRESETS.find { it.name == prefsManager.selectedEqPreset }
        if (savedPreset != null) {
            savedPreset.bands.mapIndexed { idx, value -> idx to value }.toMap()
        } else {
            mapOf(0 to 0, 1 to 0, 2 to 0, 3 to 0, 4 to 0)
        }
    }
    private val _eqBands = MutableStateFlow(initialEqBands)
    val eqBands: StateFlow<Map<Int, Int>> = _eqBands.asStateFlow()

    private val _selectedEqPreset = MutableStateFlow(prefsManager.selectedEqPreset)
    val selectedEqPreset: StateFlow<String> = _selectedEqPreset.asStateFlow()

    private val _playerStyle = MutableStateFlow(prefsManager.playerStyle)
    val playerStyle: StateFlow<com.example.ecodot.data.local.prefs.PlayerStyle> = _playerStyle.asStateFlow()

    private val _virtualizerStrength = MutableStateFlow(prefsManager.virtualizerStrength)
    val virtualizerStrength: StateFlow<Int> = _virtualizerStrength.asStateFlow()


    // History and Profile state
    val recentlyPlayed: StateFlow<List<Track>> = repository.recentlyPlayed.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val playbackHistory: StateFlow<List<PlaybackHistoryWithTrack>> = repository.playbackHistory.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val mostPlayed: StateFlow<List<Track>> = repository.mostPlayed.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val userProfile: StateFlow<UserProfile?> = repository.userProfile.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Playlists state
    val allPlaylists: StateFlow<List<com.example.ecodot.data.local.entities.Playlist>> =
        repository.allPlaylists.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activePlaylistTracks = MutableStateFlow<List<Track>>(emptyList())
    val activePlaylistTracks: StateFlow<List<Track>> = _activePlaylistTracks.asStateFlow()

    private val _activePlaylist = MutableStateFlow<com.example.ecodot.data.local.entities.Playlist?>(null)
    val activePlaylist: StateFlow<com.example.ecodot.data.local.entities.Playlist?> = _activePlaylist.asStateFlow()


    // Derived stats for Profile
    val totalMinutesListened: StateFlow<Int> = mostPlayed.combine(userProfile) { tracks, _ ->
        tracks.sumOf { (it.playCount * (it.duration / 60000.0)).toInt() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val topGenre: StateFlow<String> = userProfile.combine(mostPlayed) { profile, tracks ->
        profile?.favoriteGenres?.split(",")?.firstOrNull()?.trim() 
            ?: tracks.firstOrNull()?.artist // Fallback to top artist if no genre
            ?: "Discovery"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Discovery")

    val onRepeatTracks: StateFlow<List<Track>> = repository.getOnRepeatTracks(
        System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
    ).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _moodGreeting = MutableStateFlow("Time-of-Day Mix")
    val moodGreeting: StateFlow<String> = _moodGreeting.asStateFlow()

    private val _moodRecommendedTracks = MutableStateFlow<List<Track>>(emptyList())
    val moodRecommendedTracks: StateFlow<List<Track>> = _moodRecommendedTracks.asStateFlow()

    private val _suggestedTracks = MutableStateFlow<List<Track>>(com.example.ecodot.data.remote.RealHomeData.getRealQuickPicks())
    val suggestedTracks: StateFlow<List<Track>> = _suggestedTracks.asStateFlow()

    private val _realMusicVideos = MutableStateFlow<List<com.example.ecodot.data.remote.HomeMediaItem>>(com.example.ecodot.data.remote.RealHomeData.getRealMusicVideos())
    val realMusicVideos: StateFlow<List<com.example.ecodot.data.remote.HomeMediaItem>> = _realMusicVideos.asStateFlow()

    private val _selectedHomeCountry = MutableStateFlow(prefsManager.selectedHomeCountry)
    val selectedHomeCountry: StateFlow<String> = _selectedHomeCountry.asStateFlow()

    private val _countryVideoCharts = MutableStateFlow<List<com.example.ecodot.data.remote.HomeMediaItem>>(
        com.example.ecodot.data.remote.RealHomeData.getRealCountryCharts(prefsManager.selectedHomeCountry)
    )
    val countryVideoCharts: StateFlow<List<com.example.ecodot.data.remote.HomeMediaItem>> = _countryVideoCharts.asStateFlow()

    private val _topChartArtists = MutableStateFlow<List<Triple<String, String, String>>>(
        com.example.ecodot.data.remote.RealHomeData.getRealTopArtists(prefsManager.selectedHomeCountry)
    )
    val topChartArtists: StateFlow<List<Triple<String, String, String>>> = _topChartArtists.asStateFlow()

    private val _topArtistChannelIds = MutableStateFlow<Map<String, String>>(emptyMap())
    val topArtistChannelIds: StateFlow<Map<String, String>> = _topArtistChannelIds.asStateFlow()

    private val _chartPodcasts = MutableStateFlow<List<com.example.ecodot.data.remote.HomeChartPodcast>>(
        com.example.ecodot.data.remote.RealHomeData.getRealPodcasts().mapIndexed { idx, (t, h, img) ->
            com.example.ecodot.data.remote.HomeChartPodcast(
                rank = "${idx + 1}",
                title = t,
                host = h,
                thumbnailUrl = img,
                browseId = ""
            )
        }
    )
    val chartPodcasts: StateFlow<List<com.example.ecodot.data.remote.HomeChartPodcast>> = _chartPodcasts.asStateFlow()

    private val _isChartLoading = MutableStateFlow(false)
    val isChartLoading: StateFlow<Boolean> = _isChartLoading.asStateFlow()

    private val _homeMoodsData = MutableStateFlow(com.example.ecodot.data.remote.HomeMoodsData())
    val homeMoodsData: StateFlow<com.example.ecodot.data.remote.HomeMoodsData> = _homeMoodsData.asStateFlow()

    private val _isHomeLoading = MutableStateFlow(true)
    val isHomeLoading: StateFlow<Boolean> = _isHomeLoading.asStateFlow()

    private val _isHomeRefreshing = MutableStateFlow(false)
    val isHomeRefreshing: StateFlow<Boolean> = _isHomeRefreshing.asStateFlow()

    private val _isHomePaginating = MutableStateFlow(false)
    val isHomePaginating: StateFlow<Boolean> = _isHomePaginating.asStateFlow()

    private val _homeContinuation = MutableStateFlow<String?>(null)
    val homeContinuation: StateFlow<String?> = _homeContinuation.asStateFlow()

    private val _homeBackgroundUrl = MutableStateFlow<String?>(null)
    val homeBackgroundUrl: StateFlow<String?> = _homeBackgroundUrl.asStateFlow()

    private val _homeHeroColor = MutableStateFlow(Color(0xFFD69A32))
    val homeHeroColor: StateFlow<Color> = _homeHeroColor.asStateFlow()

    @androidx.compose.runtime.Immutable
    data class BrowseSheetUiState(
        val isVisible: Boolean = false,
        val title: String = "",
        val isLoading: Boolean = false,
        val sections: List<com.example.ecodot.data.remote.HomeSection> = emptyList(),
        val moodFilterName: String? = null
    )

    private val _browseSheetState = MutableStateFlow(BrowseSheetUiState())
    val browseSheetState: StateFlow<BrowseSheetUiState> = _browseSheetState.asStateFlow()

    @androidx.compose.runtime.Immutable
    data class BrowsePageUiState(
        val title: String = "",
        val strapline: String? = null,
        val isLoading: Boolean = false,
        val songs: List<Track> = emptyList(),
        val albums: List<Album> = emptyList(),
        val playlists: List<com.example.ecodot.data.remote.HomeMediaItem> = emptyList(),
        val videos: List<com.example.ecodot.data.remote.HomeMediaItem> = emptyList(),
        val artists: List<Artist> = emptyList(),
        val moodFilterName: String? = null,
        val browseId: String? = null,
        val params: String? = null,
        val fallbackQuery: String? = null
    )

    private val _browsePageState = MutableStateFlow(BrowsePageUiState())
    val browsePageState: StateFlow<BrowsePageUiState> = _browsePageState.asStateFlow()

    private var browsePageJob: Job? = null

    private var currentHomeParams: String? = null
    private var currentHomeMoodName: String? = null
    private var homeFeedJob: Job? = null
    private var homeRefreshCycle: Int = 0

    fun loadHomeContentForCountry(country: String) {
        _selectedHomeCountry.value = country
        prefsManager.selectedHomeCountry = country
        _countryVideoCharts.value = com.example.ecodot.data.remote.RealHomeData.getRealCountryCharts(country)
        _topChartArtists.value = com.example.ecodot.data.remote.RealHomeData.getRealTopArtists(country)
        _isChartLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val liveCharts = repository.getChartsForCountry(country)
                if (liveCharts.topArtists.isNotEmpty()) {
                    _topChartArtists.value = liveCharts.topArtists.map { Triple(it.title, it.subscribers.ifBlank { "Top Chart Artist" }, it.thumbnailUrl) }
                    _topArtistChannelIds.value = liveCharts.topArtists.filter { it.browseId.startsWith("UC") }.associate { it.title to it.browseId }
                }
                if (liveCharts.topPodcasts.isNotEmpty()) {
                    _chartPodcasts.value = liveCharts.topPodcasts
                }
                if (liveCharts.videoCharts.isNotEmpty()) {
                    _countryVideoCharts.value = liveCharts.videoCharts
                    _realMusicVideos.value = (liveCharts.videoCharts + _realMusicVideos.value).distinctBy { it.id }.take(14)
                } else {
                    val results = repository.searchYouTubeCategorized("$country Trending Music Videos", "Videos")
                    if (results.songs.isNotEmpty()) {
                        val mediaItems = results.songs.map { track ->
                            com.example.ecodot.data.remote.HomeMediaItem(
                                id = track.id,
                                title = track.title,
                                subtitle = "${track.artist} • Trending Video",
                                thumbnailUrl = track.albumArtUri ?: "https://i.ytimg.com/vi/${track.id}/hqdefault.jpg",
                                type = com.example.ecodot.data.remote.MediaType.TRACK,
                                aspectRatio = 16f / 9f
                            )
                        }
                        _countryVideoCharts.value = mediaItems.take(10)
                        _realMusicVideos.value = mediaItems
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching charts/trending videos for $country", e)
            } finally {
                _isChartLoading.value = false
            }
        }
    }

    private val _historyRecommendations = MutableStateFlow<List<Track>>(emptyList())
    val historyRecommendations: StateFlow<List<Track>> = _historyRecommendations.asStateFlow()

    init {
        // Automatically check for updates on startup
        viewModelScope.launch(Dispatchers.IO) {
            delay(2500)
            try {
                appUpdateManager.checkForUpdates(force = false)
            } catch (e: Exception) {
                Log.w(TAG, "Startup update check skipped: ${e.message}")
            }
        }

        loadHomeContentForCountry(prefsManager.selectedHomeCountry)
        viewModelScope.launch {
            repository.initDefaultPlaylists()
        }
        // One-time startup repair for existing cached/recents/history tracks in Room DB
        viewModelScope.launch(Dispatchers.IO) {
            try {
                database.trackDao().fixMisclassifiedVideoTracks()
                database.trackDao().fixMisclassifiedAudioTracks()
            } catch (e: Exception) {
                Log.w(TAG, "DB track cleanup skipped: ${e.message}")
            }
        }
        viewModelScope.launch {
            repository.allTracks.collectLatest {
                _tracks.value = it
            }           
        }
        cleanOrphanedDownloads()

        // Fetch suggestions ONCE at startup (not on every DB change)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val tracks = repository.mostPlayed.first()
                if (tracks.isNotEmpty()) {
                    fetchSuggestions(tracks.first())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch initial suggestions", e)
            }
        }

        // Fetch taste-matched recommendations ONCE at startup
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val history = repository.recentlyPlayed.first()
                if (history.isNotEmpty()) {
                    val seed = history.first()
                    try {
                        val related = repository.getYouTubeRelatedTracks(seed.id)
                        _historyRecommendations.value = related.take(15)
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to fetch taste recommendations", e)
                        try {
                            val searchResults = repository.searchYouTube("${seed.artist} ${seed.title}")
                            val firstResult = searchResults.firstOrNull()
                            if (firstResult != null) {
                                val relatedSearch = repository.getYouTubeRelatedTracks(firstResult.id)
                                _historyRecommendations.value = relatedSearch.take(15)
                            }
                        } catch (e2: Exception) {
                            Log.e(TAG, "Failed fallback search taste recommendations", e2)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load history for recommendations", e)
            }
        }
        
        val sessionToken = SessionToken(application, ComponentName(application, EcoDotSessionService::class.java))
        controllerFuture = MediaController.Builder(application, sessionToken)
            .setListener(object : MediaController.Listener {
                override fun onExtrasChanged(controller: MediaController, extras: Bundle) {
                    _isCrossfading.value = extras.getBoolean(
                        EcoDotSessionService.EXTRA_IS_CROSSFADING,
                        false
                    )
                    applyStreamTelemetryExtras(extras)
                }
            })
            .buildAsync()
        
        controllerFuture?.addListener({
            try {
                val mediaController = controllerFuture?.get()
                _controller.value = mediaController
                _isCrossfading.value = mediaController?.sessionExtras?.getBoolean(
                    EcoDotSessionService.EXTRA_IS_CROSSFADING,
                    false
                ) ?: false
                mediaController?.sessionExtras?.let { applyStreamTelemetryExtras(it) }
                
                // Initial sync upon connect
                val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
                val queueType = Types.newParameterizedType(List::class.java, Track::class.java)
                val json = moshi.adapter<List<Track>>(queueType).toJson(_playbackQueue.value)
                _controller.value?.sendCustomCommand(
                    SessionCommand(EcoDotSessionService.ACTION_UPDATE_QUEUE, Bundle.EMPTY),
                    Bundle().apply { putString("queue_json", json) }
                )
                _controller.value?.sendCustomCommand(
                    SessionCommand(EcoDotSessionService.ACTION_UPDATE_INDEX, Bundle.EMPTY),
                    Bundle().apply { putInt("index", _currentQueueIndex.value) }
                )
                _controller.value?.addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        _isPlaying.value = isPlaying
                        if (isPlaying) {
                            _hasTrackLoaded.value = true
                            startPositionUpdater()
                        }
                    }

                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        val trackId = mediaItem?.mediaId
                        if (trackId != null) {
                            val isMetadataRefreshOnly =
                                reason == Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED &&
                                trackId == lastTransitionedMediaId &&
                                pendingExplicitPlayTrackId != trackId &&
                                !_isCrossfading.value
                            if (isMetadataRefreshOnly) {
                                return
                            }
                            lastTransitionedMediaId = trackId
                            if (pendingExplicitPlayTrackId == trackId) {
                                pendingExplicitPlayTrackId = null
                            }

                            markTrackAsCached(trackId)
                            resetCanvasForNewTrack()
                            _videoAspectRatio.value = 16f / 9f
                            lastServiceBitrateBps = 0
                            lastServiceSampleRateHz = 0
                            val cachedMeta = com.example.ecodot.data.remote.YouTubeStreamResolver.resolvedAudioTelemetry[trackId]
                            publishResolvedTelemetry(
                                bitrateBps = cachedMeta?.bitrateBps ?: 0,
                                sampleRateHz = cachedMeta?.sampleRateHz ?: 0,
                                codec = cachedMeta?.codec,
                                mimeType = cachedMeta?.mimeType,
                                itag = cachedMeta?.itag?.takeIf { it > 0 }?.toString()
                            )
                            val shouldContinueVideoPlayback = stickyVideoMode || _controller.value?.playWhenReady == true || _isPlaying.value
                            viewModelScope.launch {
                                val dbOrQueueTrack = withContext(Dispatchers.IO) {
                                    database.trackDao().getTrackById(trackId)
                                } ?: findTrackInQueue(trackId)
                                val currentUri = mediaItem.localConfiguration?.uri?.toString() ?: ""
                                val uriIsVideo = currentUri.startsWith("ytvideo://", ignoreCase = true)
                                val keepVideo = stickyVideoMode || uriIsVideo
                                val (resolvedTrack, shouldBeVideo) = if (dbOrQueueTrack != null) {
                                    resolveTrackForPlayback(
                                        incoming = dbOrQueueTrack,
                                        dbTrack = dbOrQueueTrack,
                                        explicitIsVideo = keepVideo
                                    )
                                } else {
                                    Pair(null, keepVideo)
                                }

                                if (resolvedTrack != null) {
                                    _currentTrack.value = resolvedTrack
                                    _isVideoMode.value = shouldBeVideo

                                    if (resolvedTrack != dbOrQueueTrack) {
                                        withContext(Dispatchers.IO) {
                                            runCatching { repository.saveTrack(resolvedTrack) }
                                        }
                                    }

                                    // Ensure controller plays ytvideo:// if in video mode or detected as video
                                    val ctrl = _controller.value
                                    if (shouldBeVideo && resolvedTrack.isYouTube && !isDownloaded(trackId) && !uriIsVideo && ctrl != null) {
                                        val updatedMediaItem = buildMediaItem(resolvedTrack, forceVideo = true)
                                        val currentIndex = ctrl.currentMediaItemIndex
                                        if (currentIndex != androidx.media3.common.C.INDEX_UNSET) {
                                            ctrl.replaceMediaItem(currentIndex, updatedMediaItem)
                                            ctrl.prepare()
                                            if (shouldContinueVideoPlayback) {
                                                ctrl.playWhenReady = true
                                                ctrl.play()
                                            }
                                        }
                                    } else if (shouldBeVideo && shouldContinueVideoPlayback && ctrl != null) {
                                        if (ctrl.playbackState == Player.STATE_IDLE) {
                                            ctrl.prepare()
                                        }
                                        ctrl.playWhenReady = true
                                        ctrl.play()
                                    }
                                    if (shouldBeVideo && ctrl != null) {
                                        val nextIdx = ctrl.nextMediaItemIndex
                                        if (nextIdx != C.INDEX_UNSET) prepareTrackVideoModeAtIndex(ctrl, nextIdx)
                                    }
                                } else {
                                    _currentTrack.value = null
                                    _isVideoMode.value = shouldBeVideo
                                }

                                _hasTrackLoaded.value = true
                                updateDuration()
                                
                                // Update queue index reliably
                                val idx = _controller.value?.currentMediaItemIndex ?: _playbackQueue.value.indexOfFirst { it.id == trackId }
                                if (idx >= 0) setIndex(idx)
                                
                                // Fetch Lyrics
                                resolvedTrack?.let { fetchLyrics(it) }

                                 // Fetch rich video info for info sheet
                                if (resolvedTrack?.isYouTube == true) {
                                    fetchVideoInfo(trackId)
                                }

                                // Update colors from palette
                                resolvedTrack?.albumArtUri?.let { uri ->
                                    updateThemeFromArt(uri)
                                }

                                // Endless logic: if we are near the end, fetch more
                                if (_endlessMode.value && resolvedTrack?.isYouTube == true) {
                                    checkAndFetchRelatedTracks(trackId)
                                }

                                // Automatically log playback on transitions
                                resolvedTrack?.let { repository.logPlayback(it) }
                            }
                        }
                    }

                    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                        // Sync VM state when notification button changes shuffle (keeps UI in sync)
                        _shuffleMode.value = shuffleModeEnabled
                    }

                    override fun onRepeatModeChanged(repeatMode: Int) {
                        // Sync VM state when notification button changes repeat mode
                        _repeatMode.value = repeatMode
                    }

                    override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) {
                        updateDuration()
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_READY) {
                            val dur = _controller.value?.duration ?: 0L
                            if (dur != C.TIME_UNSET && dur > 0) {
                                _duration.value = dur
                                // Update current track duration if it was 0 (common for YouTube related tracks)
                                _currentTrack.value?.let { track ->
                                    if (track.duration == 0L) {
                                        val updatedTrack = track.copy(duration = dur)
                                        _currentTrack.value = updatedTrack
                                        // Update in queue to persist the duration
                                        val queue = _playbackQueue.value.toMutableList()
                                        val idx = queue.indexOfFirst { it.id == track.id }
                                        if (idx != -1) {
                                            queue[idx] = updatedTrack
                                            _playbackQueue.value = queue
                                        }
                                        // Only retry lyrics fetch with real duration if we do not already have lyrics
                                        if (_lyrics.value == null && !lyricsMemoryCache.containsKey(updatedTrack.id)) {
                                            fetchLyrics(updatedTrack)
                                        }
                                    }
                                }
                            }
                        } else if (playbackState == Player.STATE_ENDED) {
                            // Service now handles auto-skip internally for background stability.
                        }
                    }

                    override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
                        if (videoSize.width > 0 && videoSize.height > 0) {
                            val ratio = (videoSize.width.toFloat() * videoSize.pixelWidthHeightRatio) / videoSize.height.toFloat()
                            _videoAspectRatio.value = ratio.coerceIn(0.45f, 2.4f)
                        }
                    }

                    override fun onTracksChanged(tracks: androidx.media3.common.Tracks) {
                        updateStreamLabelFromTracks(tracks)
                    }
                })

                // Sync initial crossfade state; live transitions also update via MediaController.Listener.onExtrasChanged
                
                // --- RESTORE STATE ON APP REOPEN ---
                // If the service is already playing (app was killed but service is still running),
                // restore the current track and playback state immediately.
                val controller = _controller.value
                if (controller != null) {
                    val currentItem = controller.currentMediaItem
                    if (currentItem != null) {
                        _hasTrackLoaded.value = true
                        _isPlaying.value = controller.isPlaying
                        _position.value = controller.currentPosition
                        val dur = controller.duration
                        _duration.value = if (dur != C.TIME_UNSET) dur else 0L
                        
                        // Try to resolve track from DB
                        viewModelScope.launch {
                            val trackId = currentItem.mediaId
                            lastTransitionedMediaId = trackId
                            val uriIsVideo = currentItem.localConfiguration?.uri?.scheme.equals("ytvideo", ignoreCase = true)
                            val track = withContext(Dispatchers.IO) {
                                database.trackDao().getTrackById(trackId)
                            } ?: findTrackInQueue(trackId)
                            if (track != null) {
                                val (resolved, shouldBeVideo) = resolveTrackForPlayback(track, track, uriIsVideo)
                                _currentTrack.value = resolved
                                _isVideoMode.value = shouldBeVideo
                                markTrackAsCached(resolved.id)
                                resolved.albumArtUri?.let { updateThemeFromArt(it) }
                                fetchLyrics(resolved)
                            } else {
                                // Fallback: construct minimal track from MediaMetadata
                                val meta = currentItem.mediaMetadata
                                val fallbackTrack = Track(
                                    id = trackId,
                                    title = meta.title?.toString() ?: "Unknown",
                                    artist = meta.artist?.toString() ?: "Unknown",
                                    album = if (uriIsVideo) "YouTube Video" else (meta.albumTitle?.toString() ?: ""),
                                    albumArtUri = com.example.ecodot.data.remote.YouTubeParser.getHighResUrl(
                                        meta.artworkUri?.toString(),
                                        isWide = uriIsVideo
                                    ),
                                    duration = _duration.value,
                                    path = currentItem.localConfiguration?.uri?.toString() ?: "",
                                    isYouTube = true
                                )
                                _currentTrack.value = fallbackTrack
                                _isVideoMode.value = uriIsVideo || isTrackVideo(fallbackTrack)
                            }
                            if (controller.isPlaying) startPositionUpdater()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to connect to media session", e)
            }
        }, MoreExecutors.directExecutor())

        fetchHomeContent()
    }

    /** Find a track in our internal queue by ID */
    private fun findTrackInQueue(trackId: String): Track? {
        return _playbackQueue.value.find { it.id == trackId }
    }

    fun setTab(tab: HomeTab) {
        _currentTab.value = tab
    }

    fun fetchHomeContent(params: String? = null) {
        currentHomeParams = params
        if (params == null) currentHomeMoodName = null
        refreshHomeContent(forceFreshSeed = false, isPullToRefresh = false)
    }

    fun refreshHomeContent(forceFreshSeed: Boolean = true, isPullToRefresh: Boolean = true) {
        val params = currentHomeParams
        val mood = currentHomeMoodName
        val country = _selectedHomeCountry.value
        homeFeedJob?.cancel()
        if (isPullToRefresh) {
            _isHomeRefreshing.value = true
        }
        if (_homeSections.value.isEmpty()) {
            _isHomeLoading.value = true
        }
        homeRefreshCycle++
        val cycle = homeRefreshCycle

        homeFeedJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val page1Deferred = async {
                    repository.getHomeFeedPage(
                        params = params,
                        country = country,
                        resetVisitorData = forceFreshSeed
                    )
                }
                val moodsDeferred = if (params == null && (_homeMoodsData.value.moodsAndMoments.isEmpty() || forceFreshSeed)) {
                    async { repository.getMoodsAndGenres() }
                } else null
                val newReleasesDeferred = if (params == null) {
                    async { repository.getNewReleasesSections(country) }
                } else null

                var page = page1Deferred.await()
                val collectedSections = page.sections.toMutableList()
                var continuationToken = page.continuation

                // Auto-fetch 1 continuation page if initial page only returned a few shelves (so Home is rich & layered like SimpMusic)
                if (!continuationToken.isNullOrBlank() && collectedSections.size < 4) {
                    val page2 = repository.getHomeFeedPage(
                        continuation = continuationToken,
                        country = country
                    )
                    val seenTitles = collectedSections.mapTo(mutableSetOf()) { it.title.lowercase() }
                    for (sec in page2.sections) {
                        if (seenTitles.add(sec.title.lowercase())) {
                            collectedSections.add(sec)
                        }
                    }
                    continuationToken = page2.continuation
                }

                // Append New Releases shelves cleanly in "All" mode (skip duplicate/single-card repeats)
                if (newReleasesDeferred != null) {
                    val newRel = newReleasesDeferred.await()
                    val seenTitles = collectedSections.mapTo(mutableSetOf()) { it.title.lowercase() }
                    val seenItemIds = collectedSections.flatMap { it.items }.mapTo(mutableSetOf()) { it.id }
                    for (sec in newRel) {
                        val hasFreshItems = sec.items.any { it.id !in seenItemIds }
                        if (hasFreshItems && sec.items.size >= 2 && seenTitles.add(sec.title.lowercase())) {
                            collectedSections.add(sec)
                            sec.items.forEach { seenItemIds.add(it.id) }
                        }
                    }
                }

                if (collectedSections.isNotEmpty()) {
                    _homeSections.value = collectedSections
                    _homeContinuation.value = continuationToken
                    page.backgroundUrl?.let { _homeBackgroundUrl.value = it }

                    val heroThumb = collectedSections.firstOrNull { it.items.isNotEmpty() }?.items?.firstOrNull()?.thumbnailUrl
                    if (!heroThumb.isNullOrBlank()) {
                        extractHomeHeroColor(heroThumb)
                    }
                }

                if (moodsDeferred != null) {
                    val moods = moodsDeferred.await()
                    if (moods.moodsAndMoments.isNotEmpty() || moods.genres.isNotEmpty()) {
                        _homeMoodsData.value = moods
                    }
                }

                // Refresh Quick Picks + Music Videos dynamically so pull-to-refresh yields varied fresh music
                if (params == null) {
                    refreshQuickPicksFromHomeOrHistory(collectedSections, cycle, forceFreshSeed)
                    refreshTrendingVideosFromHomeOrCharts(collectedSections, country, cycle, forceFreshSeed)
                    fetchMoodRecommendations(mood)
                } else {
                    fetchMoodRecommendations(mood)
                }
            } catch (e: Exception) {
                Log.e(TAG, "refreshHomeContent failed", e)
            } finally {
                _isHomeLoading.value = false
                _isHomeRefreshing.value = false
            }
        }
    }

    fun loadMoreHomeContent() {
        val token = _homeContinuation.value
        if (token.isNullOrBlank() || _isHomePaginating.value || _isHomeRefreshing.value) return

        _isHomePaginating.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val nextPage = repository.getHomeFeedPage(
                    continuation = token,
                    country = _selectedHomeCountry.value
                )
                if (nextPage.sections.isNotEmpty()) {
                    val existing = _homeSections.value
                    val seenTitles = existing.mapTo(mutableSetOf()) { it.title.lowercase() }
                    val uniqueNew = nextPage.sections.filter { seenTitles.add(it.title.lowercase()) }
                    if (uniqueNew.isNotEmpty()) {
                        _homeSections.value = existing + uniqueNew
                    }
                }
                _homeContinuation.value = nextPage.continuation
            } catch (e: Exception) {
                Log.e(TAG, "loadMoreHomeContent failed", e)
            } finally {
                _isHomePaginating.value = false
            }
        }
    }

    private suspend fun refreshQuickPicksFromHomeOrHistory(
        sections: List<com.example.ecodot.data.remote.HomeSection>,
        cycle: Int,
        forceFresh: Boolean
    ) {
        val qpShelf = sections.firstOrNull {
            it.title.contains("Quick pick", ignoreCase = true) ||
                it.title.contains("Trending song", ignoreCase = true) ||
                (it.items.count { item -> item.type == com.example.ecodot.data.remote.MediaType.TRACK } >= 6)
        }
        val liveShelfTracks = qpShelf?.items
            ?.filter { it.type == com.example.ecodot.data.remote.MediaType.TRACK && it.aspectRatio == 1f }
            ?.map { item ->
                Track(
                    id = item.id,
                    title = item.title,
                    artist = item.subtitle.ifBlank { "YouTube Music" },
                    album = "",
                    albumArtUri = item.thumbnailUrl,
                    duration = 0,
                    path = "https://www.youtube.com/watch?v=${item.id}",
                    isYouTube = true,
                    artistId = item.artistId,
                    isExplicit = item.isExplicit
                )
            }
            .orEmpty()

        if (liveShelfTracks.size >= 8 && !forceFresh) {
            _suggestedTracks.value = liveShelfTracks.take(20)
            return
        }

        // Personalized seed rotation on refresh so Quick Picks always delivers varied YouTube Music tracks
        val recent = runCatching { repository.recentlyPlayed.first() }.getOrDefault(emptyList())
        val seedTrack = recent.takeIf { it.isNotEmpty() }?.let { list ->
            list[cycle % list.size]
        } ?: liveShelfTracks.firstOrNull() ?: _suggestedTracks.value.getOrNull(cycle % _suggestedTracks.value.size.coerceAtLeast(1))

        val relatedTracks = if (seedTrack != null) {
            runCatching { repository.getYouTubeRelatedTracks(seedTrack.id) }.getOrDefault(emptyList())
        } else emptyList()

        val refreshQueries = listOf(
            "Top Global Hits 2026",
            "Trending Viral Music Songs",
            "Today's Biggest Hits Pop R&B",
            "Bollywood & Punjabi Chartbusters"
        )
        val extraSearch = if (forceFresh || relatedTracks.size < 8) {
            runCatching { repository.searchYouTube(refreshQueries[cycle % refreshQueries.size]) }.getOrDefault(emptyList())
        } else emptyList()

        val merged = (liveShelfTracks + relatedTracks + extraSearch)
            .distinctBy { it.id }
            .let { list -> if (forceFresh && list.size > 1) list.shuffled() else list }

        if (merged.isNotEmpty()) {
            _suggestedTracks.value = merged.take(20)
        }
    }

    private suspend fun refreshTrendingVideosFromHomeOrCharts(
        sections: List<com.example.ecodot.data.remote.HomeSection>,
        country: String,
        cycle: Int,
        forceFresh: Boolean
    ) {
        val homeVideos = sections.flatMap { it.items }
            .filter { it.type == com.example.ecodot.data.remote.MediaType.TRACK && it.aspectRatio > 1.2f }
        val queries = listOf(
            "$country Trending Music Videos",
            "Top Official Music Videos 2026",
            "New Trending Music Videos"
        )
        val fetched = if (forceFresh || homeVideos.size < 4) {
            runCatching { repository.searchYouTubeCategorized(queries[cycle % queries.size], "Videos") }
                .getOrNull()?.songs?.map { track ->
                    com.example.ecodot.data.remote.HomeMediaItem(
                        id = track.id,
                        title = track.title,
                        subtitle = "${track.artist} • Music Video",
                        thumbnailUrl = track.albumArtUri ?: "https://i.ytimg.com/vi/${track.id}/hqdefault.jpg",
                        type = com.example.ecodot.data.remote.MediaType.TRACK,
                        aspectRatio = 16f / 9f,
                        isExplicit = track.isExplicit
                    )
                }.orEmpty()
        } else emptyList()

        val combined = (homeVideos + fetched + _countryVideoCharts.value)
            .distinctBy { it.id }
            .let { list -> if (forceFresh && list.size > 2) list.shuffled() else list }

        if (combined.isNotEmpty()) {
            _realMusicVideos.value = combined.take(16)
        }
    }

    private fun extractHomeHeroColor(imageUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val loader = coil.Coil.imageLoader(context)
                val req = coil.request.ImageRequest.Builder(context)
                    .data(imageUrl)
                    .size(128, 128)
                    .allowHardware(false)
                    .build()
                val res = loader.execute(req)
                val bitmap = (res.drawable as? BitmapDrawable)?.bitmap ?: return@launch
                val palette = Palette.from(bitmap).generate()
                val rgb = palette.getVibrantColor(0).takeIf { it != 0 }
                    ?: palette.getDarkVibrantColor(0).takeIf { it != 0 }
                    ?: palette.getMutedColor(0).takeIf { it != 0 }
                    ?: palette.getDominantColor(0)
                if (rgb != 0) {
                    _homeHeroColor.value = Color(rgb)
                }
            } catch (_: Exception) {
            }
        }
    }

    fun openBrowseCategory(
        title: String,
        browseId: String?,
        params: String?,
        fallbackQuery: String? = null,
        moodFilterName: String? = null
    ) {
        _browseSheetState.value = BrowseSheetUiState(
            isVisible = true,
            title = title,
            isLoading = true,
            sections = emptyList(),
            moodFilterName = moodFilterName
        )
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val remoteSections = if (!browseId.isNullOrBlank()) {
                    repository.browseCategorySections(browseId, params)
                } else emptyList()

                if (remoteSections.isNotEmpty()) {
                    _browseSheetState.value = _browseSheetState.value.copy(
                        isLoading = false,
                        sections = remoteSections
                    )
                } else {
                    val query = fallbackQuery ?: title
                    val res = repository.searchYouTubeCategorized(query, "All")
                    val builtSections = mutableListOf<com.example.ecodot.data.remote.HomeSection>()
                    if (res.songs.isNotEmpty()) {
                        builtSections.add(
                            com.example.ecodot.data.remote.HomeSection(
                                title = "Top Songs",
                                items = res.songs.map { t ->
                                    com.example.ecodot.data.remote.HomeMediaItem(
                                        id = t.id,
                                        title = t.title,
                                        subtitle = t.artist,
                                        thumbnailUrl = t.albumArtUri ?: "https://i.ytimg.com/vi/${t.id}/hqdefault.jpg",
                                        type = com.example.ecodot.data.remote.MediaType.TRACK,
                                        isExplicit = t.isExplicit,
                                        artistId = t.artistId
                                    )
                                }
                            )
                        )
                    }
                    if (res.albums.isNotEmpty()) {
                        builtSections.add(
                            com.example.ecodot.data.remote.HomeSection(
                                title = "Albums",
                                items = res.albums.mapNotNull { al ->
                                    val aid = al.id ?: return@mapNotNull null
                                    com.example.ecodot.data.remote.HomeMediaItem(
                                        id = aid,
                                        title = al.title,
                                        subtitle = al.artist,
                                        thumbnailUrl = al.artUri ?: "",
                                        type = com.example.ecodot.data.remote.MediaType.ALBUM
                                    )
                                }
                            )
                        )
                    }
                    if (res.videos.isNotEmpty()) {
                        builtSections.add(
                            com.example.ecodot.data.remote.HomeSection(
                                title = "Videos",
                                items = res.videos.map { v ->
                                    com.example.ecodot.data.remote.HomeMediaItem(
                                        id = v.id,
                                        title = v.title,
                                        subtitle = v.artist,
                                        thumbnailUrl = v.albumArtUri ?: "https://i.ytimg.com/vi/${v.id}/hqdefault.jpg",
                                        type = com.example.ecodot.data.remote.MediaType.TRACK,
                                        aspectRatio = 16f / 9f
                                    )
                                }
                            )
                        )
                    }
                    _browseSheetState.value = _browseSheetState.value.copy(
                        isLoading = false,
                        sections = builtSections
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "openBrowseCategory failed for $title", e)
                _browseSheetState.value = _browseSheetState.value.copy(isLoading = false)
            }
        }
    }

    fun dismissBrowseSheet() {
        _browseSheetState.value = _browseSheetState.value.copy(isVisible = false)
    }

    fun openBrowsePage(
        title: String,
        strapline: String? = null,
        browseId: String? = null,
        params: String? = null,
        initialItems: List<com.example.ecodot.data.remote.HomeMediaItem> = emptyList(),
        fallbackQuery: String? = null,
        moodFilterName: String? = null
    ) {
        val seedSongs = initialItems
            .filter { it.type == com.example.ecodot.data.remote.MediaType.TRACK && it.aspectRatio <= 1.2f }
            .map { item ->
                Track(
                    id = item.id,
                    title = item.title,
                    artist = item.subtitle.split("•").firstOrNull()?.trim().orEmpty().ifBlank { "YouTube Music" },
                    album = "",
                    albumArtUri = item.thumbnailUrl,
                    duration = 0,
                    path = "https://www.youtube.com/watch?v=${item.id}",
                    isYouTube = true,
                    artistId = item.artistId,
                    isExplicit = item.isExplicit
                )
            }
        val seedVideos = initialItems.filter {
            it.type == com.example.ecodot.data.remote.MediaType.TRACK && it.aspectRatio > 1.2f
        }
        val seedAlbums = initialItems.filter { it.type == com.example.ecodot.data.remote.MediaType.ALBUM }.map { item ->
            Album(id = item.id, title = item.title, artist = item.subtitle.ifBlank { "Album" }, artUri = item.thumbnailUrl, tracks = emptyList())
        }
        val seedPlaylists = initialItems.filter { it.type == com.example.ecodot.data.remote.MediaType.PLAYLIST }
        val seedArtists = initialItems.filter { it.type == com.example.ecodot.data.remote.MediaType.ARTIST }.map { item ->
            Artist(id = item.id, name = item.title, imageUrl = item.thumbnailUrl)
        }

        _browsePageState.value = BrowsePageUiState(
            title = title,
            strapline = strapline,
            isLoading = true,
            songs = seedSongs,
            albums = seedAlbums,
            playlists = seedPlaylists,
            videos = seedVideos,
            artists = seedArtists,
            moodFilterName = moodFilterName,
            browseId = browseId,
            params = params,
            fallbackQuery = fallbackQuery ?: title
        )

        browsePageJob?.cancel()
        browsePageJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val query = fallbackQuery ?: title
                val remoteDeferred = if (!browseId.isNullOrBlank()) {
                    async { repository.browseCategorySections(browseId, params) }
                } else null
                val searchDeferred = async { repository.searchYouTubeCategorized(query, "All") }
                val playlistsDeferred = async { repository.searchYouTubePlaylists(query) }

                val remoteItems = remoteDeferred?.await()?.flatMap { it.items }.orEmpty()
                val searchRes = searchDeferred.await()
                val fetchedPlaylists = playlistsDeferred.await()

                val remoteSongs = remoteItems
                    .filter { it.type == com.example.ecodot.data.remote.MediaType.TRACK && it.aspectRatio <= 1.2f }
                    .map { item ->
                        Track(
                            id = item.id,
                            title = item.title,
                            artist = item.subtitle.split("•").firstOrNull()?.trim().orEmpty().ifBlank { "YouTube Music" },
                            album = "",
                            albumArtUri = item.thumbnailUrl,
                            duration = 0,
                            path = "https://www.youtube.com/watch?v=${item.id}",
                            isYouTube = true,
                            artistId = item.artistId,
                            isExplicit = item.isExplicit
                        )
                    }
                val remoteVideos = remoteItems.filter {
                    it.type == com.example.ecodot.data.remote.MediaType.TRACK && it.aspectRatio > 1.2f
                }
                val remoteAlbums = remoteItems
                    .filter { it.type == com.example.ecodot.data.remote.MediaType.ALBUM }
                    .map { Album(id = it.id, title = it.title, artist = it.subtitle.ifBlank { "Album" }, artUri = it.thumbnailUrl, tracks = emptyList()) }
                val remotePlaylists = remoteItems.filter { it.type == com.example.ecodot.data.remote.MediaType.PLAYLIST }
                val remoteArtists = remoteItems
                    .filter { it.type == com.example.ecodot.data.remote.MediaType.ARTIST }
                    .map { Artist(id = it.id, name = it.title, imageUrl = it.thumbnailUrl) }

                val searchVideoItems = searchRes.videos.map { v ->
                    com.example.ecodot.data.remote.HomeMediaItem(
                        id = v.id,
                        title = v.title,
                        subtitle = v.artist,
                        thumbnailUrl = v.albumArtUri ?: "https://i.ytimg.com/vi/${v.id}/hqdefault.jpg",
                        type = com.example.ecodot.data.remote.MediaType.TRACK,
                        aspectRatio = 16f / 9f,
                        isExplicit = v.isExplicit,
                        artistId = v.artistId
                    )
                }

                _browsePageState.value = _browsePageState.value.copy(
                    isLoading = false,
                    songs = (seedSongs + remoteSongs + searchRes.songs).distinctBy { it.id }.take(25),
                    albums = (seedAlbums + remoteAlbums + searchRes.albums).distinctBy { it.id ?: it.title }.take(20),
                    playlists = (seedPlaylists + remotePlaylists + fetchedPlaylists).distinctBy { it.id }.take(20),
                    videos = (seedVideos + remoteVideos + searchVideoItems).distinctBy { it.id }.take(16),
                    artists = (seedArtists + remoteArtists + searchRes.artists).distinctBy { it.id.ifBlank { it.name } }.take(14)
                )
            } catch (e: Exception) {
                Log.e(TAG, "openBrowsePage failed for $title", e)
                _browsePageState.value = _browsePageState.value.copy(isLoading = false)
            }
        }
    }

    fun refreshBrowsePage() {
        val current = _browsePageState.value
        if (current.title.isBlank()) return
        openBrowsePage(
            title = current.title,
            strapline = current.strapline,
            browseId = current.browseId,
            params = current.params,
            fallbackQuery = current.fallbackQuery,
            moodFilterName = current.moodFilterName
        )
    }

    fun setHomeMood(mood: String?) {
        currentHomeMoodName = mood
        val params = when (mood) {
            "Relax" -> "ggM8SgQIBxADSgQIBRABSgQICRABSgQIChABSgQIDRABSgQICBABSgQIBBABSgQIDhABSgQIAxABSgQIBhAB"
            "Sleep" -> "ggM8SgQIBxABSgQIBRADSgQICRABSgQIChABSgQIDRABSgQICBABSgQIBBABSgQIDhABSgQIAxABSgQIBhAB"
            "Energize" -> "ggM8SgQIBxABSgQIBRABSgQICRADSgQIChABSgQIDRABSgQICBABSgQIBBABSgQIDhABSgQIAxABSgQIBhAB"
            "Sad" -> "ggM8SgQIBxABSgQIBRABSgQICRABSgQIChADSgQIDRABSgQICBABSgQIBBABSgQIDhABSgQIAxABSgQIBhAB"
            "Romance" -> "ggM8SgQIBxABSgQIBRABSgQICRABSgQIChABSgQIDRADSgQICBABSgQIBBABSgQIDhABSgQIAxABSgQIBhAB"
            "Feel good", "Feel Good" -> "ggM8SgQIBxABSgQIBRABSgQICRABSgQIChABSgQIDRABSgQICBADSgQIBBABSgQIDhABSgQIAxABSgQIBhAB"
            "Workout" -> "ggM8SgQIBxABSgQIBRABSgQICRABSgQIChABSgQIDRABSgQICBABSgQIBBADSgQIDhABSgQIAxABSgQIBhAB"
            "Party" -> "ggM8SgQIBxABSgQIBRABSgQICRABSgQIChABSgQIDRABSgQICBABSgQIBBABSgQIDhADSgQIAxABSgQIBhAB"
            "Commute" -> "ggM8SgQIBxABSgQIBRABSgQICRABSgQIChABSgQIDRABSgQICBABSgQIBBABSgQIDhABSgQIAxADSgQIBhAB"
            "Focus" -> "ggM8SgQIBxABSgQIBRABSgQICRABSgQIChABSgQIDRABSgQICBABSgQIBBABSgQIDhABSgQIAxABSgQIBhAD"
            else -> null
        }
        currentHomeParams = params
        refreshHomeContent(forceFreshSeed = false, isPullToRefresh = false)
    }

    fun fetchMoodRecommendations(moodOverride: String? = null) {
        viewModelScope.launch {
            val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            val mood = moodOverride ?: when (hour) {
                in 5..10 -> "Chill"      // 5 AM to 10:59 AM
                in 11..16 -> "Upbeat"    // 11 AM to 4:59 PM
                in 17..20 -> "Focus"     // 5 PM to 8:59 PM
                else -> "Chill"          // 9 PM to 4:59 AM
            }
            val greeting = if (moodOverride != null) {
                "$moodOverride Mix"
            } else when (hour) {
                in 5..10 -> "Good Morning • Chill Mix"
                in 11..16 -> "Good Afternoon • Upbeat Hype"
                in 17..20 -> "Good Evening • Focus Beats"
                else -> "Good Night • Midnight Chill"
            }
            _moodGreeting.value = greeting

            val keywords = when (mood) {
                "Chill", "Relax", "Sleep" -> listOf("chill", "lofi", "lo-fi", "sleep", "relax", "acoustic", "calm", "slow", "ambient", "soft", "jazz", "piano", "soul")
                "Focus" -> listOf("focus", "study", "beats", "instrumental", "classical", "synthwave", "coding", "ambient", "calm", "relax")
                "Upbeat", "Workout", "Energize", "Party" -> listOf("upbeat", "dance", "rock", "pop", "workout", "energetic", "gym", "hype", "electronic", "electro", "party", "rap", "hip hop", "techno")
                "Romance", "Feel Good" -> listOf("love", "romance", "acoustic", "pop", "sweet", "feel good", "happy")
                "Sad" -> listOf("sad", "slow", "melancholy", "acoustic", "piano", "cry", "heartbreak")
                else -> listOf("chill", "lofi", "pop")
            }

            try {
                val localTracks = repository.allTracks.first()
                val localMatches = localTracks.filter { track ->
                    keywords.any { kw ->
                        track.title.contains(kw, ignoreCase = true) ||
                        track.artist.contains(kw, ignoreCase = true) ||
                        track.album.contains(kw, ignoreCase = true) ||
                        (track.genre != null && track.genre.contains(kw, ignoreCase = true))
                    }
                }
                if (localMatches.size >= 5) {
                    _moodRecommendedTracks.value = localMatches.shuffled().take(10)
                } else {
                    // Fallback/Supplement: Search YouTube for mood
                    val ytQuery = when (mood) {
                        "Chill", "Relax", "Sleep" -> "lofi chill beats"
                        "Focus" -> "synthwave focus study"
                        "Upbeat", "Workout", "Energize", "Party" -> "upbeat workout energetic pop"
                        "Romance", "Feel Good" -> "love romantic acoustic songs"
                        "Sad" -> "sad slow emotional songs"
                        else -> "chill music"
                    }
                    val ytResults = repository.searchYouTube(ytQuery)
                    val combined = (localMatches + ytResults).distinctBy { it.id }.shuffled()
                    _moodRecommendedTracks.value = combined.take(10)
                }
            } catch (e: Exception) {
                Log.e("MusicViewModel", "Failed to fetch mood recommendations", e)
            }
        }
    }

    fun startPositionUpdater() {
        positionUpdaterJob?.cancel()
        positionUpdaterJob = viewModelScope.launch {
            while (_isPlaying.value) {
                _controller.value?.let { controller ->
                    _position.value = controller.currentPosition
                    val dur = controller.duration
                    _duration.value = if (dur != C.TIME_UNSET) dur else 0L
                    _isCrossfading.value = controller.sessionExtras.getBoolean(
                        EcoDotSessionService.EXTRA_IS_CROSSFADING,
                        false
                    )
                }
                delay(250) // Reduced from 100ms — still smooth seekbar, 60% less CPU
            }
        }
    }

    fun stopPositionUpdater() {
        positionUpdaterJob?.cancel()
        positionUpdaterJob = null
    }

    private fun checkAndFetchRelatedTracks(videoId: String) {
        val queue = _playbackQueue.value
        val currentIdx = queue.indexOfFirst { it.id == videoId }
        val remaining = queue.size - currentIdx - 1
        // If we are within 7 tracks of the end, fetch more
        if (remaining <= 7) {
            fetchRelatedJob?.cancel()
            fetchRelatedJob = viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    Log.d(TAG, "Fetching related tracks for $videoId (remaining=$remaining)")
                    val related = repository.getYouTubeRelatedTracks(videoId)
                    
                    if (!isActive) return@launch
                    
                    val currentQueue = _playbackQueue.value
                    val existingIds = currentQueue.map { it.id }.toSet()
                    val newTracks = related.filter { it.id !in existingIds }
                    
                    if (newTracks.isNotEmpty()) {
                        repository.saveTracks(newTracks)
                        val updatedQueue = currentQueue.toMutableList()
                        val tracksToAdd = newTracks.take(20)
                        autoGeneratedTrackIds.addAll(tracksToAdd.map { it.id })
                        updatedQueue.addAll(tracksToAdd)
                        
                        setQueue(updatedQueue)
                        _originalPlaybackQueue.value = _originalPlaybackQueue.value + tracksToAdd
                        
                        runOnMain {
                            _controller.value?.addMediaItems(tracksToAdd.map { buildMediaItem(it) })
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to fetch related tracks", e)
                }
            }
        }
    }

    private fun runOnMain(action: () -> Unit) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Main) { action() }
    }

    fun toggleEndlessMode() {
        _endlessMode.value = !_endlessMode.value
    }

    private fun updateDuration() {
        _controller.value?.let {
            val dur = it.duration
            _duration.value = if (dur != C.TIME_UNSET) dur else 0L
        }
    }

    fun seekTo(position: Long) {
        _controller.value?.seekTo(position)
        _position.value = position
    }

    fun seekRelative(deltaMs: Long) {
        val controller = _controller.value ?: return
        val dur = controller.duration.takeIf { it != androidx.media3.common.C.TIME_UNSET && it > 0L }
            ?: _duration.value.coerceAtLeast(1L)
        val cur = controller.currentPosition.coerceAtLeast(0L)
        val target = (cur + deltaMs).coerceIn(0L, dur)
        seekTo(target)
    }

    fun toggleShuffle() {
        val controller = _controller.value ?: return
        val newValue = !_shuffleMode.value
        _shuffleMode.value = newValue
        
        val queue = _playbackQueue.value.toMutableList()
        val currentIdx = _currentQueueIndex.value
        
        if (queue.isNotEmpty()) {
            val newQueue = mutableListOf<Track>()
            val currentTrack = if (currentIdx in 0 until queue.size) queue[currentIdx] else queue.firstOrNull() ?: return
            val safeIdx = if (currentIdx in 0 until queue.size) currentIdx else 0

            if (newValue) {
                // Shuffle: Keep history, shuffle upcoming
                val played = queue.subList(0, safeIdx + 1)
                val upcoming = queue.subList(safeIdx + 1, queue.size).toMutableList()
                upcoming.shuffle()
                newQueue.addAll(played)
                newQueue.addAll(upcoming)
            } else {
                // Restore: Keep history, restore upcoming from original
                val original = _originalPlaybackQueue.value
                val currentOriginalIdx = original.indexOfFirst { it.id == currentTrack.id }
                
                newQueue.addAll(queue.subList(0, safeIdx + 1))
                if (currentOriginalIdx != -1) {
                    val remainingOriginal = original.subList(currentOriginalIdx + 1, original.size)
                    val historyIds = newQueue.map { it.id }.toSet()
                    newQueue.addAll(remainingOriginal.filter { it.id !in historyIds })
                }
            }
            
            if (newQueue.isNotEmpty()) {
                setQueue(newQueue)
                val nextMediaItems = newQueue.subList(safeIdx + 1, newQueue.size).map { buildMediaItem(it) }
                val totalItems = controller.mediaItemCount
                if (totalItems > safeIdx + 1) {
                    controller.removeMediaItems(safeIdx + 1, totalItems)
                }
                controller.addMediaItems(nextMediaItems)
            }
        }
    }


    fun cycleRepeatMode() {
        val nextMode = when (_repeatMode.value) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
            Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
            else -> Player.REPEAT_MODE_OFF
        }
        _controller.value?.repeatMode = nextMode
        _repeatMode.value = nextMode
    }

    val lyricsMemoryCache = java.util.concurrent.ConcurrentHashMap<String, Pair<LrcLibResponse?, List<LyricLine>>>()
    @Volatile private var currentLyricsTrackId: String? = null
    private var lyricsJob: kotlinx.coroutines.Job? = null

    fun fetchLyricsManual(track: Track) {
        fetchLyrics(track, forceRefresh = true)
    }

    private fun fetchLyrics(track: Track, forceRefresh: Boolean = false) {
        val trackId = track.id
        if (trackId.isBlank()) return

        // 1. If already active for this track and we already have a cached or loaded entry, avoid duplicate work
        if (!forceRefresh && currentLyricsTrackId == trackId && (_lyrics.value != null || lyricsMemoryCache.containsKey(trackId))) {
            val cached = lyricsMemoryCache[trackId]
            if (cached != null) {
                _lyrics.value = cached.first
                _syncedLyrics.value = cached.second
                _isLyricsLoading.value = false
            }
            return
        }

        currentLyricsTrackId = trackId

        // 2. Check instant memory cache: if present, apply synchronously without resetting or loading spinner!
        val cached = lyricsMemoryCache[trackId]
        if (cached != null && !forceRefresh) {
            _lyrics.value = cached.first
            _syncedLyrics.value = cached.second
            _isLyricsLoading.value = false
            return
        }

        // 3. For new tracks, start loading cleanly
        lyricsJob?.cancel()
        lyricsJob = viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _isLyricsLoading.value = true
                _lyrics.value = null
                _syncedLyrics.value = emptyList()
            }

            try {
                // Prioritize SimpMusic API by passing track.id (videoId)
                val response = lyricsRepository.fetchLyrics(track.id, track.artist, track.title, track.duration)
                val parsedLines = response?.syncedLyrics?.let { parseLrc(it) } ?: emptyList()
                
                // Cache result (even if null, to remember this track has no lyrics)
                lyricsMemoryCache[trackId] = Pair(response, parsedLines)

                withContext(Dispatchers.Main) {
                    if (currentLyricsTrackId == trackId) {
                        _lyrics.value = response
                        _syncedLyrics.value = parsedLines
                        _isLyricsLoading.value = false
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Lyrics fetch error for ${track.title}: ${e.message}")
                lyricsMemoryCache[trackId] = Pair(null, emptyList())
                withContext(Dispatchers.Main) {
                    if (currentLyricsTrackId == trackId) {
                        _isLyricsLoading.value = false
                    }
                }
            }
        }
    }

    private fun parseLrc(lrcContent: String): List<LyricLine> =
        com.example.ecodot.data.remote.LyricsRepository.parseLrcLines(lrcContent)


    private fun cleanOrphanedDownloads() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>().applicationContext
                val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) ?: return@launch
                val files = downloadDir.listFiles() ?: return@launch
                val dbTracks = database.trackDao().getAllTracks().first().map { it.id }.toSet()
                files.forEach { file ->
                    if (file.isFile && file.name.endsWith(".m4a")) {
                        val trackId = file.name.substringBefore(".m4a")
                        if (trackId !in dbTracks) {
                            Log.d(TAG, "Deleting orphaned download file: ${file.name}")
                            file.delete()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cleaning orphaned downloads", e)
            }
        }
    }

    fun clearPlaybackHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    private var lastSearchQuery: String? = null
    private var lastSearchType: String? = null
    private var searchJob: Job? = null
    private var suggestionsJob: Job? = null

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()

    fun ensureMoodsLoaded() {
        val current = _homeMoodsData.value
        if (current.moodsAndMoments.isNotEmpty() || current.genres.isNotEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val loaded = repository.getMoodsAndGenres()
                if (loaded.moodsAndMoments.isNotEmpty() || loaded.genres.isNotEmpty()) {
                    _homeMoodsData.value = loaded
                }
            } catch (e: Exception) {
                Log.w(TAG, "ensureMoodsLoaded failed", e)
            }
        }
    }

    fun loadMoodCategoryArtwork(item: com.example.ecodot.data.remote.MoodCategoryItem) {
        if (item.params.isBlank()) return
        if (!_moodCategoryArtwork.value[item.params].isNullOrBlank()) return
        if (!requestedMoodArtwork.add(item.params)) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = repository.getMoodCategoryArtwork(item.browseId, item.params)
                if (!url.isNullOrBlank()) {
                    _moodCategoryArtwork.value = _moodCategoryArtwork.value + (item.params to url)
                }
            } catch (e: Exception) {
                Log.w(TAG, "loadMoodCategoryArtwork failed for ${item.title}", e)
            }
        }
    }

    fun openChartBrowsePage() {
        val country = _selectedHomeCountry.value
        val existingVideos = _countryVideoCharts.value
        val existingArtists = _topChartArtists.value.map { (title, _, img) ->
            val chId = _topArtistChannelIds.value[title] ?: ""
            Artist(id = chId, name = title, imageUrl = img)
        }
        val existingPodcasts = _chartPodcasts.value.map { p ->
            com.example.ecodot.data.remote.HomeMediaItem(
                id = p.browseId,
                title = p.title,
                subtitle = p.host,
                thumbnailUrl = p.thumbnailUrl,
                type = com.example.ecodot.data.remote.MediaType.PLAYLIST
            )
        }.filter { it.id.isNotBlank() }

        _browsePageState.value = BrowsePageUiState(
            title = "$country Top Charts",
            strapline = "SIMPMUSIC & YOUTUBE MUSIC CHARTS",
            isLoading = true,
            playlists = existingPodcasts,
            videos = existingVideos,
            artists = existingArtists,
            browseId = "FEmusic_charts",
            fallbackQuery = "$country Top 100 Songs"
        )

        browsePageJob?.cancel()
        browsePageJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val chartsDeferred = async { repository.getChartsForCountry(country) }
                val songsDeferred = async { repository.searchYouTubeCategorized("$country top charts hits", "All") }
                val chartData = chartsDeferred.await()
                val searchRes = songsDeferred.await()

                val chartArtists = chartData.topArtists.map {
                    Artist(id = it.browseId, name = it.title, imageUrl = it.thumbnailUrl)
                }
                _browsePageState.value = _browsePageState.value.copy(
                    isLoading = false,
                    songs = searchRes.songs.take(25),
                    albums = searchRes.albums.take(20),
                    playlists = (chartData.chartPlaylists + searchRes.playlists).distinctBy { it.id }.take(20),
                    videos = (chartData.videoCharts + existingVideos).distinctBy { it.id }.take(20),
                    artists = (chartArtists + existingArtists + searchRes.artists).distinctBy { it.id.ifBlank { it.name } }.take(16)
                )
            } catch (e: Exception) {
                Log.e(TAG, "openChartBrowsePage failed", e)
                _browsePageState.value = _browsePageState.value.copy(isLoading = false)
            }
        }
    }

    fun performSearch(
        query: String,
        type: String? = null,
        saveToHistory: Boolean = false,
        forceRefresh: Boolean = false
    ) {
        if (query.isBlank()) {
            clearSearch()
            return
        }

        if (!forceRefresh && lastSearchQuery == query && lastSearchType == type &&
            (_searchResults.value.isNotEmpty() || _searchAlbums.value.isNotEmpty() || _searchArtists.value.isNotEmpty() ||
                _searchPlaylists.value.isNotEmpty() || _searchFeaturedPlaylists.value.isNotEmpty() ||
                _searchPodcasts.value.isNotEmpty() || _isSearching.value)) {
            // Unchanged query, skip redundant call to avoid hitting YouTube Music rate limits or cancelling active job
            return
        }
        lastSearchQuery = query
        lastSearchType = type
        
        // Add to Room history only when explicitly requested (e.g. user pressed enter or tapped chip)
        if (saveToHistory) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.addRecentSearch(query, "Query", null)
            }
        }

        _isSearching.value = true
        _searchResults.value = emptyList()
        _searchVideos.value = emptyList()
        _searchAlbums.value = emptyList()
        _searchArtists.value = emptyList()
        _searchPlaylists.value = emptyList()
        _searchFeaturedPlaylists.value = emptyList()
        _searchPodcasts.value = emptyList()
        _searchError.value = null
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            try {
                // Fetch local database matches and YouTube results in parallel
                val localDeferred = async(Dispatchers.IO) {
                    repository.searchTracksLocally("%$query%")
                }
                val remoteDeferred = async(Dispatchers.IO) {
                    repository.searchYouTubeCategorized(query, type)
                }

                val localMatches = localDeferred.await()
                val remoteResults = remoteDeferred.await()

                if (coroutineContext[Job] == searchJob) {
                    val localIds = localMatches.map { it.id }.toSet()
                    
                    // Filter out remote results that are already present in the local database
                    val uniqueRemoteSongs = remoteResults.songs.filter { it.id !in localIds }
                    
                    // Combine them, putting local matches (already liked/downloaded/played) at the top of the search results list
                    val combinedSongs = (localMatches + uniqueRemoteSongs).distinctBy { it.id }

                    _searchResults.value = combinedSongs
                    _searchVideos.value = remoteResults.videos
                    _searchAlbums.value = remoteResults.albums
                    _searchArtists.value = remoteResults.artists
                    _searchPlaylists.value = remoteResults.playlists
                    _searchFeaturedPlaylists.value = remoteResults.featuredPlaylists
                    _searchPodcasts.value = remoteResults.podcasts
                }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    Log.e(TAG, "Search failed for query: $query", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(getApplication(), "Search failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                    if (coroutineContext[Job] == searchJob) {
                        _searchError.value = "Network error or timeout. Please try again."
                    }
                }
            } finally {
                if (coroutineContext[Job] == searchJob) {
                    _isSearching.value = false
                }
            }
        }
    }

    fun updateSuggestions(query: String) {
        val trimmed = query.trim()
        if (trimmed.length < 2) {
            _searchSuggestions.value = emptyList()
            _instantSearchItems.value = emptyList()
            return
        }
        suggestionsJob?.cancel()
        suggestionsJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val data = repository.getSearchSuggestionsAndInstantMatches(trimmed)
                _searchSuggestions.value = data.suggestions
                if (data.instantItems.isNotEmpty()) {
                    _instantSearchItems.value = data.instantItems
                } else {
                    val local = repository.searchTracksLocally("%$trimmed%")
                    val remote = repository.searchYouTubeInstantMatches(trimmed)
                    val songs = (local + remote.songs).distinctBy { it.id }.take(5)
                    val artists = remote.artists.take(2)
                    val albums = remote.albums.take(2)

                    val items = mutableListOf<InstantSearchItem>()
                    val matchingArtist = artists.firstOrNull { it.name.startsWith(trimmed, ignoreCase = true) }
                        ?: artists.firstOrNull()
                    if (matchingArtist != null) {
                        items.add(InstantSearchItem.ArtistMatch(matchingArtist.id, matchingArtist.name, matchingArtist.imageUrl))
                    }
                    songs.forEach { track ->
                        items.add(InstantSearchItem.SongMatch(track))
                    }
                    if (items.none { it is InstantSearchItem.ArtistMatch } && albums.isNotEmpty()) {
                        val alb = albums.first()
                        items.add(InstantSearchItem.AlbumMatch(alb.id, alb.title, alb.artist, alb.artUri))
                    }
                    _instantSearchItems.value = items
                }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    Log.e(TAG, "Suggestions fetch failed: ${e.message}")
                }
            }
        }
    }

    fun clearSearch() {
        lastSearchQuery = null
        lastSearchType = null
        _searchResults.value = emptyList()
        _searchVideos.value = emptyList()
        _searchAlbums.value = emptyList()
        _searchArtists.value = emptyList()
        _searchPlaylists.value = emptyList()
        _searchFeaturedPlaylists.value = emptyList()
        _searchPodcasts.value = emptyList()
        _searchSuggestions.value = emptyList()
        _instantSearchItems.value = emptyList()
        _isSearching.value = false
    }

    fun selectSearchResult(query: String, type: String, itemId: String?, imageUrl: String?, subtitle: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addRecentSearch(query = query, type = type, itemId = itemId, imageUrl = imageUrl, subtitle = subtitle)
        }
    }

    fun removeSearchHistory(item: RecentSearchItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeRecentSearch(item)
        }
    }

    fun clearAllSearchHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearRecentSearches()
        }
    }

    fun playQueue(
        tracks: List<Track>,
        startIndex: Int = 0,
        isVideo: Boolean = false,
        preservePlaybackSource: Boolean = false
    ) {
        if (tracks.isEmpty() || startIndex !in tracks.indices) return
        val controller = _controller.value ?: return

        resetCanvasForNewTrack()
        val initial = tracks.getOrNull(startIndex)
        val initialIsVideo = isVideo || isTrackVideo(initial)
        stickyVideoMode = initialIsVideo
        _isVideoMode.value = initialIsVideo

        loadTrackJob?.cancel()
        loadTrackJob = viewModelScope.launch {
            _isLoadingTrack.value = true

            val resolvedPairs = withContext(Dispatchers.IO) {
                tracks.mapIndexed { idx, t ->
                    val dbT = database.trackDao().getTrackById(t.id)
                    resolveTrackForPlayback(
                        incoming = t,
                        dbTrack = dbT,
                        explicitIsVideo = (idx == startIndex && isVideo)
                    )
                }
            }
            val mappedTracks = resolvedPairs.map { it.first }
            val (track, shouldBeVideo) = resolvedPairs[startIndex]

            _currentTrack.value = track
            _isVideoMode.value = shouldBeVideo
            _hasTrackLoaded.value = true
            pendingExplicitPlayTrackId = track.id

            if (!preservePlaybackSource) {
                _playbackSource.value = when {
                    _isSearching.value -> "Search"
                    _currentTab.value == HomeTab.Home -> "Home"
                    else -> "Library"
                }
            }

            autoGeneratedTrackIds.clear()
            setQueue(mappedTracks)
            _originalPlaybackQueue.value = mappedTracks
            setIndex(startIndex)

            try {
                // Persist normalized video/metadata track BEFORE MediaController resolves URI
                withContext(Dispatchers.IO) {
                    runCatching { repository.saveTrack(track) }
                }

                val mediaItems = resolvedPairs.map { (t, vid) ->
                    buildMediaItem(t, forceVideo = vid)
                }

                controller.setMediaItems(mediaItems, startIndex, C.TIME_UNSET)
                controller.prepare()
                controller.play()

                if (_endlessMode.value && track.isYouTube) {
                    checkAndFetchRelatedTracks(track.id)
                }

                try {
                    markTrackAsCached(track.id)
                } catch (e: Exception) {
                    Log.e(TAG, "Cache mark error", e)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Playback error", e)
                if (isActive) {
                    runOnMain { Toast.makeText(getApplication(), "Playback Error: ${e.message}", Toast.LENGTH_SHORT).show() }
                }
            } finally {
                _isLoadingTrack.value = false
            }
        }
    }

    fun startArtistRadio(topTracks: List<Track>) {
        if (topTracks.isEmpty()) return
        _endlessMode.value = true
        _playbackSource.value = "Artist Radio"
        playQueue(topTracks, 0)
    }

    fun playTrack(track: Track, clearQueue: Boolean = true, isVideo: Boolean = false) {
        val shouldBeVideo = isVideo || isTrackVideo(track)
        if (clearQueue) {
            playQueue(listOf(track), 0, isVideo = shouldBeVideo)
            return
        }
        resetCanvasForNewTrack()
        _isVideoMode.value = shouldBeVideo

        val controller = _controller.value ?: return

        loadTrackJob?.cancel()
        loadTrackJob = viewModelScope.launch {
            _isLoadingTrack.value = true

            val dbTrack = withContext(Dispatchers.IO) { database.trackDao().getTrackById(track.id) }
            val (finalTrack, resolvedIsVideo) = resolveTrackForPlayback(
                incoming = track,
                dbTrack = dbTrack,
                explicitIsVideo = isVideo
            )
            _currentTrack.value = finalTrack
            _isVideoMode.value = resolvedIsVideo
            _hasTrackLoaded.value = true
            pendingExplicitPlayTrackId = finalTrack.id
            markTrackAsCached(finalTrack.id)

            // Set source based on current state
            _playbackSource.value = when {
                _isSearching.value -> "Search"
                _currentTab.value == HomeTab.Home -> "Home"
                else -> "Library"
            }

            setIndex(_playbackQueue.value.indexOfFirst { it.id == finalTrack.id })

            try {
                if (finalTrack.isYouTube) {
                    withContext(Dispatchers.IO) {
                        runCatching { repository.saveTrack(finalTrack) }
                    }
                }

                val mediaItem = buildMediaItem(finalTrack, forceVideo = resolvedIsVideo)

                // Check if already in controller queue to avoid duplicate loads
                var foundIdx = -1
                for (i in 0 until controller.mediaItemCount) {
                    if (controller.getMediaItemAt(i).mediaId == finalTrack.id) {
                        foundIdx = i
                        break
                    }
                }

                if (foundIdx != -1) {
                    val existingUri = controller.getMediaItemAt(foundIdx).localConfiguration?.uri?.toString().orEmpty()
                    val desiredIsVideoUri = resolvedIsVideo && finalTrack.isYouTube && !isDownloaded(finalTrack.id)
                    if (desiredIsVideoUri != existingUri.startsWith("ytvideo://")) {
                        controller.replaceMediaItem(foundIdx, mediaItem)
                    }
                    controller.seekToDefaultPosition(foundIdx)
                } else {
                    controller.addMediaItem(mediaItem)
                    controller.seekToDefaultPosition(controller.mediaItemCount - 1)
                }

                controller.prepare()
                controller.play()

                // Trigger endless if needed
                if (_endlessMode.value && finalTrack.isYouTube) {
                    checkAndFetchRelatedTracks(finalTrack.id)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Playback error", e)
                if (isActive) {
                    runOnMain { Toast.makeText(getApplication(), "Playback Error: ${e.message}", Toast.LENGTH_SHORT).show() }
                }
            } finally {
                _isLoadingTrack.value = false
            }
        }
    }

    fun removeFromQueue(absoluteIndex: Int, track: Track) {
        viewModelScope.launch {
            queueMutationMutex.withLock {
                val controller = _controller.value
                val updatedQueue = _playbackQueue.value.toMutableList()
                if (updatedQueue.isEmpty()) return@withLock

                val activeIdx = resolveActiveQueueIndex(updatedQueue.size, controller)
                var targetIndex = -1
                if (absoluteIndex in updatedQueue.indices && updatedQueue[absoluteIndex].id == track.id) {
                    targetIndex = absoluteIndex
                } else {
                    // Fall back to next upcoming match first, then active track, then any match in queue.
                    val searchStart = (activeIdx + 1).coerceIn(0, updatedQueue.size)
                    for (idx in searchStart until updatedQueue.size) {
                        if (updatedQueue[idx].id == track.id) {
                            targetIndex = idx
                            break
                        }
                    }
                    if (targetIndex == -1 && activeIdx in updatedQueue.indices && updatedQueue[activeIdx].id == track.id) {
                        targetIndex = activeIdx
                    }
                    if (targetIndex == -1) {
                        targetIndex = updatedQueue.indexOfFirst { it.id == track.id }
                    }
                }

                if (targetIndex != -1) {
                    updatedQueue.removeAt(targetIndex)
                    autoGeneratedTrackIds.remove(track.id)

                    val updatedOriginal = _originalPlaybackQueue.value.toMutableList()
                    val origIdx = updatedOriginal.indexOfFirst { it.id == track.id }
                    if (origIdx != -1) {
                        updatedOriginal.removeAt(origIdx)
                        _originalPlaybackQueue.value = updatedOriginal
                    }

                    if (updatedQueue.isEmpty()) {
                        setQueue(emptyList())
                        setIndex(-1)
                        _currentTrack.value = null
                        _hasTrackLoaded.value = false
                        _isPlaying.value = false
                        controller?.let { ctrl ->
                            ctrl.stop()
                            ctrl.clearMediaItems()
                        }
                        invalidateStagedNextCrossfade()
                        Toast.makeText(getApplication(), "Queue cleared", Toast.LENGTH_SHORT).show()
                        return@withLock
                    }

                    setQueue(updatedQueue)
                    if (targetIndex < activeIdx) {
                        setIndex((activeIdx - 1).coerceAtLeast(0))
                    } else if (targetIndex == activeIdx) {
                        val nextIdx = targetIndex.coerceIn(0, updatedQueue.lastIndex)
                        setIndex(nextIdx)
                        _currentTrack.value = updatedQueue[nextIdx]
                    }

                    if (controller != null && targetIndex < controller.mediaItemCount) {
                        controller.removeMediaItem(targetIndex)
                    }
                    if (targetIndex <= activeIdx + 1) {
                        invalidateStagedNextCrossfade()
                    }
                    Toast.makeText(getApplication(), "Removed from queue", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            queueMutationMutex.withLock {
                val controller = _controller.value ?: return@withLock
                val queue = _playbackQueue.value.toMutableList()
                if (fromIndex in 0 until queue.size && toIndex in 0 until queue.size) {
                    val activeIdx = resolveActiveQueueIndex(queue.size, controller)
                    val item = queue.removeAt(fromIndex)
                    queue.add(toIndex, item)
                    setQueue(queue)

                    if (fromIndex < controller.mediaItemCount && toIndex < controller.mediaItemCount) {
                        controller.moveMediaItem(fromIndex, toIndex)
                    }
                    val updatedIdx = controller.currentMediaItemIndex
                    if (updatedIdx in queue.indices) {
                        setIndex(updatedIdx)
                    } else if (activeIdx in queue.indices) {
                        // Track active item's shifted position if controller hasn't updated yet
                        val shifted = when {
                            fromIndex == activeIdx -> toIndex
                            fromIndex < activeIdx && toIndex >= activeIdx -> activeIdx - 1
                            fromIndex > activeIdx && toIndex <= activeIdx -> activeIdx + 1
                            else -> activeIdx
                        }
                        setIndex(shifted.coerceIn(0, queue.lastIndex))
                    }

                    if (!_shuffleMode.value) {
                        val original = _originalPlaybackQueue.value.toMutableList()
                        if (fromIndex in 0 until original.size && toIndex in 0 until original.size) {
                            val origItem = original.removeAt(fromIndex)
                            original.add(toIndex, origItem)
                            _originalPlaybackQueue.value = original
                        }
                    }
                    if (fromIndex == activeIdx + 1 || toIndex == activeIdx + 1) {
                        invalidateStagedNextCrossfade()
                    }
                }
            }
        }
    }

    fun addToQueue(track: Track, autoGenerated: Boolean = false) {
        addTracksToQueue(listOf(track), autoGenerated)
    }

    fun addTracksToQueue(tracks: List<Track>, autoGenerated: Boolean = false) {
        viewModelScope.launch {
            if (tracks.isEmpty()) return@launch
            var startedFreshQueue = false
            queueMutationMutex.withLock {
                val resolvedTracks = tracks.map { track ->
                    val dbTrack = if (track.isYouTube) database.trackDao().getTrackById(track.id) else null
                    resolveTrackForPlayback(track, dbTrack, isTrackVideo(track)).first
                }
                if (!autoGenerated) {
                    repository.saveTracks(resolvedTracks)
                    autoGeneratedTrackIds.removeAll(resolvedTracks.map { it.id }.toSet())
                } else {
                    autoGeneratedTrackIds.addAll(resolvedTracks.map { it.id })
                }

                val controller = _controller.value
                val currentQueue = _playbackQueue.value
                if (!autoGenerated && (currentQueue.isEmpty() || controller == null || controller.mediaItemCount == 0)) {
                    playQueue(resolvedTracks, 0)
                    startedFreshQueue = true
                    val message = if (resolvedTracks.size == 1) {
                        "Playing ${resolvedTracks.first().title}"
                    } else {
                        "Playing ${resolvedTracks.size} tracks"
                    }
                    Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show()
                    loadTrackJob?.join()
                    return@withLock
                }

                val updatedQueue = currentQueue.toMutableList()
                val updatedOriginal = _originalPlaybackQueue.value.toMutableList()
                val activeIdx = resolveActiveQueueIndex(updatedQueue.size, controller)
                val insertIdx = if (autoGenerated) {
                    updatedQueue.size
                } else {
                    findUserQueueInsertionIndex(updatedQueue, activeIdx)
                }
                val origActiveIdx = resolveOriginalActiveIndex(updatedOriginal, activeIdx, updatedQueue)
                val origInsertIdx = if (autoGenerated) {
                    updatedOriginal.size
                } else {
                    findUserQueueInsertionIndex(updatedOriginal, origActiveIdx)
                }

                updatedQueue.addAll(insertIdx.coerceIn(0, updatedQueue.size), resolvedTracks)
                updatedOriginal.addAll(origInsertIdx.coerceIn(0, updatedOriginal.size), resolvedTracks)
                setQueue(updatedQueue)
                _originalPlaybackQueue.value = updatedOriginal

                controller?.let { ctrl ->
                    val mediaItems = resolvedTracks.map { buildMediaItem(it) }
                    val ctrlIdx = insertIdx.coerceIn(0, ctrl.mediaItemCount)
                    if (ctrlIdx >= ctrl.mediaItemCount) {
                        ctrl.addMediaItems(mediaItems)
                    } else {
                        ctrl.addMediaItems(ctrlIdx, mediaItems)
                    }
                    if (ctrl.playbackState == Player.STATE_IDLE) {
                        ctrl.prepare()
                    }
                }
                if (insertIdx == activeIdx + 1) {
                    invalidateStagedNextCrossfade()
                }
            }
            if (!autoGenerated && !startedFreshQueue) {
                val message = if (tracks.size == 1) "Added to queue" else "Added ${tracks.size} tracks to queue"
                Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun playNext(track: Track) {
        playTracksNext(listOf(track))
    }

    fun playTracksNext(tracks: List<Track>) {
        viewModelScope.launch {
            if (tracks.isEmpty()) return@launch
            var startedFreshQueue = false
            queueMutationMutex.withLock {
                val resolvedTracks = tracks.map { track ->
                    val dbTrack = if (track.isYouTube) database.trackDao().getTrackById(track.id) else null
                    resolveTrackForPlayback(track, dbTrack, isTrackVideo(track)).first
                }
                repository.saveTracks(resolvedTracks)
                autoGeneratedTrackIds.removeAll(resolvedTracks.map { it.id }.toSet())

                val controller = _controller.value
                val updatedQueue = _playbackQueue.value.toMutableList()
                if (updatedQueue.isEmpty() || controller == null || controller.mediaItemCount == 0) {
                    playQueue(resolvedTracks, 0)
                    startedFreshQueue = true
                    val msg = if (resolvedTracks.size == 1) {
                        "Playing ${resolvedTracks.first().title}"
                    } else {
                        "Playing ${resolvedTracks.size} tracks"
                    }
                    Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
                    loadTrackJob?.join()
                    return@withLock
                }

                val updatedOriginal = _originalPlaybackQueue.value.toMutableList()
                val activeIdx = resolveActiveQueueIndex(updatedQueue.size, controller)
                val insertIdx = (activeIdx + 1).coerceIn(0, updatedQueue.size)
                val origActiveIdx = resolveOriginalActiveIndex(updatedOriginal, activeIdx, updatedQueue)
                val origInsertIdx = (origActiveIdx + 1).coerceIn(0, updatedOriginal.size)

                updatedQueue.addAll(insertIdx, resolvedTracks)
                updatedOriginal.addAll(origInsertIdx, resolvedTracks)
                setQueue(updatedQueue)
                _originalPlaybackQueue.value = updatedOriginal

                val ctrlIdx = insertIdx.coerceIn(0, controller.mediaItemCount)
                val mediaItems = resolvedTracks.map { buildMediaItem(it) }
                if (ctrlIdx >= controller.mediaItemCount) {
                    controller.addMediaItems(mediaItems)
                } else {
                    controller.addMediaItems(ctrlIdx, mediaItems)
                }
                if (controller.playbackState == Player.STATE_IDLE) {
                    controller.prepare()
                }
                invalidateStagedNextCrossfade()
            }
            if (!startedFreshQueue) {
                val msg = if (tracks.size == 1) "Playing next" else "Playing ${tracks.size} songs next"
                Toast.makeText(getApplication(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun prepareTrackVideoModeAtIndex(controller: MediaController, targetIndex: Int) {
        if (!stickyVideoMode && !_isVideoMode.value) return
        if (targetIndex !in 0 until controller.mediaItemCount) return
        val targetTrack = _playbackQueue.value.getOrNull(targetIndex) ?: return
        if (!targetTrack.isYouTube || isDownloaded(targetTrack.id)) return
        val existingUri = controller.getMediaItemAt(targetIndex).localConfiguration?.uri?.toString().orEmpty()
        if (!existingUri.startsWith("ytvideo://", ignoreCase = true)) {
            controller.replaceMediaItem(targetIndex, buildMediaItem(targetTrack, forceVideo = true))
        }
    }

    /**
     * Skip to the next track using ExoPlayer natively while keeping video mode & playback seamless.
     */
    fun skipNext() {
        val controller = _controller.value ?: return
        val shouldContinue = stickyVideoMode || _isVideoMode.value || controller.playWhenReady || _isPlaying.value
        val nextIdx = controller.nextMediaItemIndex
        if (nextIdx != C.INDEX_UNSET) {
            prepareTrackVideoModeAtIndex(controller, nextIdx)
            controller.seekToDefaultPosition(nextIdx)
            if (shouldContinue) {
                controller.playWhenReady = true
                controller.prepare()
                controller.play()
            }
        } else if (controller.hasNextMediaItem() || _repeatMode.value == Player.REPEAT_MODE_ALL) {
            controller.seekToNextMediaItem()
            if (shouldContinue) {
                controller.playWhenReady = true
                controller.prepare()
                controller.play()
            }
        } else {
            runOnMain { Toast.makeText(getApplication(), "End of Queue", Toast.LENGTH_SHORT).show() }
        }
    }

    /**
     * Skip to the previous track using ExoPlayer natively while keeping video mode & playback seamless.
     */
    fun skipPrevious() {
        val controller = _controller.value ?: return
        if (controller.currentPosition > 3000) {
            controller.seekTo(0)
            return
        }
        val shouldContinue = stickyVideoMode || _isVideoMode.value || controller.playWhenReady || _isPlaying.value
        val prevIdx = controller.previousMediaItemIndex
        if (prevIdx != C.INDEX_UNSET) {
            prepareTrackVideoModeAtIndex(controller, prevIdx)
            controller.seekToDefaultPosition(prevIdx)
            if (shouldContinue) {
                controller.playWhenReady = true
                controller.prepare()
                controller.play()
            }
        } else {
            controller.seekToPreviousMediaItem()
            if (shouldContinue) {
                controller.playWhenReady = true
                controller.prepare()
                controller.play()
            }
        }
    }

    /**
     * Skip to a specific track using ExoPlayer natively while keeping video mode & playback seamless.
     */
    fun skipToQueueItem(index: Int) {
        val controller = _controller.value ?: return
        if (index >= 0 && index < controller.mediaItemCount) {
            val shouldContinue = stickyVideoMode || _isVideoMode.value || controller.playWhenReady || _isPlaying.value
            prepareTrackVideoModeAtIndex(controller, index)
            controller.seekToDefaultPosition(index)
            if (shouldContinue) {
                controller.playWhenReady = true
                controller.prepare()
                controller.play()
            }
        }
    }

    /** Build a MediaItem from a track, using yt:// or ytvideo:// protocol for YouTube tracks */
    private fun buildMediaItem(track: Track, forceVideo: Boolean? = null): MediaItem {
        val isLocal = isDownloaded(track.id)
        val isVid = track.isYouTube && !isLocal && (forceVideo ?: isTrackVideo(track))
        val uri = if (isLocal) {
            val context = getApplication<Application>().applicationContext
            val localFile = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "${track.id}.m4a")
            localFile.absolutePath
        } else if (track.isYouTube) {
            if (isVid) "ytvideo://${track.id}" else "yt://${track.id}"
        } else {
            track.path
        }
        val activeQuality = prefsManager.audioQuality
        val diskKey = when {
            isVid -> "ytvideo_${track.id}"
            activeQuality == com.example.ecodot.data.local.prefs.AudioQuality.HIGH -> "yt_${track.id}"
            else -> "yt_${track.id}_${activeQuality.name.lowercase()}"
        }
        val builder = MediaItem.Builder()
            .setMediaId(track.id)
            .setUri(uri.toUri())
            .setCustomCacheKey(diskKey)
            .setMediaMetadata(MediaMetadata.Builder()
                .setTitle(track.title)
                .setArtist(track.artist)
                .setAlbumTitle(track.album)
                .setArtworkUri(track.albumArtUri?.toUri())
                .build())
        if (isVid) {
            builder.setMimeType(androidx.media3.common.MimeTypes.APPLICATION_M3U8)
        }
        return builder.build()
    }

    fun setVideoMode(isVideo: Boolean) {
        stickyVideoMode = isVideo
        val currentTrackVal = _currentTrack.value
        if (isVideo && currentTrackVal != null && currentTrackVal.isYouTube && !currentTrackVal.path.startsWith("ytvideo://", ignoreCase = true)) {
            val upgraded = currentTrackVal.copy(
                path = "ytvideo://${currentTrackVal.id}",
                album = if (isGenericOrVideoPlaceholderAlbum(currentTrackVal.album)) "YouTube Video" else currentTrackVal.album
            )
            _currentTrack.value = upgraded
            viewModelScope.launch(Dispatchers.IO) {
                runCatching { repository.saveTrack(upgraded) }
            }
        }
        val track = _currentTrack.value
        val controller = _controller.value
        val stateChanged = _isVideoMode.value != isVideo
        _isVideoMode.value = isVideo
        if (track == null || controller == null) return

        // Only replace media item if it's YouTube and URI scheme actually differs
        if (track.isYouTube && !isDownloaded(track.id)) {
            val currentUri = controller.currentMediaItem?.localConfiguration?.uri?.toString().orEmpty()
            val alreadyMatches = if (isVideo) currentUri.startsWith("ytvideo://") else currentUri.startsWith("yt://")
            if (!alreadyMatches) {
                val currentPosition = controller.currentPosition
                val shouldResume = controller.playWhenReady || _isPlaying.value || isVideo
                val mediaItem = buildMediaItem(track, forceVideo = isVideo)
                val currentIndex = controller.currentMediaItemIndex
                if (currentIndex != androidx.media3.common.C.INDEX_UNSET) {
                    controller.replaceMediaItem(currentIndex, mediaItem)
                    controller.seekTo(currentIndex, currentPosition)
                    controller.prepare()
                    if (shouldResume) {
                        controller.playWhenReady = true
                        controller.play()
                    }
                }
            } else if (stateChanged && !controller.isPlaying) {
                controller.playWhenReady = true
                controller.play()
            }
            if (isVideo) {
                val nextIdx = controller.nextMediaItemIndex
                if (nextIdx != C.INDEX_UNSET) prepareTrackVideoModeAtIndex(controller, nextIdx)
            }
        }
    }

    fun togglePlayback() {
        val controller = _controller.value ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            controller.play()
        }
    }

    fun updatePaletteColors(dominant: Color, accent: Color) {
        _dominantColor.value = dominant
        _accentColor.value = accent
    }

    /**
     * Extracts colors from the album art URI and updates the theme.
     * Uses Coil for image loading and Palette for color extraction.
     */
    private fun updateThemeFromArt(artUri: String) {
        viewModelScope.launch {
            try {
                // In a real implementation with kmpalette, we'd use the loader.
                // For now, we'll simulate color extraction or use a simpler method 
                // if the full kmpalette setup requires a Composable context (which rememberPaletteState does).
                // Since this is a ViewModel, we'll use android.graphics.Bitmap + Palette directly.
                
                withContext(Dispatchers.IO) {
                    val loader = coil.Coil.imageLoader(getApplication())
                    val request = coil.request.ImageRequest.Builder(getApplication())
                        .data(artUri)
                        .size(128, 128)
                        .allowHardware(false) // Required for Palette
                        .build()
                    
                    val result = loader.execute(request)
                    val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
                    
                    if (bitmap != null) {
                        val palette = Palette.from(bitmap).generate()
                        // SimpMusic palette hierarchy for vibrant concert & ambient breathing lights
                        val defaultDark = 0xFF191C19.toInt()
                        val dominantRgb = palette.getDarkVibrantColor(0)
                            .takeIf { it != 0 }
                            ?: palette.getVibrantColor(0).takeIf { it != 0 }
                            ?: palette.getDarkMutedColor(0).takeIf { it != 0 }
                            ?: palette.getMutedColor(0).takeIf { it != 0 }
                            ?: palette.getDominantColor(defaultDark)

                        val accentRgb = palette.vibrantSwatch?.rgb
                            ?: palette.lightVibrantSwatch?.rgb
                            ?: palette.darkVibrantSwatch?.rgb
                            ?: palette.getVibrantColor(palette.getMutedColor(0xFF79DA9F.toInt()))

                        withContext(Dispatchers.Main) {
                            _dominantColor.value = Color(dominantRgb)
                            _accentColor.value = Color(accentRgb)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to extract colors from $artUri", e)
            }
        }
    }

    fun setBlurIntensity(value: Float) {
        _blurIntensity.value = value
    }

    fun toggleCrossfade() {
        setCrossfadeEnabled(!_crossfadeEnabled.value)
    }

    private fun fetchSuggestions(seedTrack: Track) {
        viewModelScope.launch {
            try {
                val related = repository.getYouTubeRelatedTracks(seedTrack.id)
                _suggestedTracks.value = related.take(10)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch suggestions", e)
            }
        }
    }

    fun fetchVideoInfo(videoId: String) {
        if (videoId.isBlank()) return
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _isVideoInfoLoading.value = true
            if (_videoInfo.value?.videoId != videoId) {
                _videoInfo.value = null
            }
            try {
                val info = repository.fetchVideoInfo(videoId, prefsManager.audioQuality)
                if (info != null || _videoInfo.value?.videoId != videoId) {
                    _videoInfo.value = info
                }
            } catch (e: Exception) {
                Log.e(TAG, "fetchVideoInfo failed", e)
            } finally {
                _isVideoInfoLoading.value = false
            }
        }
    }

    fun updateProfile(name: String, genres: String, avatarUrl: String?, isPro: Boolean) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfile()
                        val savedAvatarUrl = if (avatarUrl != null && avatarUrl.startsWith("content://")) {
                try {
                    val context = getApplication<Application>()
                    val uri = Uri.parse(avatarUrl)
                    val inputStream = context.contentResolver.openInputStream(uri)
                    if (inputStream != null) {
                        val bytes = inputStream.readBytes()
                        inputStream.close()
                        // Remove old avatar files so storage stays bounded while Coil gets a fresh cache key
                        context.filesDir.listFiles { file ->
                            file.isFile && file.name.startsWith("user_avatar") && file.name.endsWith(".jpg")
                        }?.forEach { old -> runCatching { old.delete() } }
                        val destFile = File(context.filesDir, "user_avatar_${System.currentTimeMillis()}.jpg")
                        destFile.writeBytes(bytes)
                        Uri.fromFile(destFile).toString()
                    } else {
                        avatarUrl
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to save avatar locally", e)
                    avatarUrl
                }
            } else {
                avatarUrl
            }
            repository.updateProfile(current.copy(
                name = name, 
                favoriteGenres = genres,
                avatarUri = savedAvatarUrl,
                isPro = isPro
            ))
        }
    }

    fun formatDuration(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }


    fun setPlayerStyle(style: com.example.ecodot.data.local.prefs.PlayerStyle) {
        prefsManager.playerStyle = style
        _playerStyle.value = style
    }

    fun applyEqPreset(preset: com.example.ecodot.domain.model.EqPreset) {
        _selectedEqPreset.value = preset.name
        prefsManager.selectedEqPreset = preset.name
        val updated = _eqBands.value.toMutableMap()
        preset.bands.forEachIndexed { band, level ->
            updated[band] = level
            sendCustomCommand("SET_EQ_BAND", Bundle().apply {
                putInt("band", band)
                putInt("level", level)
            })
        }
        _eqBands.value = updated
    }

    fun setEqBand(band: Int, level: Int, isFromPreset: Boolean = false) {
        if (!isFromPreset && _selectedEqPreset.value != "Custom") {
            _selectedEqPreset.value = "Custom"
            prefsManager.selectedEqPreset = "Custom"
        }
        val updated = _eqBands.value.toMutableMap()
        updated[band] = level
        _eqBands.value = updated
        sendCustomCommand("SET_EQ_BAND", Bundle().apply {
            putInt("band", band)
            putInt("level", level)
        })
    }

    fun toggleEq() {
        val newValue = !_eqEnabled.value
        prefsManager.equalizerEnabled = newValue
        _eqEnabled.value = newValue
        sendCustomCommand("SET_EQ_ENABLED", Bundle().apply {
            putBoolean("enabled", newValue)
        })
    }

    fun setVirtualizerStrength(strength: Int) {
        prefsManager.virtualizerStrength = strength
        _virtualizerStrength.value = strength
        sendCustomCommand("SET_VIRTUALIZER", Bundle().apply {
            putInt("strength", strength)
        })
    }

    private fun sendCustomCommand(action: String, args: Bundle) {
        val controller = _controller.value ?: return
        controller.sendCustomCommand(SessionCommand(action, Bundle.EMPTY), args)
    }

    // ======================== PLAYLIST MANAGEMENT ========================

    fun createPlaylist(name: String, description: String = "") {
        createPlaylistCustom(name, description, null)
    }

    fun createPlaylistCustom(name: String, description: String = "", customJson: String? = null) {
        viewModelScope.launch {
            val id = repository.createPlaylist(name, description)
            if (customJson != null) {
                val playlists = repository.allPlaylists.first()
                val playlist = playlists.find { it.id == id }
                if (playlist != null) {
                    repository.updatePlaylistDetails(playlist, name, description, customJson)
                }
            }
        }
    }

    fun generateDailyMix(
        name: String,
        mood: String,
        size: Int,
        type: String, // "Local Only", "YouTube", "Mixed"
        style: String = "Pop",
        strategy: String = "Most Listened",
        tags: String = ""
    ) {
        viewModelScope.launch {
            try {
                // 1. Create custom theme JSON based on mood
                val moodPalette = mapOf(
                    "Chill" to ("#FFF857A6" to "#FFFF5858"),
                    "Focus" to ("#FF00C9FF" to "#FF92FE9D"),
                    "Workout" to ("#FFFF007F" to "#FF3F00FF"),
                    "Party" to ("#FF7F00FF" to "#FFE100FF"),
                    "Sleep" to ("#FF0B3C5D" to "#FF328CC1"),
                    "Study" to ("#FF1D2731" to "#FF007A87"),
                    "Upbeat" to ("#FFF9D423" to "#FFFF4E50"),
                    "Romance" to ("#FFFF2658" to "#FF86002A"),
                    "Gaming" to ("#FF00F2FE" to "#FF4FACFE"),
                    "Acoustic" to ("#FFB8A9C9" to "#FF5B92E5")
                )
                val (startHex, endHex) = moodPalette[mood] ?: ("#FF1DB954" to "#FF070708")
                val coverTags = if (moodPalette.containsKey(mood)) listOf("daily", mood.lowercase()) else listOf("daily")
                val theme = com.example.ecodot.util.PlaylistCustomizer.serializeCustomCover(
                    com.example.ecodot.util.CustomCover(startHex, endHex, false, coverTags)
                )

                val playlistId = repository.createPlaylist(name, "Your personalized daily $mood mix using $strategy strategy.")
                val playlists = repository.allPlaylists.first()
                val playlist = playlists.find { it.id == playlistId }
                if (playlist != null) {
                    repository.updatePlaylistDetails(playlist, name, playlist.description, theme)
                }

                // 2. Query tracks according to Curation Strategy
                val localTracks = repository.allTracks.first()
                val baseTracks = when (strategy) {
                    "Most Listened" -> {
                        localTracks.filter { it.playCount > 0 }.sortedByDescending { it.playCount }
                    }
                    "Recently Played" -> {
                        localTracks.filter { it.lastPlayedAt != null }.sortedByDescending { it.lastPlayedAt }
                    }
                    "Followed Artists" -> {
                        val followed = repository.followedArtists.first().map { it.artistName.lowercase() }
                        localTracks.filter { track -> followed.any { artist -> track.artist.lowercase().contains(artist) } }
                    }
                    "On Repeat" -> {
                        localTracks.filter { it.playCount >= 3 }.sortedByDescending { it.playCount }
                    }
                    "Taste Recommendations" -> {
                        val topGen = topGenre.value.lowercase()
                        if (topGen.isNotBlank() && topGen != "unknown") {
                            localTracks.filter { it.genre?.lowercase()?.contains(topGen) == true }
                        } else {
                            localTracks
                        }
                    }
                    else -> localTracks
                }

                // Combine strategy results with mood keywords and styles
                val keywords = when (mood) {
                    "Chill" -> listOf("chill", "lofi", "lo-fi", "sleep", "relax", "acoustic", "calm", "slow", "ambient", "soft", "jazz", "piano", "soul")
                    "Focus" -> listOf("focus", "study", "beats", "instrumental", "classical", "synthwave", "coding", "ambient", "calm", "relax")
                    "Workout" -> listOf("workout", "gym", "hype", "run", "fast", "energetic", "rock", "rap", "metal", "techno", "upbeat")
                    "Party" -> listOf("party", "dance", "club", "house", "upbeat", "electronic", "electro", "techno", "pop", "hip hop")
                    "Sleep" -> listOf("sleep", "lullaby", "relaxing", "calm", "ambient", "rain", "soft", "peaceful")
                    "Study" -> listOf("study", "piano", "focus", "instrumental", "ambient", "classical")
                    "Upbeat" -> listOf("happy", "sunshine", "upbeat", "energetic", "cheerful", "pop", "dance")
                    "Romance" -> listOf("love", "romantic", "romance", "ballad", "slow", "sweet", "r&b")
                    "Gaming" -> listOf("gaming", "synthwave", "electronic", "cyber", "action", "beat", "hype", "ost")
                    "Acoustic" -> listOf("acoustic", "guitar", "unplugged", "singer", "folk", "indie")
                    else -> emptyList()
                }

                val styleKeywords = when(style.lowercase()) {
                    "rap" -> listOf("rap", "trap", "flow", "bars", "hip hop")
                    "hip hop" -> listOf("hip hop", "hiphop", "rap", "r&b", "beat")
                    "pop" -> listOf("pop", "chart", "hits", "dance")
                    "rock" -> listOf("rock", "metal", "grunge", "indie rock", "guitar")
                    "lofi" -> listOf("lofi", "lo-fi", "relax", "chillhop", "chill")
                    "electronic" -> listOf("electronic", "electro", "edm", "house", "techno")
                    "acoustic" -> listOf("acoustic", "guitar", "singer", "unplugged")
                    "instrumental" -> listOf("instrumental", "beats", "orchestra", "piano")
                    else -> listOf(style.lowercase())
                }

                val allSearchKeywords = keywords + styleKeywords + tags.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }

                val matchedLocal = (if (baseTracks.isEmpty()) localTracks else baseTracks).filter { track ->
                    allSearchKeywords.any { kw ->
                        track.title.contains(kw, ignoreCase = true) ||
                        track.artist.contains(kw, ignoreCase = true) ||
                        track.album.contains(kw, ignoreCase = true) ||
                        (track.genre != null && track.genre.contains(kw, ignoreCase = true))
                    }
                }.shuffled()

                val finalTracks = mutableListOf<Track>()
                if (type == "Local Only") {
                    finalTracks.addAll(matchedLocal.take(size))
                    if (finalTracks.size < size) {
                        val remaining = (if (baseTracks.isEmpty()) localTracks else baseTracks).filter { !finalTracks.contains(it) }.shuffled()
                        finalTracks.addAll(remaining.take(size - finalTracks.size))
                    }
                } else if (type == "YouTube") {
                    val queryBuilder = StringBuilder()
                    if (strategy == "Taste Recommendations" && topGenre.value.isNotBlank()) {
                        queryBuilder.append("${topGenre.value} ")
                    } else if (strategy == "Followed Artists") {
                        val followed = repository.followedArtists.first()
                        if (followed.isNotEmpty()) queryBuilder.append("${followed.shuffled().first().artistName} ")
                    }
                    queryBuilder.append("$style $mood mix")
                    val ytResults = repository.searchYouTube(queryBuilder.toString()).shuffled()
                    finalTracks.addAll(ytResults.take(size))
                } else { // "Mixed"
                    val localTake = size / 2
                    finalTracks.addAll(matchedLocal.take(localTake))
                    
                    val queryBuilder = StringBuilder()
                    if (strategy == "Taste Recommendations" && topGenre.value.isNotBlank()) {
                        queryBuilder.append("${topGenre.value} ")
                    } else if (strategy == "Followed Artists") {
                        val followed = repository.followedArtists.first()
                        if (followed.isNotEmpty()) queryBuilder.append("${followed.shuffled().first().artistName} ")
                    }
                    queryBuilder.append("$style $mood mix")

                    val ytResults = repository.searchYouTube(queryBuilder.toString()).shuffled()
                    finalTracks.addAll(ytResults.take(size - finalTracks.size))

                    if (finalTracks.size < size) {
                        val remaining = (if (baseTracks.isEmpty()) localTracks else baseTracks).filter { !finalTracks.contains(it) }.shuffled()
                        finalTracks.addAll(remaining.take(size - finalTracks.size))
                    }
                }

                finalTracks.distinctBy { it.id }.forEach { track ->
                    repository.addTrackToPlaylist(playlistId, track)
                }
            } catch (e: Exception) {
                Log.e("MusicViewModel", "Failed to generate daily mix", e)
            }
        }
    }

    // ======================== LIKED SONGS ========================
    // Single source of truth: DB query driven by currentTrack.
    // Automatically correct when track changes OR when user likes/unlikes.
    val isCurrentTrackLiked: StateFlow<Boolean> = currentTrack
        .flatMapLatest { track ->
            if (track == null) kotlinx.coroutines.flow.flowOf(false)
            else repository.isTrackLiked(track.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val likedTrackIds: StateFlow<Set<String>> = repository.getLikedTrackIds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val likedTracks: StateFlow<List<Track>> = repository.allTracks
        .combine(likedTrackIds) { all, ids ->
            all.filter { it.id in ids }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var lastServiceBitrateBps: Int = 0
    private var lastServiceSampleRateHz: Int = 0
    private var lastServiceCodec: String? = null
    private var lastServiceMimeType: String? = null

    private fun normalizeSampleRate(rawHz: Int, serviceHz: Int, resolverHz: Int): Int {
        val preferredFull = serviceHz.takeIf { it >= 44_100 } ?: resolverHz.takeIf { it >= 44_100 }
        return when {
            rawHz in 1..24_000 && preferredFull != null -> preferredFull
            rawHz > 0 -> rawHz
            serviceHz > 0 -> serviceHz
            resolverHz > 0 -> resolverHz
            else -> 0
        }
    }

    private fun publishResolvedTelemetry(
        bitrateBps: Int,
        sampleRateHz: Int,
        codec: String?,
        mimeType: String?,
        itag: String?
    ) {
        val normalizedBps = com.example.ecodot.ui.components.player.PlayerStyleMath.normalizeBitrateBps(bitrateBps)
        _activeStreamTelemetry.value = ActiveStreamTelemetry(
            bitrateBps = normalizedBps,
            sampleRateHz = sampleRateHz,
            codec = codec?.takeIf { it.isNotBlank() },
            mimeType = mimeType?.substringBefore(";")?.trim()?.takeIf { it.isNotBlank() },
            itag = itag?.takeIf { it.isNotBlank() && it != "0" }
        )
        _activeStreamLabel.value =
            com.example.ecodot.ui.components.player.PlayerStyleMath.formatRealTimeAudioDetail(
                bitrateBps = normalizedBps,
                sampleRateHz = sampleRateHz
            )
    }

    private fun applyStreamTelemetryExtras(extras: Bundle) {
        val bps = com.example.ecodot.ui.components.player.PlayerStyleMath.normalizeBitrateBps(
            extras.getInt(EcoDotSessionService.EXTRA_STREAM_BITRATE_BPS, 0)
        )
        val hz = extras.getInt(EcoDotSessionService.EXTRA_STREAM_SAMPLE_RATE_HZ, 0)
        val mime = extras.getString(EcoDotSessionService.EXTRA_STREAM_MIME_TYPE)?.takeIf { it.isNotBlank() }
        val codec = extras.getString(EcoDotSessionService.EXTRA_STREAM_CODECS)?.takeIf { it.isNotBlank() }
        if (bps > 0) lastServiceBitrateBps = bps
        if (hz > 0) lastServiceSampleRateHz = hz
        if (mime != null) lastServiceMimeType = mime
        if (codec != null) lastServiceCodec = codec
        updateStreamLabelFromResolvedValues()
    }

    private fun updateStreamLabelFromTracks(tracks: androidx.media3.common.Tracks) {
        val selectedAudioFormat = tracks.groups
            .firstOrNull { it.type == C.TRACK_TYPE_AUDIO && it.isSelected }
            ?.let { group ->
                (0 until group.length)
                    .firstOrNull { idx -> group.isTrackSelected(idx) }
                    ?.let { idx -> group.getTrackFormat(idx) }
            }
        val track = _currentTrack.value
        val resolverMeta = track?.id?.let {
            com.example.ecodot.data.remote.YouTubeStreamResolver.resolvedAudioTelemetry[it]
        }
        val rawBps = selectedAudioFormat?.bitrate?.takeIf { it > 0 }
            ?: selectedAudioFormat?.averageBitrate?.takeIf { it > 0 }
            ?: lastServiceBitrateBps.takeIf { it > 0 }
            ?: resolverMeta?.bitrateBps
            ?: 0
        val bps = com.example.ecodot.ui.components.player.PlayerStyleMath.normalizeBitrateBps(rawBps)
        val hz = normalizeSampleRate(
            rawHz = selectedAudioFormat?.sampleRate ?: 0,
            serviceHz = lastServiceSampleRateHz,
            resolverHz = resolverMeta?.sampleRateHz ?: 0
        )
        val codec = selectedAudioFormat?.codecs?.takeIf { it.isNotBlank() }
            ?: lastServiceCodec
            ?: resolverMeta?.codec
        val mime = selectedAudioFormat?.sampleMimeType?.takeIf { it.isNotBlank() }
            ?: lastServiceMimeType
            ?: resolverMeta?.mimeType
        if (bps > 0) lastServiceBitrateBps = bps
        if (hz > 0) lastServiceSampleRateHz = hz
        if (codec != null) lastServiceCodec = codec
        if (mime != null) lastServiceMimeType = mime
        publishResolvedTelemetry(
            bitrateBps = bps,
            sampleRateHz = hz,
            codec = codec,
            mimeType = mime,
            itag = resolverMeta?.itag?.takeIf { it > 0 }?.toString()
        )
    }

    private fun updateStreamLabelFromResolvedValues() {
        val track = _currentTrack.value
        val resolverMeta = track?.id?.let {
            com.example.ecodot.data.remote.YouTubeStreamResolver.resolvedAudioTelemetry[it]
        }
        val bps = com.example.ecodot.ui.components.player.PlayerStyleMath.normalizeBitrateBps(
            lastServiceBitrateBps.takeIf { it > 0 } ?: resolverMeta?.bitrateBps ?: 0
        )
        val hz = normalizeSampleRate(
            rawHz = lastServiceSampleRateHz,
            serviceHz = lastServiceSampleRateHz,
            resolverHz = resolverMeta?.sampleRateHz ?: 0
        )
        publishResolvedTelemetry(
            bitrateBps = bps,
            sampleRateHz = hz,
            codec = lastServiceCodec ?: resolverMeta?.codec,
            mimeType = lastServiceMimeType ?: resolverMeta?.mimeType,
            itag = resolverMeta?.itag?.takeIf { it > 0 }?.toString()
        )
    }

    /**
     * Likes or unlikes the current track.
     * Only writes to DB — isCurrentTrackLiked updates itself reactively.
     */
    fun likeCurrentTrack() {
        val track = _currentTrack.value ?: return
        toggleTrackLike(track)
    }

    fun toggleTrackLike(track: Track) {
        viewModelScope.launch {
            repository.toggleTrackLike(track)
        }
    }

    suspend fun getPlaylistByName(name: String): com.example.ecodot.data.local.entities.Playlist? {
        return repository.getPlaylistByName(name)
    }

    fun deletePlaylist(playlist: com.example.ecodot.data.local.entities.Playlist) {
        viewModelScope.launch {
            repository.deletePlaylist(playlist)
        }
    }

    fun renamePlaylist(playlist: com.example.ecodot.data.local.entities.Playlist, newName: String) {
        viewModelScope.launch {
            repository.renamePlaylist(playlist, newName)
        }
    }

    fun updatePlaylistDetails(playlist: com.example.ecodot.data.local.entities.Playlist, name: String, description: String, coverArtUri: String?) {
        viewModelScope.launch {
            repository.updatePlaylistDetails(playlist, name, description, coverArtUri)
        }
    }

    fun getPlaylistByIdFlow(playlistId: Long): kotlinx.coroutines.flow.Flow<com.example.ecodot.data.local.entities.Playlist?> {
        return repository.getPlaylistByIdFlow(playlistId)
    }

    fun reorderTrackInPlaylist(playlistId: Long, currentTrackIds: List<String>, fromIndex: Int, toIndex: Int) {
        val list = currentTrackIds.toMutableList()
        if (fromIndex in list.indices && toIndex in list.indices) {
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
            viewModelScope.launch(Dispatchers.IO) {
                repository.reorderTracks(playlistId, list)
            }
        }
    }

    fun savePlaylistTrackOrder(playlistId: Long, trackIds: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.reorderTracks(playlistId, trackIds)
        }
    }

    fun updatePlaylistFolder(playlist: com.example.ecodot.data.local.entities.Playlist, folderName: String?) {
        viewModelScope.launch {
            repository.updatePlaylistFolder(playlist, folderName)
        }
    }

    fun addTrackToPlaylist(playlistId: Long, track: Track) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(playlistId, track)
        }
    }

    fun removeTrackFromPlaylist(playlistId: Long, trackId: String) {
        viewModelScope.launch {
            repository.removeTrackFromPlaylist(playlistId, trackId)
        }
    }

    fun loadPlaylist(playlist: com.example.ecodot.data.local.entities.Playlist) {
        _activePlaylist.value = playlist
        activePlaylistJob?.cancel()
        activePlaylistJob = viewModelScope.launch {
            repository.getTracksInPlaylist(playlist.id).collect { tracks ->
                _activePlaylistTracks.value = tracks
            }
        }
    }

    /**
     * Replaces the current queue with a new list of tracks and starts playing from the specified index.
     */
    fun playTracks(tracks: List<Track>, startIndex: Int = 0, isVideo: Boolean = false) {
        if (tracks.isEmpty()) return
        playQueue(
            tracks = tracks,
            startIndex = startIndex.coerceIn(tracks.indices),
            isVideo = isVideo,
            preservePlaybackSource = true
        )
    }

    fun playPlaylist(playlist: com.example.ecodot.data.local.entities.Playlist) {
        viewModelScope.launch {
            val tracks = repository.getTracksInPlaylist(playlist.id).first()
            if (tracks.isNotEmpty()) {
                _playbackSource.value = playlist.name
                playTracks(tracks)
            } else {
                Toast.makeText(getApplication(), "Playlist is empty", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun shufflePlaylist(playlist: com.example.ecodot.data.local.entities.Playlist) {
        viewModelScope.launch {
            val tracks = repository.getTracksInPlaylist(playlist.id).first()
            if (tracks.isNotEmpty()) {
                _playbackSource.value = playlist.name
                val shuffledTracks = tracks.shuffled()
                _shuffleMode.value = true
                _controller.value?.shuffleModeEnabled = true
                playTracks(shuffledTracks)
            }
        }
    }

    fun addPlaylistToQueue(playlist: com.example.ecodot.data.local.entities.Playlist) {
        viewModelScope.launch {
            val tracks = repository.getTracksInPlaylist(playlist.id).first()
            if (tracks.isNotEmpty()) {
                addTracksToQueue(tracks)
            }
        }
    }

    fun getTracksInPlaylistFlow(playlistId: Long) = repository.getTracksInPlaylist(playlistId)
    fun getPlaylistsForTrackFlow(trackId: String) = repository.getPlaylistsContainingTrack(trackId)
    fun isTrackLiked(trackId: String) = repository.isTrackLiked(trackId)

    val userPrefs = com.example.ecodot.data.local.prefs.UserPreferencesManager(application)
    private val _pinnedPlaylistIds = MutableStateFlow(userPrefs.pinnedPlaylistIds)
    val pinnedPlaylistIds: StateFlow<Set<String>> = _pinnedPlaylistIds.asStateFlow()

    fun isPlaylistPinned(playlistId: Long): Boolean {
        return userPrefs.isPlaylistPinned(playlistId)
    }

    fun togglePinPlaylist(playlistId: Long): Boolean {
        val pinned = userPrefs.togglePinPlaylist(playlistId)
        _pinnedPlaylistIds.value = userPrefs.pinnedPlaylistIds
        return pinned
    }

    fun playPlaylistNext(playlist: com.example.ecodot.data.local.entities.Playlist) {
        viewModelScope.launch {
            val tracks = repository.getTracksInPlaylist(playlist.id).first()
            if (tracks.isNotEmpty()) {
                tracks.reversed().forEach { playNext(it) }
                Toast.makeText(getApplication(), "Playing \"${playlist.name}\" next", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun setTrackAsRingtone(
        track: Track,
        startOffsetMs: Long,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            
            // 1. Check WRITE_SETTINGS permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!android.provider.Settings.System.canWrite(context)) {
                    withContext(Dispatchers.Main) {
                        onResult(false, "PERMISSION_REQUIRED")
                    }
                    return@launch
                }
            }

            try {
                // 2. Locate local track file (downloads only)
                val localFile = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "${track.id}.m4a")
                if (!localFile.exists()) {
                    withContext(Dispatchers.Main) {
                        onResult(false, "Track must be downloaded first to set as ringtone.")
                    }
                    return@launch
                }

                // 3. Create temp output file for the clip
                val cacheDir = context.cacheDir
                val clippedTempFile = File(cacheDir, "temp_ringtone_${track.id}.m4a")
                if (clippedTempFile.exists()) clippedTempFile.delete()

                // 4. Perform clipping using MediaExtractor and MediaMuxer
                clipAudio(localFile, clippedTempFile, startOffsetMs, 30_000L)

                // 5. Insert into MediaStore and set as Ringtone
                val resolver = context.contentResolver
                val ringtoneCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                // Delete previous ringtone entry if exists to avoid duplication
                try {
                    resolver.delete(
                        ringtoneCollection,
                        "${MediaStore.Audio.Media.DISPLAY_NAME} = ?",
                        arrayOf("EcoDot_${track.id}.m4a")
                    )
                } catch (e: Exception) {
                    // Ignore
                }

                val contentValues = ContentValues().apply {
                    put(MediaStore.Audio.Media.DISPLAY_NAME, "EcoDot_${track.id}.m4a")
                    put(MediaStore.Audio.Media.TITLE, "EcoDot: ${track.title} (Clip)")
                    put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp4")
                    put(MediaStore.Audio.Media.IS_RINGTONE, true)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_RINGTONES)
                    }
                }

                val uri = resolver.insert(ringtoneCollection, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        clippedTempFile.inputStream().use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }

                    // Set as actual ringtone
                    android.media.RingtoneManager.setActualDefaultRingtoneUri(
                        context,
                        android.media.RingtoneManager.TYPE_RINGTONE,
                        uri
                    )

                    clippedTempFile.delete()
                    withContext(Dispatchers.Main) {
                        onResult(true, "Ringtone successfully set!")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onResult(false, "Failed to insert ringtone into database.")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set ringtone", e)
                withContext(Dispatchers.Main) {
                    onResult(false, "Error: ${e.message}")
                }
            }
        }
    }

    private fun clipAudio(inputFile: File, outputFile: File, startMs: Long, durationMs: Long) {
        val extractor = android.media.MediaExtractor()
        extractor.setDataSource(inputFile.absolutePath)
        
        val trackIndex = 0
        extractor.selectTrack(trackIndex)
        val format = extractor.getTrackFormat(trackIndex)
        
        val muxer = android.media.MediaMuxer(outputFile.absolutePath, android.media.MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        val muxerTrackIndex = muxer.addTrack(format)
        muxer.start()
        
        val bufferSize = if (format.containsKey(android.media.MediaFormat.KEY_MAX_INPUT_SIZE)) {
            format.getInteger(android.media.MediaFormat.KEY_MAX_INPUT_SIZE)
        } else {
            1024 * 1024
        }
        val dstBuf = java.nio.ByteBuffer.allocate(bufferSize)
        val bufferInfo = android.media.MediaCodec.BufferInfo()
        
        extractor.seekTo(startMs * 1000L, android.media.MediaExtractor.SEEK_TO_CLOSEST_SYNC)
        val endUs = (startMs + durationMs) * 1000L
        
        while (true) {
            bufferInfo.offset = 0
            bufferInfo.size = extractor.readSampleData(dstBuf, 0)
            if (bufferInfo.size < 0) {
                break
            }
            bufferInfo.presentationTimeUs = extractor.sampleTime
            if (bufferInfo.presentationTimeUs > endUs) {
                break
            }
            val sampleFlags = extractor.sampleFlags
            bufferInfo.flags = if (sampleFlags.and(android.media.MediaExtractor.SAMPLE_FLAG_SYNC) != 0) {
                android.media.MediaCodec.BUFFER_FLAG_KEY_FRAME
            } else {
                0
            }
            muxer.writeSampleData(muxerTrackIndex, dstBuf, bufferInfo)
            extractor.advance()
        }
        
        muxer.stop()
        muxer.release()
        extractor.release()
    }

    fun saveAlbum(tracks: List<Track>) {
        viewModelScope.launch {
            try {
                repository.saveTracks(tracks)
            } catch (e: Exception) {
                Log.e("MusicViewModel", "Failed to save album", e)
            }
        }
    }

    fun unsaveAlbum(tracks: List<Track>) {
        viewModelScope.launch {
            try {
                val downloaded = downloadedTracks.value.map { it.id }.toSet()
                val playlists = repository.allPlaylists.first()
                val playlistTrackIds = mutableSetOf<String>()
                playlists.forEach { pl ->
                    val plTracks = repository.getTracksInPlaylist(pl.id).first()
                    playlistTrackIds.addAll(plTracks.map { it.id })
                }
                
                tracks.forEach { track ->
                    if (track.id !in downloaded && track.id !in playlistTrackIds) {
                        repository.deleteTrack(track)
                    }
                }
            } catch (e: Exception) {
                Log.e("MusicViewModel", "Failed to unsave album", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        releaseCanvasPlayer()
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
    }
}

sealed interface InstantSearchItem {
    data class ArtistMatch(val id: String, val name: String, val thumbnailUrl: String?) : InstantSearchItem
    data class SongMatch(val track: Track) : InstantSearchItem
    data class AlbumMatch(val id: String?, val title: String, val artist: String, val artUri: String?) : InstantSearchItem
    data class PlaylistMatch(val id: String, val title: String, val subtitle: String, val thumbnailUrl: String?) : InstantSearchItem
}


