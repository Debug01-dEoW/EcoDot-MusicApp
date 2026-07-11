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
import android.widget.Toast
import android.util.Log
import androidx.media3.common.MediaMetadata
import com.example.ecodot.data.remote.LrcLibResponse
import com.example.ecodot.data.remote.VideoInfo
import androidx.compose.ui.graphics.Color
import android.os.Bundle
import com.google.gson.Gson
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.palette.graphics.Palette
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C as MediaC
import com.example.ecodot.util.NetworkMonitor
import com.example.ecodot.util.NetworkState

data class LyricLine(val timeMs: Long, val text: String)

data class Artist(
    val id: String,
    val name: String,
    val imageUrl: String?
)

data class Album(
    val id: String?,
    val title: String,
    val artist: String,
    val artUri: String?,
    val tracks: List<Track>
)

data class Genre(
    val name: String,
    val tracks: List<Track>
)

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

    private val _cacheSize = MutableStateFlow(0L)
    val cacheSize: StateFlow<Long> = _cacheSize.asStateFlow()

    fun updateCacheSize() {
        _cacheSize.value = com.example.ecodot.playback.CacheManager.getCacheSize(getApplication())
    }

    fun clearCache() {
        com.example.ecodot.playback.CacheManager.clearCache(getApplication())
        updateCacheSize()
    }
    
    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks.asStateFlow()

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
    private val _searchAlbums = MutableStateFlow<List<Album>>(emptyList())
    val searchAlbums: StateFlow<List<Album>> = _searchAlbums.asStateFlow()

    private val _searchArtists = MutableStateFlow<List<Artist>>(emptyList())
    val searchArtists: StateFlow<List<Artist>> = _searchArtists.asStateFlow()

    val searchHistory: StateFlow<List<RecentSearchItem>> = repository.recentSearches
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _searchSuggestions = MutableStateFlow<List<String>>(emptyList())
    val searchSuggestions: StateFlow<List<String>> = _searchSuggestions.asStateFlow()

    private val _videoInfo = MutableStateFlow<VideoInfo?>(null)
    val videoInfo: StateFlow<VideoInfo?> = _videoInfo.asStateFlow()

    private val _isVideoInfoLoading = MutableStateFlow(false)
    val isVideoInfoLoading: StateFlow<Boolean> = _isVideoInfoLoading.asStateFlow()

    private val _homeSections = MutableStateFlow<List<com.example.ecodot.data.remote.HomeSection>>(emptyList())
    val homeSections: StateFlow<List<com.example.ecodot.data.remote.HomeSection>> = _homeSections.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

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

    private val _canvasPlayer = MutableStateFlow<ExoPlayer?>(null)
    val canvasPlayer: StateFlow<ExoPlayer?> = _canvasPlayer.asStateFlow()

    /** Call this to start/replace the canvas for the current track. */
    @androidx.media3.common.util.UnstableApi
    fun setupCanvasPlayer(context: Context, url: String) {
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
                if (updated.canvasUrl.isNullOrBlank() || updated.canvasUrl == "DISABLED") {
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
    private val _eqEnabled = MutableStateFlow(false)
    val eqEnabled: StateFlow<Boolean> = _eqEnabled.asStateFlow()

    private val _eqBands = MutableStateFlow(mapOf<Int, Int>(0 to 0, 1 to 0, 2 to 0, 3 to 0, 4 to 0))
    val eqBands: StateFlow<Map<Int, Int>> = _eqBands.asStateFlow()

    private val _virtualizerStrength = MutableStateFlow(0)
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

    private val _suggestedTracks = MutableStateFlow<List<Track>>(emptyList())
    val suggestedTracks: StateFlow<List<Track>> = _suggestedTracks.asStateFlow()

    private val _historyRecommendations = MutableStateFlow<List<Track>>(emptyList())
    val historyRecommendations: StateFlow<List<Track>> = _historyRecommendations.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initDefaultPlaylists()
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
        controllerFuture = MediaController.Builder(application, sessionToken).buildAsync()
        
        controllerFuture?.addListener({
            try {
                _controller.value = controllerFuture?.get()
                
                // Initial sync upon connect
                val json = Gson().toJson(_playbackQueue.value)
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
                            viewModelScope.launch {
                                val track = database.trackDao().getTrackById(trackId)
                                    ?: findTrackInQueue(trackId)
                                _currentTrack.value = track
                                _hasTrackLoaded.value = true
                                updateDuration()
                                
                                // Update queue index reliably
                                val idx = _controller.value?.currentMediaItemIndex ?: _playbackQueue.value.indexOfFirst { it.id == trackId }
                                if (idx >= 0) setIndex(idx)
                                
                                // Fetch Lyrics
                                track?.let { fetchLyrics(it) }

                                 // Fetch rich video info for info sheet
                                if (track?.isYouTube == true) {
                                    fetchVideoInfo(trackId)
                                }

                                // Update colors from palette
                                track?.albumArtUri?.let { uri ->
                                    updateThemeFromArt(uri)
                                }

                                // Endless logic: if we are near the end, fetch more
                                if (_endlessMode.value && track?.isYouTube == true) {
                                    checkAndFetchRelatedTracks(trackId)
                                }

                                // Automatically log playback on transitions
                                track?.let { repository.logPlayback(it) }
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
                                        // Retry lyrics fetch with real duration
                                        fetchLyrics(updatedTrack)
                                    }
                                }
                            }
                            
                            // If lyrics still null, ensure we have a fetch triggered
                            if (_lyrics.value == null) {
                                _currentTrack.value?.let { fetchLyrics(it) }
                            }
                        } else if (playbackState == Player.STATE_ENDED) {
                            // Service now handles auto-skip internally for background stability.
                        }
                    }
                })

                // No custom-command queue sync needed — ViewModel writes directly
                // to SharedPlaybackQueue which the service reads from.
                
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
                            val track = database.trackDao().getTrackById(trackId)
                                ?: findTrackInQueue(trackId)
                            if (track != null) {
                                _currentTrack.value = track
                                track.albumArtUri?.let { updateThemeFromArt(it) }
                                fetchLyrics(track)
                            } else {
                                // Fallback: construct minimal track from MediaMetadata
                                val meta = currentItem.mediaMetadata
                                _currentTrack.value = Track(
                                    id = trackId,
                                    title = meta.title?.toString() ?: "Unknown",
                                    artist = meta.artist?.toString() ?: "Unknown",
                                    album = meta.albumTitle?.toString() ?: "",
                                    albumArtUri = meta.artworkUri?.toString(),
                                    duration = _duration.value,
                                    path = "",
                                    isYouTube = true
                                )
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

    private fun fetchHomeContent() {
        viewModelScope.launch {
            _homeSections.value = repository.getHomeFeed()
            fetchMoodRecommendations()
        }
    }

    fun fetchMoodRecommendations() {
        viewModelScope.launch {
            val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            val mood = when (hour) {
                in 5..10 -> "Chill"      // 5 AM to 10:59 AM
                in 11..16 -> "Upbeat"    // 11 AM to 4:59 PM
                in 17..20 -> "Focus"     // 5 PM to 8:59 PM
                else -> "Chill"          // 9 PM to 4:59 AM
            }
            val greeting = when (hour) {
                in 5..10 -> "Good Morning • Chill Mix"
                in 11..16 -> "Good Afternoon • Upbeat Hype"
                in 17..20 -> "Good Evening • Focus Beats"
                else -> "Good Night • Midnight Chill"
            }
            _moodGreeting.value = greeting

            val keywords = when (mood) {
                "Chill" -> listOf("chill", "lofi", "lo-fi", "sleep", "relax", "acoustic", "calm", "slow", "ambient", "soft", "jazz", "piano", "soul")
                "Focus" -> listOf("focus", "study", "beats", "instrumental", "classical", "synthwave", "coding", "ambient", "calm", "relax")
                "Upbeat" -> listOf("upbeat", "dance", "rock", "pop", "workout", "energetic", "gym", "hype", "electronic", "electro", "party", "rap", "hip hop", "techno")
                else -> emptyList()
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
                        "Chill" -> "lofi chill beats"
                        "Focus" -> "synthwave focus study"
                        "Upbeat" -> "upbeat workout energetic pop"
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

    fun fetchLyricsManual(track: Track) {
        fetchLyrics(track)
    }

    private var lyricsJob: kotlinx.coroutines.Job? = null

    private fun fetchLyrics(track: Track) {
        lyricsJob?.cancel()
        lyricsJob = viewModelScope.launch {
            _isLyricsLoading.value = true
            _lyrics.value = null
            _syncedLyrics.value = emptyList()
            // Prioritize SimpMusic API by passing track.id (videoId)
            val response = lyricsRepository.fetchLyrics(track.id, track.artist, track.title, track.duration)
            
            if (coroutineContext[kotlinx.coroutines.Job]?.isActive == true) {
                _lyrics.value = response
                response?.syncedLyrics?.let {
                    _syncedLyrics.value = parseLrc(it)
                }
                _isLyricsLoading.value = false
            }
        }
    }

    private fun parseLrc(lrcContent: String): List<LyricLine> {
        val lines = mutableListOf<LyricLine>()
        // Improved regex to handle multiple timestamps and different ms precisions (e.g. .xx or .xxx)
        val timeRegex = Regex("\\[(\\d+):(\\d{2})(?:[.:](\\d{2,3}))?\\]")
        
        lrcContent.lines().forEach { line ->
            val matches = timeRegex.findAll(line).toList()
            if (matches.isNotEmpty()) {
                val lastMatch = matches.last()
                val text = line.substring(lastMatch.range.last + 1).trim()
                
                // Allow empty text for instrumental breaks if needed, but usually we filter them
                matches.forEach { match ->
                    val min = match.groupValues[1].toLongOrNull() ?: 0L
                    val sec = match.groupValues[2].toLongOrNull() ?: 0L
                    val msStr = match.groupValues[3]
                    val ms = when (msStr.length) {
                        2 -> (msStr.toLongOrNull() ?: 0L) * 10
                        3 -> msStr.toLongOrNull() ?: 0L
                        else -> 0L
                    }
                    val timeMs = (min * 60 + sec) * 1000 + ms
                    lines.add(LyricLine(timeMs, text))
                }
            }
        }
        return lines.sortedBy { it.timeMs }
    }


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

    fun performSearch(query: String, type: String? = null) {
        if (query.isBlank()) {
            clearSearch()
            return
        }

        if (lastSearchQuery == query && lastSearchType == type && 
            (_searchResults.value.isNotEmpty() || _searchAlbums.value.isNotEmpty() || _searchArtists.value.isNotEmpty() || _isSearching.value)) {
            // Unchanged query, skip redundant call to avoid hitting YouTube Music rate limits or cancelling active job
            return
        }
        lastSearchQuery = query
        lastSearchType = type
        
        // Add to Room history
        viewModelScope.launch(Dispatchers.IO) {
            repository.addRecentSearch(query, "Query", null)
        }

        _isSearching.value = true
        _searchResults.value = emptyList()
        _searchAlbums.value = emptyList()
        _searchArtists.value = emptyList()
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
                    _searchAlbums.value = remoteResults.albums
                    _searchArtists.value = remoteResults.artists
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
        if (query.length < 2) {
            _searchSuggestions.value = emptyList()
            return
        }
        suggestionsJob?.cancel()
        suggestionsJob = viewModelScope.launch {
            try {
                val suggestions = repository.getSearchSuggestions(query)
                if (coroutineContext[Job] == suggestionsJob) {
                    _searchSuggestions.value = suggestions
                }
            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    Log.e(TAG, "Suggestions fetch failed for query: $query", e)
                }
            }
        }
    }

    fun clearSearch() {
        lastSearchQuery = null
        lastSearchType = null
        _searchResults.value = emptyList()
        _searchAlbums.value = emptyList()
        _searchArtists.value = emptyList()
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

    fun playQueue(tracks: List<Track>, startIndex: Int = 0, isVideo: Boolean = false) {
        val controller = _controller.value ?: return
        
        _isVideoMode.value = isVideo

        loadTrackJob?.cancel()
        loadTrackJob = viewModelScope.launch {
            _isLoadingTrack.value = true
            
            val initialTrack = tracks.getOrNull(startIndex) ?: return@launch
            val dbTrack = withContext(Dispatchers.IO) { database.trackDao().getTrackById(initialTrack.id) }
            val track = dbTrack ?: initialTrack
            
            _currentTrack.value = track
            _hasTrackLoaded.value = true
            
            _playbackSource.value = when {
                _isSearching.value -> "Search"
                _currentTab.value == HomeTab.Home -> "Home"
                else -> "Library"
            }

            val mappedTracks = withContext(Dispatchers.IO) {
                tracks.map { t -> database.trackDao().getTrackById(t.id) ?: t }
            }
            setQueue(mappedTracks)
            _originalPlaybackQueue.value = mappedTracks
            setIndex(startIndex)

            try {
                val mediaItems = mappedTracks.map { buildMediaItem(it) }
                
                controller.setMediaItems(mediaItems, startIndex, C.TIME_UNSET)
                controller.prepare()
                controller.play()

                if (_endlessMode.value && track.isYouTube) {
                    checkAndFetchRelatedTracks(track.id)
                }

                if (track.isYouTube) {
                    try { repository.saveTrack(track) } catch(e:Exception){ Log.e(TAG, "DB error", e) }
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
        if (clearQueue) {
            playQueue(listOf(track), 0, isVideo = isVideo)
            return
        }
        _isVideoMode.value = isVideo
        
        val controller = _controller.value ?: return

        loadTrackJob?.cancel()
        loadTrackJob = viewModelScope.launch {
            _isLoadingTrack.value = true
            
            // Set current track immediately for instant UI feedback
            val dbTrack = withContext(Dispatchers.IO) { database.trackDao().getTrackById(track.id) }
            val finalTrack = dbTrack ?: track
            _currentTrack.value = finalTrack
            _hasTrackLoaded.value = true
            
            // Set source based on current state
            _playbackSource.value = when {
                _isSearching.value -> "Search"
                _currentTab.value == HomeTab.Home -> "Home"
                else -> "Library"
            }

            setIndex(_playbackQueue.value.indexOfFirst { it.id == finalTrack.id })

            try {
                val mediaItem = buildMediaItem(finalTrack)
                
                // Check if already in controller queue to avoid duplicate loads
                var foundIdx = -1
                for (i in 0 until controller.mediaItemCount) {
                    if (controller.getMediaItemAt(i).mediaId == finalTrack.id) {
                        foundIdx = i
                        break
                    }
                }
                
                if (foundIdx != -1) {
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

                if (finalTrack.isYouTube) {
                    try { repository.saveTrack(finalTrack) } catch(e:Exception){ Log.e(TAG, "DB error", e) }
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
        val controller = _controller.value ?: return
        val updatedQueue = _playbackQueue.value.toMutableList()
        
        var targetIndex = -1
        if (absoluteIndex in 0 until updatedQueue.size && updatedQueue[absoluteIndex].id == track.id) {
            targetIndex = absoluteIndex
        } else {
            // Stale index! Find the track in the upcoming queue.
            val searchStart = _currentQueueIndex.value + 1
            for (idx in searchStart until updatedQueue.size) {
                if (updatedQueue[idx].id == track.id) {
                    targetIndex = idx
                    break
                }
            }
        }
        
        if (targetIndex != -1) {
            updatedQueue.removeAt(targetIndex)
            setQueue(updatedQueue)
            
            val updatedOriginal = _originalPlaybackQueue.value.toMutableList()
            val origIdx = updatedOriginal.indexOfFirst { it.id == track.id }
            if (origIdx != -1) {
                updatedOriginal.removeAt(origIdx)
                _originalPlaybackQueue.value = updatedOriginal
            }
            
            if (targetIndex < controller.mediaItemCount) {
                controller.removeMediaItem(targetIndex)
            }
        }
    }

    fun moveQueueItem(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            val controller = _controller.value ?: return@launch
            val queue = _playbackQueue.value.toMutableList()
            if (fromIndex in 0 until queue.size && toIndex in 0 until queue.size) {
                val item = queue.removeAt(fromIndex)
                queue.add(toIndex, item)
                setQueue(queue)
                
                if (fromIndex < controller.mediaItemCount && toIndex < controller.mediaItemCount) {
                    controller.moveMediaItem(fromIndex, toIndex)
                }
                
                if (!_shuffleMode.value) {
                    val original = _originalPlaybackQueue.value.toMutableList()
                    if (fromIndex in 0 until original.size && toIndex in 0 until original.size) {
                        val origItem = original.removeAt(fromIndex)
                        original.add(toIndex, origItem)
                        _originalPlaybackQueue.value = original
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
            val updatedQueue = _playbackQueue.value.toMutableList()
            val updatedOriginal = _originalPlaybackQueue.value.toMutableList()
            
            if (tracks.isNotEmpty()) {
                updatedQueue.addAll(tracks)
                updatedOriginal.addAll(tracks)
                setQueue(updatedQueue)
                _originalPlaybackQueue.value = updatedOriginal
                
                _controller.value?.let { controller ->
                    val mediaItems = tracks.map { buildMediaItem(it) }
                    controller.addMediaItems(mediaItems)
                }
                
                if (!autoGenerated) {
                    val message = if (tracks.size == 1) "Added to queue" else "Added ${tracks.size} tracks to queue"
                    Toast.makeText(getApplication(), message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun playNext(track: Track) {
        viewModelScope.launch {
            val controller = _controller.value ?: return@launch
            // Insert after current in our internal queue
            val updatedQueue = _playbackQueue.value.toMutableList()
            val updatedOriginal = _originalPlaybackQueue.value.toMutableList()
            val insertIdx = _currentQueueIndex.value + 1
            updatedQueue.add(insertIdx.coerceIn(0, updatedQueue.size), track)
            updatedOriginal.add(insertIdx.coerceIn(0, updatedOriginal.size), track)
            setQueue(updatedQueue)
            _originalPlaybackQueue.value = updatedOriginal
            
            controller.addMediaItem(insertIdx.coerceIn(0, controller.mediaItemCount), buildMediaItem(track))
            
            Toast.makeText(getApplication(), "Playing next", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Skip to the next track using ExoPlayer natively.
     */
    fun skipNext() {
        val controller = _controller.value ?: return
        if (controller.hasNextMediaItem() || _repeatMode.value == Player.REPEAT_MODE_ALL) {
            controller.seekToNextMediaItem()
        } else {
            runOnMain { Toast.makeText(getApplication(), "End of Queue", Toast.LENGTH_SHORT).show() }
        }
    }

    /**
     * Skip to the previous track using ExoPlayer natively.
     */
    fun skipPrevious() {
        val controller = _controller.value ?: return
        if (controller.currentPosition > 3000) {
            controller.seekTo(0)
            return
        }
        controller.seekToPreviousMediaItem()
    }

    /**
     * Skip to a specific track using ExoPlayer natively.
     */
    fun skipToQueueItem(index: Int) {
        val controller = _controller.value ?: return
        if (index >= 0 && index < controller.mediaItemCount) {
            controller.seekToDefaultPosition(index)
        }
    }

    /** Build a MediaItem from a track, using yt:// protocol for YouTube tracks */
    private fun buildMediaItem(track: Track): MediaItem {
        val isLocal = isDownloaded(track.id)
        val uri = if (isLocal) {
            val context = getApplication<Application>().applicationContext
            val localFile = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "${track.id}.m4a")
            localFile.absolutePath
        } else if (track.isYouTube) {
            if (_isVideoMode.value) "ytvideo://${track.id}" else "yt://${track.id}"
        } else {
            track.path
        }
        return MediaItem.Builder()
            .setMediaId(track.id)
            .setUri(uri.toUri())
            .setMediaMetadata(MediaMetadata.Builder()
                .setTitle(track.title)
                .setArtist(track.artist)
                .setArtworkUri(track.albumArtUri?.toUri())
                .build())
            .build()
    }

    fun setVideoMode(isVideo: Boolean) {
        if (_isVideoMode.value == isVideo) return
        _isVideoMode.value = isVideo
        val track = _currentTrack.value ?: return
        val controller = _controller.value ?: return
        val currentPosition = controller.currentPosition
        val currentPlayWhenReady = controller.playWhenReady
        
        // Only replace media item if it's YouTube, local files are the same
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
                        .allowHardware(false) // Required for Palette
                        .build()
                    
                    val result = loader.execute(request)
                    val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
                    
                    if (bitmap != null) {
                        val palette = Palette.from(bitmap).generate()
                        val dominant = palette.getDominantColor(0xFF191C19.toInt())
                        val accent = palette.getVibrantColor(
                            palette.getMutedColor(0xFF79DA9F.toInt())
                        )
                        
                        withContext(Dispatchers.Main) {
                            _dominantColor.value = Color(dominant)
                            _accentColor.value = Color(accent)
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
        _crossfadeEnabled.value = !_crossfadeEnabled.value
    }

    private fun fetchSuggestions(seedTrack: Track) {
        viewModelScope.launch {
            try {
                // Get related tracks for the user's most played song
                val related = repository.getYouTubeRelatedTracks(seedTrack.id)
                _suggestedTracks.value = related.take(10)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch suggestions", e)
            }
        }
    }

    fun fetchVideoInfo(videoId: String) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _isVideoInfoLoading.value = true
            _videoInfo.value = null
            try {
                val info = repository.fetchVideoInfo(videoId)
                _videoInfo.value = info
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
                        val destFile = File(context.filesDir, "user_avatar.jpg")
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


    fun setEqBand(band: Int, level: Int) {
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
        _eqEnabled.value = newValue
        sendCustomCommand("SET_EQ_ENABLED", Bundle().apply {
            putBoolean("enabled", newValue)
        })
    }

    fun setVirtualizerStrength(strength: Int) {
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
                val theme = when (mood) {
                    "Chill" -> com.google.gson.Gson().toJson(com.example.ecodot.util.CustomCover("#FFF857A6", "#FFFF5858", false, listOf("daily", mood.lowercase())))
                    "Focus" -> com.google.gson.Gson().toJson(com.example.ecodot.util.CustomCover("#FF00C9FF", "#FF92FE9D", false, listOf("daily", mood.lowercase())))
                    "Workout" -> com.google.gson.Gson().toJson(com.example.ecodot.util.CustomCover("#FFFF007F", "#FF3F00FF", false, listOf("daily", mood.lowercase())))
                    "Party" -> com.google.gson.Gson().toJson(com.example.ecodot.util.CustomCover("#FF7F00FF", "#FFE100FF", false, listOf("daily", mood.lowercase())))
                    "Sleep" -> com.google.gson.Gson().toJson(com.example.ecodot.util.CustomCover("#FF0B3C5D", "#FF328CC1", false, listOf("daily", mood.lowercase())))
                    "Study" -> com.google.gson.Gson().toJson(com.example.ecodot.util.CustomCover("#FF1D2731", "#FF007A87", false, listOf("daily", mood.lowercase())))
                    "Upbeat" -> com.google.gson.Gson().toJson(com.example.ecodot.util.CustomCover("#FFF9D423", "#FFFF4E50", false, listOf("daily", mood.lowercase())))
                    "Romance" -> com.google.gson.Gson().toJson(com.example.ecodot.util.CustomCover("#FFFF2658", "#FF86002A", false, listOf("daily", mood.lowercase())))
                    "Gaming" -> com.google.gson.Gson().toJson(com.example.ecodot.util.CustomCover("#FF00F2FE", "#FF4FACFE", false, listOf("daily", mood.lowercase())))
                    "Acoustic" -> com.google.gson.Gson().toJson(com.example.ecodot.util.CustomCover("#FFB8A9C9", "#FF5B92E5", false, listOf("daily", mood.lowercase())))
                    else -> com.google.gson.Gson().toJson(com.example.ecodot.util.CustomCover("#FF1DB954", "#FF070708", false, listOf("daily")))
                }

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
        val controller = _controller.value ?: return
        
        _isVideoMode.value = isVideo
        
        viewModelScope.launch {
            // 1. Update internal queue state first
            setQueue(tracks)
            _originalPlaybackQueue.value = tracks
            setIndex(startIndex)
            
            // 2. Set all tracks into the ExoPlayer queue
            val mediaItems = tracks.map { buildMediaItem(it) }
            controller.setMediaItems(mediaItems, startIndex, androidx.media3.common.C.TIME_UNSET)
            controller.prepare()
            controller.play()
        }
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

