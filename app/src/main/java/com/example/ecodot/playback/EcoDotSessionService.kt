package com.example.ecodot.playback

import android.content.Intent
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.*
import com.example.ecodot.R
import com.example.ecodot.data.local.database.EcoDotDatabase
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.data.repository.MusicRepository
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.firstOrNull
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import android.os.Environment
import java.io.File
import androidx.media3.exoplayer.source.MediaSourceFactory

class EcoDotSessionService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var effectManager: AudioEffectManager? = null
    private lateinit var repository: MusicRepository

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(
        Dispatchers.Main + serviceJob +
        CoroutineExceptionHandler { _, e ->
            Log.e(TAG, "Coroutine error: ${e.message}", e)
        }
    )

    private var activePlayerIndex = 0
    private val players = arrayOfNulls<ExoPlayer>(2)
    private var crossfadeMonitorJob: Job? = null
    private var isCrossfading = false
    private lateinit var prefsManager: com.example.ecodot.data.local.prefs.UserPreferencesManager

    // ── URL Cache: Prevents runBlocking from hitting network on every buffer refill ──
    private data class CachedUrl(val url: String, val timestamp: Long)
    private val urlCache = java.util.concurrent.ConcurrentHashMap<String, CachedUrl>()
    private val URL_CACHE_TTL_MS = 3_600_000L // 1 hour

    // ── Sleep Timer State ──
    private var sleepTimerJob: Job? = null
    private var isSleepTimerAtEndOfTrack = false

    // ── Synced Lyrics Notification Updater State ──
    private var currentPlayingTrackId: String? = null
    private var originalArtistName: String = ""
    private var originalTitle: String = ""
    private var lyricsUpdaterJob: Job? = null
    private var currentLyricsList: List<com.example.ecodot.ui.viewmodel.LyricLine> = emptyList()
    private var lastLyricLineText: String = ""

    companion object {
        private const val TAG = "EcoDotSessionService"

        const val ACTION_TOGGLE_LIKE     = "ACTION_TOGGLE_LIKE"
        const val ACTION_TOGGLE_SHUFFLE  = "ACTION_TOGGLE_SHUFFLE"
        const val ACTION_CYCLE_REPEAT    = "ACTION_CYCLE_REPEAT"
        const val ACTION_FORWARD_10      = "ACTION_FORWARD_10"
        const val ACTION_SET_EQ_BAND     = "SET_EQ_BAND"
        const val ACTION_SET_VIRTUALIZER = "SET_VIRTUALIZER"
        const val ACTION_SET_EQ_ENABLED  = "SET_EQ_ENABLED"
        const val ACTION_UPDATE_QUEUE    = "UPDATE_QUEUE"
        const val ACTION_UPDATE_INDEX    = "UPDATE_INDEX"
        const val ACTION_SET_SLEEP_TIMER = "SET_SLEEP_TIMER"
        const val ACTION_CANCEL_SLEEP_TIMER = "CANCEL_SLEEP_TIMER"
    }

    override fun onCreate() {
        super.onCreate()
        val db = EcoDotDatabase.getInstance(this)
        repository = MusicRepository(this, db.trackDao(), db.historyDao(),
            db.profileDao(), db.playlistDao(), db.recentSearchDao(), db.followedArtistDao())

        prefsManager = com.example.ecodot.data.local.prefs.UserPreferencesManager(this)

        val resolvingDataSourceFactory = ResolvingDataSource.Factory(
            DefaultDataSource.Factory(this),
            object : ResolvingDataSource.Resolver {
                override fun resolveDataSpec(dataSpec: DataSpec): DataSpec {
                    val uri = dataSpec.uri
                    if (uri.scheme == "yt" || uri.scheme == "ytvideo") {
                        val videoId = uri.host ?: return dataSpec
                        val isVideo = uri.scheme == "ytvideo"
                        
                        // Check local download first (instant, no network)
                        if (!isVideo) {
                            val localFile = File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "$videoId.m4a")
                            if (localFile.exists()) {
                                return dataSpec.buildUpon().setUri(android.net.Uri.fromFile(localFile)).build()
                            }
                        }
                        
                        // Check in-memory URL cache first (avoids blocking network call)
                        val cacheKey = "${videoId}_${isVideo}"
                        val cached = urlCache[cacheKey]
                        if (cached != null && System.currentTimeMillis() - cached.timestamp < URL_CACHE_TTL_MS) {
                            return dataSpec.buildUpon().setUri(android.net.Uri.parse(cached.url)).build()
                        }
                        
                        // Cache miss — resolve with timeout to prevent indefinite thread blocking
                        val streamUrl = runBlocking(Dispatchers.IO) {
                            withTimeoutOrNull(8000L) {
                                repository.getYouTubeStreamUrl(
                                    videoId = videoId,
                                    quality = prefsManager.audioQuality,
                                    videoQuality = prefsManager.videoQuality,
                                    isVideo = isVideo
                                )
                            }
                        }
                        if (streamUrl != null) {
                            urlCache[cacheKey] = CachedUrl(streamUrl, System.currentTimeMillis())
                            return dataSpec.buildUpon().setUri(android.net.Uri.parse(streamUrl)).build()
                        }
                    }
                    return dataSpec
                }
            }
        )

        @OptIn(UnstableApi::class)
        val cacheDataSourceFactory = androidx.media3.datasource.cache.CacheDataSource.Factory()
            .setCache(CacheManager.getCache(this))
            .setUpstreamDataSourceFactory(resolvingDataSourceFactory)
            .setFlags(androidx.media3.datasource.cache.CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        @OptIn(UnstableApi::class)
        val mediaSourceFactory = DefaultMediaSourceFactory(this)
            .setDataSourceFactory(cacheDataSourceFactory)

        val sharedSessionId = C.generateAudioSessionIdV21(this)
        players[0] = buildPlayer(mediaSourceFactory, sharedSessionId)
        players[1] = buildPlayer(mediaSourceFactory, sharedSessionId)

        effectManager = AudioEffectManager(this, players[0]!!.audioSessionId)

        mediaSession = MediaSession.Builder(this, players[0]!!)
            .setCallback(CommandCallback())
            .build()

        updateNotification()
        startCrossfadeMonitor()

        val filter = android.content.IntentFilter().apply {
            addAction(android.content.Intent.ACTION_SCREEN_OFF)
            addAction(android.content.Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(screenStateReceiver, filter)
    }

    @OptIn(UnstableApi::class)
    private fun buildPlayer(mediaSourceFactory: MediaSourceFactory, sessionId: Int): ExoPlayer {
        val loadControl = androidx.media3.exoplayer.DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                30_000, // Increased min buffer to 30s to withstand CPU spikes
                60_000, // Increased max buffer to 60s
                5_000,  // Increased playback start buffer to 5s for smoother starts
                8_000   // Increased rebuffer buffer to 8s
            )
            .build()

        return ExoPlayer.Builder(this)
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(loadControl)
            .setWakeMode(C.WAKE_MODE_NETWORK) // Prevent CPU/WiFi sleep in background
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build().apply {
                setAudioSessionId(sessionId)
                addListener(object : Player.Listener {
                    override fun onAudioSessionIdChanged(audioSessionId: Int) {
                        if (players[activePlayerIndex] == null || players[activePlayerIndex] == this@apply) {
                            effectManager?.updateAudioSessionId(audioSessionId)
                        }
                    }
                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        if (players[activePlayerIndex] == this@apply) {
                            val trackId = mediaItem?.mediaId
                            if (trackId != currentPlayingTrackId) {
                                currentPlayingTrackId = trackId
                                isCrossfading = false // Reset lock on transition
                                updateNotification()

                                // Load and inject artwork bytes for full-bleed notification
                                mediaItem?.let { loadArtworkBytesAndInject(it) }

                                // Load lyrics and reset state for notification updater
                                if (mediaItem != null) {
                                    val title = mediaItem.mediaMetadata.title?.toString() ?: ""
                                    val artist = mediaItem.mediaMetadata.artist?.toString() ?: ""
                                    originalArtistName = artist
                                    originalTitle = title
                                    loadLyricsForUpdater(
                                        trackId = mediaItem.mediaId,
                                        title = title,
                                        artist = artist,
                                        duration = duration
                                    )
                                }

                                // Prefetch the next track in queue to avoid runBlocking network delays during transition
                                val nextIndex = currentMediaItemIndex + 1
                                if (nextIndex < mediaItemCount) {
                                    val nextItem = getMediaItemAt(nextIndex)
                                    preFetchStreamUrl(nextItem.mediaId)
                                }

                                // Check and handle sleep timer end of track transition
                                if (isSleepTimerAtEndOfTrack) {
                                    pause()
                                    isSleepTimerAtEndOfTrack = false
                                    val prefs = getSharedPreferences("sleep_timer_prefs", android.content.Context.MODE_PRIVATE)
                                    prefs.edit().clear().apply()
                                    Log.d(TAG, "Sleep Timer: Paused playback at the end of track.")
                                }
                            }
                        }
                    }
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        if (players[activePlayerIndex] == this@apply) {
                            if (isPlaying) {
                                startLyricsNotificationUpdater()
                            } else {
                                lyricsUpdaterJob?.cancel()
                                lyricsUpdaterJob = null
                            }
                        }
                    }
                    override fun onPlaybackStateChanged(state: Int) {
                        if (players[activePlayerIndex] == this@apply) {
                            updateNotification()
                        }
                    }
                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        Log.e(TAG, "Player error: ${error.message}")
                    }
                    override fun onShuffleModeEnabledChanged(enabled: Boolean) = updateNotification()
                    override fun onRepeatModeChanged(mode: Int) = updateNotification()
                })
            }
    }

    private fun preFetchStreamUrl(videoId: String) {
        val cacheKey = "${videoId}_false"
        if (urlCache.containsKey(cacheKey)) return // Already cached

        serviceScope.launch(Dispatchers.IO) {
            try {
                val streamUrl = repository.getYouTubeStreamUrl(
                    videoId = videoId,
                    quality = prefsManager.audioQuality,
                    videoQuality = prefsManager.videoQuality,
                    isVideo = false
                )
                if (streamUrl != null) {
                    urlCache[cacheKey] = CachedUrl(streamUrl, System.currentTimeMillis())
                    Log.d(TAG, "Pre-fetched stream URL for next track: $videoId")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to pre-fetch stream URL for: $videoId", e)
            }
        }
    }

    private var lastPreCachedTrackId: String? = null

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun preCacheTrack(videoId: String) {
        if (lastPreCachedTrackId == videoId) return
        lastPreCachedTrackId = videoId

        serviceScope.launch(Dispatchers.IO) {
            try {
                val cacheKey = "${videoId}_false"
                var streamUrl = urlCache[cacheKey]?.url
                if (streamUrl == null) {
                    streamUrl = repository.getYouTubeStreamUrl(
                        videoId = videoId,
                        quality = prefsManager.audioQuality,
                        videoQuality = prefsManager.videoQuality,
                        isVideo = false
                    )
                    if (streamUrl != null) {
                        urlCache[cacheKey] = CachedUrl(streamUrl, System.currentTimeMillis())
                    }
                }

                if (streamUrl != null) {
                    Log.d(TAG, "Pre-caching bytes for next track: $videoId")
                    val cache = CacheManager.getCache(this@EcoDotSessionService)
                    val upstream = androidx.media3.datasource.DefaultHttpDataSource.Factory()
                        .setAllowCrossProtocolRedirects(true)
                        .createDataSource()
                    
                    val cacheDataSource = androidx.media3.datasource.cache.CacheDataSource(
                        cache,
                        upstream,
                        0
                    )
                    
                    val dataSpec = androidx.media3.datasource.DataSpec.Builder()
                        .setUri(android.net.Uri.parse(streamUrl))
                        .setLength(1024 * 1024 * 3 / 2) // Cache first 1.5 MB
                        .build()

                    val cacheWriter = androidx.media3.datasource.cache.CacheWriter(
                        cacheDataSource,
                        dataSpec,
                        null,
                        null
                    )
                    cacheWriter.cache()
                    Log.d(TAG, "Finished pre-caching 1.5MB for track: $videoId")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to pre-cache track: $videoId", e)
            }
        }
    }

    private fun startCrossfadeMonitor() {
        crossfadeMonitorJob?.cancel()
        crossfadeMonitorJob = serviceScope.launch {
            while (isActive) {
                delay(500)

                val activePlayer = players[activePlayerIndex] ?: continue
                if (activePlayer.playWhenReady && activePlayer.playbackState == Player.STATE_READY) {
                    val duration = activePlayer.duration
                    val currentPos = activePlayer.currentPosition
                    if (duration != C.TIME_UNSET && currentPos != C.TIME_UNSET) {
                        val remaining = duration - currentPos

                        // 1. Trigger pre-caching 15 seconds before the track ends
                        if (remaining in 1000L..16000L) {
                            val nextWindowIndex = activePlayer.currentMediaItemIndex + 1
                            if (nextWindowIndex < activePlayer.mediaItemCount) {
                                val nextItem = activePlayer.getMediaItemAt(nextWindowIndex)
                                preCacheTrack(nextItem.mediaId)
                            }
                        }

                        // 2. Trigger crossfade if enabled and not already crossfading
                        if (prefsManager.crossfadeEnabled && !isCrossfading) {
                            val crossfadeMs = prefsManager.crossfadeDuration * 1000L
                            if (remaining > 0 && remaining <= crossfadeMs && currentPos > duration / 2 && currentPos > 5000L && activePlayer.hasNextMediaItem()) {
                                triggerCrossfade(activePlayer, crossfadeMs)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun triggerCrossfade(oldPlayer: ExoPlayer, durationMs: Long) {
        isCrossfading = true
        val nextIndex = (activePlayerIndex + 1) % 2
        val newPlayer = players[nextIndex] ?: return

        val nextWindowIndex = oldPlayer.currentMediaItemIndex + 1
        val items = mutableListOf<MediaItem>()
        for (i in nextWindowIndex until oldPlayer.mediaItemCount) {
            items.add(oldPlayer.getMediaItemAt(i))
        }

        if (items.isEmpty()) {
            isCrossfading = false
            return
        }

        newPlayer.setMediaItems(items)
        newPlayer.prepare()
        newPlayer.volume = 0f

        serviceScope.launch {
            // Buffer wait
            val startTime = System.currentTimeMillis()
            while (newPlayer.playbackState != Player.STATE_READY) {
                delay(50)
                if (System.currentTimeMillis() - startTime > 8000) {
                    isCrossfading = false
                    return@launch
                }
            }

            if (oldPlayer.playbackState == Player.STATE_ENDED || !oldPlayer.playWhenReady) {
                isCrossfading = false
                return@launch
            }

            newPlayer.playWhenReady = true
            
            val safeFrom = nextWindowIndex.coerceAtMost(oldPlayer.mediaItemCount)
            if (safeFrom < oldPlayer.mediaItemCount) {
                oldPlayer.removeMediaItems(safeFrom, oldPlayer.mediaItemCount)
            }
            
            mediaSession?.player = newPlayer
            activePlayerIndex = nextIndex
            updateNotification()
            effectManager?.updateAudioSessionId(newPlayer.audioSessionId)

            val steps = 50
            val stepDelay = durationMs / steps
            for (i in 1..steps) {
                if (!isActive) break
                
                // Sync pause states
                if (!newPlayer.playWhenReady && oldPlayer.playWhenReady) {
                    oldPlayer.pause()
                } else if (newPlayer.playWhenReady && !oldPlayer.playWhenReady) {
                    oldPlayer.play()
                }

                val progress = i.toFloat() / steps
                newPlayer.volume = progress
                oldPlayer.volume = 1f - progress
                delay(stepDelay)
            }
            
            oldPlayer.playWhenReady = false
            oldPlayer.stop()
            oldPlayer.clearMediaItems()
            oldPlayer.volume = 1f
            isCrossfading = false
        }
    }

    override fun onGetSession(info: MediaSession.ControllerInfo) = mediaSession

    override fun onDestroy() {
        try {
            unregisterReceiver(screenStateReceiver)
        } catch (e: Exception) {
            // Ignore
        }
        serviceJob.cancel()
        players.forEach { it?.run { release() } }
        mediaSession?.release()
        effectManager?.release()
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val p = mediaSession?.player
        if (prefsManager.killServiceOnExit) {
            p?.pause()
            p?.stop()
            stopSelf()
        } else if (p == null || !p.playWhenReady || p.mediaItemCount == 0) {
            stopSelf()
        }
    }

    private fun updateNotification() {
        val session = mediaSession ?: return
        val player  = session.player
        val mediaId = player.currentMediaItem?.mediaId

        serviceScope.launch {
            val liked = mediaId?.let { repository.isTrackLikedSuspended(it) } ?: false
            session.setCustomLayout(buildLayout(player, liked, mediaId != null))
        }
    }

    private fun buildLayout(player: Player, liked: Boolean, hasMedia: Boolean) =
        listOf(
            CommandButton.Builder()
                .setSessionCommand(SessionCommand(ACTION_TOGGLE_LIKE, Bundle.EMPTY))
                .setIconResId(if (liked) R.drawable.ic_heart_filled else R.drawable.ic_heart)
                .setDisplayName("Like").setEnabled(hasMedia).build(),

            CommandButton.Builder()
                .setSessionCommand(SessionCommand(ACTION_TOGGLE_SHUFFLE, Bundle.EMPTY))
                .setIconResId(R.drawable.ic_shuffle)
                .setDisplayName("Shuffle").setEnabled(true).build(),

            CommandButton.Builder()
                .setSessionCommand(SessionCommand(ACTION_CYCLE_REPEAT, Bundle.EMPTY))
                .setIconResId(
                    if (player.repeatMode == Player.REPEAT_MODE_ONE)
                        R.drawable.ic_repeat_one else R.drawable.ic_repeat
                )
                .setDisplayName("Repeat").setEnabled(true).build(),

            CommandButton.Builder()
                .setSessionCommand(SessionCommand(ACTION_FORWARD_10, Bundle.EMPTY))
                .setIconResId(R.drawable.ic_forward_10)
                .setDisplayName("Forward 10s").setEnabled(hasMedia).build()
        )

    private inner class CommandCallback : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val sessionCmds = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                .add(SessionCommand(ACTION_TOGGLE_LIKE,     Bundle.EMPTY))
                .add(SessionCommand(ACTION_TOGGLE_SHUFFLE,  Bundle.EMPTY))
                .add(SessionCommand(ACTION_CYCLE_REPEAT,    Bundle.EMPTY))
                .add(SessionCommand(ACTION_FORWARD_10,      Bundle.EMPTY))
                .add(SessionCommand(ACTION_SET_EQ_BAND,     Bundle.EMPTY))
                .add(SessionCommand(ACTION_SET_VIRTUALIZER, Bundle.EMPTY))
                .add(SessionCommand(ACTION_SET_EQ_ENABLED,  Bundle.EMPTY))
                .add(SessionCommand(ACTION_UPDATE_QUEUE,    Bundle.EMPTY))
                .add(SessionCommand(ACTION_UPDATE_INDEX,    Bundle.EMPTY))
                .add(SessionCommand(ACTION_SET_SLEEP_TIMER, Bundle.EMPTY))
                .add(SessionCommand(ACTION_CANCEL_SLEEP_TIMER, Bundle.EMPTY))
                .build()

            val playerCmds = MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS.buildUpon()
                .add(Player.COMMAND_SEEK_TO_NEXT)
                .add(Player.COMMAND_SEEK_TO_PREVIOUS)
                .add(Player.COMMAND_SEEK_FORWARD)
                .add(Player.COMMAND_SEEK_BACK)
                .add(Player.COMMAND_SET_SHUFFLE_MODE)
                .add(Player.COMMAND_SET_REPEAT_MODE)
                .build()

            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(sessionCmds)
                .setAvailablePlayerCommands(playerCmds)
                .build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            command: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            Log.d(TAG, "onCustomCommand: action=${command.customAction}")
            when (command.customAction) {
                ACTION_TOGGLE_LIKE    -> serviceScope.launch { toggleLike(session.player) }
                ACTION_TOGGLE_SHUFFLE -> session.player.let {
                    it.shuffleModeEnabled = !it.shuffleModeEnabled; updateNotification()
                }
                ACTION_CYCLE_REPEAT   -> session.player.let {
                    it.repeatMode = when (it.repeatMode) {
                        Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                        Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                        else                   -> Player.REPEAT_MODE_OFF
                    }; updateNotification()
                }
                ACTION_FORWARD_10     -> session.player.let {
                    Log.d(TAG, "ACTION_FORWARD_10 received, pos=${it.currentPosition}")
                    val target = it.currentPosition + 10_000L
                    it.seekTo(if (it.duration != C.TIME_UNSET) target.coerceAtMost(it.duration) else target)
                }
                ACTION_SET_EQ_BAND    -> effectManager?.setBandLevel(
                    args.getInt("band").toShort(), args.getInt("level").toShort())
                ACTION_SET_VIRTUALIZER -> effectManager?.setVirtualizerStrength(
                    args.getInt("strength").toShort())
                ACTION_SET_EQ_ENABLED  -> effectManager?.setEnabled(args.getBoolean("enabled"))
                ACTION_UPDATE_QUEUE, ACTION_UPDATE_INDEX -> { /* no-op */ }
                ACTION_SET_SLEEP_TIMER -> {
                    val minutes = args.getInt("minutes", 0)
                    val endOfTrack = args.getBoolean("endOfTrack", false)
                    if (endOfTrack) {
                        enableSleepTimerEndOfTrack()
                    } else if (minutes > 0) {
                        startSleepTimer(minutes)
                    }
                }
                ACTION_CANCEL_SLEEP_TIMER -> {
                    cancelSleepTimer()
                }
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }

    private suspend fun toggleLike(player: Player) {
        val trackId = player.currentMediaItem?.mediaId ?: return
        val item = player.currentMediaItem ?: return
        withContext(Dispatchers.IO) {
            val db = EcoDotDatabase.getInstance(this@EcoDotSessionService)
            var track = db.trackDao().getTrackById(trackId)
            if (track == null) {
                track = Track(
                    id = trackId,
                    title = originalTitle.ifEmpty { item.mediaMetadata.title?.toString() ?: "Unknown" },
                    artist = originalArtistName.ifEmpty { item.mediaMetadata.artist?.toString() ?: "Unknown" },
                    album = item.mediaMetadata.albumTitle?.toString() ?: "",
                    albumArtUri = item.mediaMetadata.artworkUri?.toString(),
                    duration = player.duration.coerceAtLeast(0L),
                    path = "",
                    isYouTube = item.localConfiguration?.uri?.scheme == "yt" || 
                                item.localConfiguration?.uri?.scheme == "ytvideo" || 
                                item.localConfiguration?.uri?.toString()?.contains("youtube") == true
                )
            }
            repository.toggleTrackLike(track)
        }
        
        withContext(Dispatchers.Main) {
            val currentIndex = player.currentMediaItemIndex
            if (currentIndex != androidx.media3.common.C.INDEX_UNSET) {
                val currentMediaItem = player.currentMediaItem
                if (currentMediaItem != null) {
                    val extras = currentMediaItem.mediaMetadata.extras ?: Bundle.EMPTY
                    val newExtras = Bundle(extras)
                    newExtras.putLong("like_toggle_ts", System.currentTimeMillis())
                    
                    val updatedMetadata = currentMediaItem.mediaMetadata.buildUpon()
                        .setExtras(newExtras)
                        .build()
                    val updatedMediaItem = currentMediaItem.buildUpon()
                        .setMediaMetadata(updatedMetadata)
                        .build()
                    player.replaceMediaItem(currentIndex, updatedMediaItem)
                }
            }
            updateNotification()
        }
    }

    private fun startSleepTimer(minutes: Int) {
        cancelSleepTimer() // Stop any previous timer
        
        val durationMs = minutes * 60 * 1000L
        val endTimestamp = System.currentTimeMillis() + durationMs

        // Store end time in shared preferences for UI syncing
        val prefs = getSharedPreferences("sleep_timer_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit()
            .putLong("sleep_timer_end_timestamp", endTimestamp)
            .putBoolean("sleep_timer_end_of_track", false)
            .apply()

        Log.d(TAG, "Sleep Timer: Started for $minutes minutes (ends at $endTimestamp)")

        sleepTimerJob = serviceScope.launch {
            // Wait until 10 seconds before the target end time
            val fadeStart = endTimestamp - 10_000L
            while (System.currentTimeMillis() < fadeStart) {
                delay(1000)
            }

            // Smooth fade-out over 10 seconds
            val activePlayer = players[activePlayerIndex]
            val initialVolume = activePlayer?.volume ?: 1f
            val steps = 20
            for (i in 1..steps) {
                if (!isActive) break
                val progress = i.toFloat() / steps
                activePlayer?.volume = initialVolume * (1f - progress)
                delay(500)
            }

            // Pause playback and restore volume state for next playback
            activePlayer?.pause()
            activePlayer?.volume = 1f
            
            // Clean up states
            prefs.edit().clear().apply()
            sleepTimerJob = null
            Log.d(TAG, "Sleep Timer: Timer expired, playback paused.")
        }
    }

    private fun enableSleepTimerEndOfTrack() {
        cancelSleepTimer() // Stop any standard timer
        isSleepTimerAtEndOfTrack = true
        
        val prefs = getSharedPreferences("sleep_timer_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit()
            .putLong("sleep_timer_end_timestamp", 0L)
            .putBoolean("sleep_timer_end_of_track", true)
            .apply()
            
        Log.d(TAG, "Sleep Timer: Set to end of track.")
    }

    private fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        isSleepTimerAtEndOfTrack = false

        // Clear shared preferences
        val prefs = getSharedPreferences("sleep_timer_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().clear().apply()

        // Restore volume just in case it was fading out
        players.forEach { it?.volume = 1f }
        Log.d(TAG, "Sleep Timer: Timer cancelled.")
    }

    private val screenStateReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context, intent: android.content.Intent) {
            if (intent.action == android.content.Intent.ACTION_SCREEN_OFF || intent.action == android.content.Intent.ACTION_SCREEN_ON) {
                val player = players[activePlayerIndex] ?: return
                if (prefsManager.lockscreenLyricsEnabled && player.isPlaying) {
                    val activityIntent = android.content.Intent(context, com.example.ecodot.ui.screens.LockscreenLyricsActivity::class.java).apply {
                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    }
                    context.startActivity(activityIntent)
                }
            }
        }
    }

    private fun loadArtworkBytesAndInject(mediaItem: MediaItem) {
        val artUri = mediaItem.mediaMetadata.artworkUri ?: return
        
        // Skip if artwork data is already injected
        if (mediaItem.mediaMetadata.artworkData != null) return

        serviceScope.launch(Dispatchers.IO) {
            try {
                val loader = coil.Coil.imageLoader(this@EcoDotSessionService)
                val request = coil.request.ImageRequest.Builder(this@EcoDotSessionService)
                    .data(artUri)
                    .allowHardware(false) // Required to read bitmap pixels / compress
                    .build()
                val result = loader.execute(request)
                if (result is coil.request.SuccessResult) {
                    val bitmap = (result.drawable as android.graphics.drawable.BitmapDrawable).bitmap
                    val stream = java.io.ByteArrayOutputStream()
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 80, stream)
                    val bytes = stream.toByteArray()
                    
                    withContext(Dispatchers.Main) {
                        val activePlayer = players[activePlayerIndex]
                        val currentIndex = activePlayer?.currentMediaItemIndex
                        if (activePlayer != null && currentIndex != null && currentIndex != androidx.media3.common.C.INDEX_UNSET) {
                            val currentMediaItem = activePlayer.currentMediaItem
                            if (currentMediaItem != null && currentMediaItem.mediaId == mediaItem.mediaId) {
                                val updatedMetadata = currentMediaItem.mediaMetadata.buildUpon()
                                    .setArtworkData(bytes, androidx.media3.common.MediaMetadata.PICTURE_TYPE_FRONT_COVER)
                                    .build()
                                val updatedMediaItem = currentMediaItem.buildUpon()
                                    .setMediaMetadata(updatedMetadata)
                                    .build()
                                
                                activePlayer.replaceMediaItem(currentIndex, updatedMediaItem)
                                Log.d(TAG, "Successfully injected artwork bytes for full-bleed notification: ${mediaItem.mediaId}")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load artwork bytes for notification: ${mediaItem.mediaId}", e)
            }
        }
    }

    private fun loadLyricsForUpdater(trackId: String, title: String, artist: String, duration: Long) {
        currentLyricsList = emptyList()
        lastLyricLineText = ""
        serviceScope.launch(Dispatchers.IO) {
            try {
                val lyricsRepository = com.example.ecodot.data.remote.LyricsRepository()
                val response = lyricsRepository.fetchLyrics(trackId, artist, title, duration)
                val rawLrc = response?.syncedLyrics
                if (rawLrc != null) {
                    val timeRegex = Regex("\\[(\\d+):(\\d{2})(?:[.:](\\d{2,3}))?\\]")
                    val parsed = mutableListOf<com.example.ecodot.ui.viewmodel.LyricLine>()
                    rawLrc.lines().forEach { line ->
                        val matches = timeRegex.findAll(line).toList()
                        if (matches.isNotEmpty()) {
                            val lastMatch = matches.last()
                            val text = line.substring(lastMatch.range.last + 1).trim()
                            matches.forEach { match ->
                                val min = match.groupValues[1].toLongOrNull() ?: 0L
                                val sec = match.groupValues[2].toLongOrNull() ?: 0L
                                val msStr = match.groupValues[3]
                                val ms = when (msStr.length) {
                                    2 -> (msStr.toLongOrNull() ?: 0L) * 10
                                    3 -> msStr.toLongOrNull() ?: 0L
                                    else -> 0L
                                }
                                val time = min * 60 * 1000L + sec * 1000L + ms
                                parsed.add(com.example.ecodot.ui.viewmodel.LyricLine(time, text))
                            }
                        }
                    }
                    currentLyricsList = parsed.sortedBy { it.timeMs }
                    Log.d(TAG, "Lyrics loaded for notification updater: ${parsed.size} lines")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load lyrics for notification updater", e)
            }
        }
    }

    private fun startLyricsNotificationUpdater() {
        lyricsUpdaterJob?.cancel()
        lyricsUpdaterJob = serviceScope.launch {
            while (isActive) {
                delay(500)
                val player = players[activePlayerIndex] ?: continue
                if (!player.isPlaying) continue

                val mediaItem = player.currentMediaItem ?: continue
                val position = player.currentPosition

                if (currentLyricsList.isNotEmpty()) {
                    val activeLine = currentLyricsList.indexOfLast { position >= it.timeMs }
                    val activeText = if (activeLine >= 0 && activeLine < currentLyricsList.size) {
                        currentLyricsList[activeLine].text
                    } else {
                        ""
                    }

                    if (activeText != lastLyricLineText) {
                        lastLyricLineText = activeText
                        updateNotificationLyrics(player, mediaItem, activeText)
                    }
                }
            }
        }
    }

    private fun updateNotificationLyrics(player: Player, mediaItem: MediaItem, lyricText: String) {
        val displayArtist = if (lyricText.isNotEmpty()) {
            "$originalArtistName • $lyricText"
        } else {
            originalArtistName
        }

        val currentIndex = player.currentMediaItemIndex
        if (currentIndex != androidx.media3.common.C.INDEX_UNSET) {
            val updatedMetadata = mediaItem.mediaMetadata.buildUpon()
                .setArtist(displayArtist)
                .build()
            val updatedMediaItem = mediaItem.buildUpon()
                .setMediaMetadata(updatedMetadata)
                .build()
            
            player.replaceMediaItem(currentIndex, updatedMediaItem)
            Log.d(TAG, "Notification Lyrics updated: $displayArtist")
        }
    }
}
