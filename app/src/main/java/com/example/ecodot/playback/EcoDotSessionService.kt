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

import org.koin.android.ext.android.inject

class EcoDotSessionService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var effectManager: AudioEffectManager? = null
    private val repository: MusicRepository by inject()
    private val prefsManager: com.example.ecodot.data.local.prefs.UserPreferencesManager by inject()
    private val getSponsorSegmentsUseCase: com.example.ecodot.domain.usecase.GetSponsorSegmentsUseCase by inject()

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(
        Dispatchers.Main + serviceJob +
        CoroutineExceptionHandler { _, e ->
            Log.e(TAG, "Coroutine error: ${e.message}", e)
        }
    )

    private var activePlayerIndex = 0
    private val players = arrayOfNulls<ExoPlayer>(2)
    private val filterProcessors = arrayOfNulls<CrossfadeFilterAudioProcessor>(2)
    private var crossfadeMonitorJob: Job? = null
    private var crossfadeJob: Job? = null
    private var stagingJob: Job? = null
    private var stagedDeckIndex: Int? = null
    private var stagedTargetMediaId: String? = null
    private var activeCrossfadeIncomingId: String? = null
    @Volatile
    private var isCrossfading = false

    private val musicAudioAttributes: AudioAttributes by lazy {
        AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()
    }

    // ── SponsorBlock State ──
    private var sponsorBlockJob: Job? = null
    private var currentSponsorSegments: List<com.example.ecodot.domain.model.SponsorSegment> = emptyList()
    private var lastSkippedSegmentStart: Long? = null

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
        const val ACTION_SET_BASS_BOOST  = "SET_BASS_BOOST"
        const val ACTION_SET_EQ_ENABLED  = "SET_EQ_ENABLED"
        const val ACTION_SET_SKIP_SILENCE = "SET_SKIP_SILENCE"
        const val ACTION_SET_NORMALIZE_VOLUME = "SET_NORMALIZE_VOLUME"
        const val ACTION_REFRESH_SPONSOR_SEGMENTS = "REFRESH_SPONSOR_SEGMENTS"
        const val ACTION_CLEAR_URL_CACHE = "CLEAR_URL_CACHE"
        const val ACTION_UPDATE_QUEUE    = "UPDATE_QUEUE"
        const val ACTION_UPDATE_INDEX    = "UPDATE_INDEX"
        const val ACTION_INVALIDATE_STAGED_NEXT = "INVALIDATE_STAGED_NEXT"
        const val ACTION_SET_SLEEP_TIMER = "SET_SLEEP_TIMER"
        const val ACTION_CANCEL_SLEEP_TIMER = "CANCEL_SLEEP_TIMER"
        const val EXTRA_IS_CROSSFADING = "is_crossfading"
        const val EXTRA_CROSSFADE_DURATION_MS = "crossfade_duration_ms"
        const val EXTRA_STREAM_BITRATE_BPS = "stream_bitrate_bps"
        const val EXTRA_STREAM_SAMPLE_RATE_HZ = "stream_sample_rate_hz"
        const val EXTRA_STREAM_MIME_TYPE = "stream_mime_type"
        const val EXTRA_STREAM_CODECS = "stream_codecs"
    }

    @Volatile private var lastCrossfadeDurationMs: Long = 0L
    @Volatile private var lastStreamBitrateBps: Int = 0
    @Volatile private var lastStreamSampleRateHz: Int = 0
    @Volatile private var lastStreamMimeType: String? = null
    @Volatile private var lastStreamCodecs: String? = null

    override fun onCreate() {
        com.example.ecodot.EcoDotApplication.ensureKoinStarted(applicationContext)
        super.onCreate()

        val httpDataSourceFactory = androidx.media3.datasource.DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36")
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(15000)
            .setAllowCrossProtocolRedirects(true)

        val baseDataSourceFactory = DefaultDataSource.Factory(this, httpDataSourceFactory)

        val resolvingDataSourceFactory = ResolvingDataSource.Factory(
            baseDataSourceFactory,
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
                        val activeQuality = prefsManager.audioQuality
                        val cacheKey = "${videoId}_${isVideo}_${activeQuality.name}"
                        val diskCacheKey = diskCacheKeyFor(videoId, isVideo, activeQuality)
                        val cached = urlCache[cacheKey]
                        if (cached != null && System.currentTimeMillis() - cached.timestamp < URL_CACHE_TTL_MS) {
                            prefsManager.addCachedTrackId(videoId)
                            return dataSpec.buildUpon()
                                .setUri(android.net.Uri.parse(cached.url))
                                .setKey(diskCacheKey)
                                .build()
                        }

                        // Cache miss — resolve with timeout to prevent indefinite thread blocking
                        val streamUrl = runBlocking(Dispatchers.IO) {
                            withTimeoutOrNull(8000L) {
                                repository.getYouTubeStreamUrl(
                                    videoId = videoId,
                                    quality = activeQuality,
                                    videoQuality = prefsManager.videoQuality,
                                    isVideo = isVideo
                                )
                            }
                        }
                        if (streamUrl != null) {
                            urlCache[cacheKey] = CachedUrl(streamUrl, System.currentTimeMillis())
                            prefsManager.addCachedTrackId(videoId)
                            return dataSpec.buildUpon()
                                .setUri(android.net.Uri.parse(streamUrl))
                                .setKey(diskCacheKey)
                                .build()
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
        val fp0 = CrossfadeFilterAudioProcessor()
        val fp1 = CrossfadeFilterAudioProcessor()
        filterProcessors[0] = fp0
        filterProcessors[1] = fp1
        players[0] = buildPlayer(mediaSourceFactory, sharedSessionId, fp0, handleAudioFocus = true)
        players[1] = buildPlayer(mediaSourceFactory, sharedSessionId, fp1, handleAudioFocus = false)

        effectManager = AudioEffectManager(this, players[0]!!.audioSessionId).apply {
            setEnabled(prefsManager.equalizerEnabled)
            setNormalizeVolume(prefsManager.normalizeVolume)
            setVirtualizerStrength(prefsManager.virtualizerStrength.toShort())
            setBassBoostStrength(prefsManager.bassBoostStrength.toShort())
            val bandCount = AudioEffectManager.deviceEqSpec.value.bandCount
            val savedRaw = prefsManager.customEqBandsRaw
            if (savedRaw.isNotBlank()) {
                val restored = com.example.ecodot.domain.model.decodeEqBands(savedRaw, bandCount)
                restored.forEach { (idx, level) ->
                    setBandLevel(idx.toShort(), level.toShort())
                }
            } else {
                val allPresets = com.example.ecodot.domain.model.DEFAULT_EQ_PRESETS +
                    com.example.ecodot.domain.model.decodeCustomEqPresets(prefsManager.customEqPresetsRaw)
                val savedPreset = allPresets.find { it.name == prefsManager.selectedEqPreset }
                savedPreset?.bands?.forEachIndexed { idx, level ->
                    setBandLevel(idx.toShort(), level.toShort())
                }
            }
        }

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
    private fun buildPlayer(
        mediaSourceFactory: MediaSourceFactory,
        sessionId: Int,
        filterProcessor: CrossfadeFilterAudioProcessor,
        handleAudioFocus: Boolean
    ): ExoPlayer {
        val loadControl = androidx.media3.exoplayer.DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                30_000, // Increased min buffer to 30s to withstand CPU spikes
                60_000, // Increased max buffer to 60s
                5_000,  // Increased playback start buffer to 5s for smoother starts
                8_000   // Increased rebuffer buffer to 8s
            )
            .build()

        val renderersFactory = object : androidx.media3.exoplayer.DefaultRenderersFactory(this) {
            override fun buildAudioSink(
                context: android.content.Context,
                enableFloatOutput: Boolean,
                enableAudioTrackPlaybackParams: Boolean
            ): androidx.media3.exoplayer.audio.AudioSink {
                return androidx.media3.exoplayer.audio.DefaultAudioSink.Builder(context)
                    .setEnableFloatOutput(enableFloatOutput)
                    .setEnableAudioTrackPlaybackParams(enableAudioTrackPlaybackParams)
                    .setAudioProcessors(arrayOf(filterProcessor))
                    .build()
            }
        }

        val trackSelector = androidx.media3.exoplayer.trackselection.DefaultTrackSelector(this).apply {
            setParameters(
                buildUponParameters()
                    .setMaxVideoSize(3840, 2160) // Up to 4K / 1080p HD
                    .setMaxVideoBitrate(Int.MAX_VALUE)
                    .setForceHighestSupportedBitrate(true) // Always lock into 1080p full resolution video track
            )
        }

        return ExoPlayer.Builder(this)
            .setRenderersFactory(renderersFactory)
            .setTrackSelector(trackSelector)
            .setMediaSourceFactory(mediaSourceFactory)
            .setLoadControl(loadControl)
            .setWakeMode(C.WAKE_MODE_NETWORK) // Prevent CPU/WiFi sleep in background
            .setAudioAttributes(musicAudioAttributes, handleAudioFocus)
            .setHandleAudioBecomingNoisy(true)
            .build().apply {
                setAudioSessionId(sessionId)
                skipSilenceEnabled = prefsManager.skipSilent
                addListener(object : Player.Listener {
                    override fun onAudioSessionIdChanged(audioSessionId: Int) {
                        if (players[activePlayerIndex] == null || players[activePlayerIndex] == this@apply) {
                            effectManager?.updateAudioSessionId(audioSessionId)
                        }
                    }
                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        if (players[activePlayerIndex] == this@apply) {
                            onTrackActivated(this@apply, mediaItem)
                        }
                    }
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        if (players[activePlayerIndex] == this@apply) {
                            if (isPlaying) {
                                startLyricsNotificationUpdater()
                                publishAudioTelemetryFor(this@apply)
                            } else {
                                lyricsUpdaterJob?.cancel()
                                lyricsUpdaterJob = null
                            }
                        }
                    }
                    override fun onPlaybackStateChanged(state: Int) {
                        if (players[activePlayerIndex] == this@apply) {
                            updateNotification()
                            if (state == Player.STATE_READY) {
                                publishAudioTelemetryFor(this@apply)
                            }
                        }
                    }
                    override fun onTracksChanged(tracks: androidx.media3.common.Tracks) {
                        if (players[activePlayerIndex] == this@apply) {
                            publishAudioTelemetryFor(this@apply)
                        }
                    }
                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        Log.e(TAG, "Player error: ${error.message}")
                        if (stagedDeckIndex != null && players[stagedDeckIndex!!] == this@apply) {
                            clearStagedCrossfade()
                        }
                    }
                    override fun onShuffleModeEnabledChanged(enabled: Boolean) = updateNotification()
                    override fun onRepeatModeChanged(mode: Int) = updateNotification()
                })
            }
    }

    private fun onTrackActivated(player: ExoPlayer, mediaItem: MediaItem?) {
        val trackId = mediaItem?.mediaId ?: return
        val isVideoItem = mediaItem.localConfiguration?.uri?.scheme == "ytvideo"
        prefsManager.addCachedTrackId(trackId)
        serviceScope.launch(Dispatchers.IO) {
            try {
                val title = mediaItem.mediaMetadata.title?.toString() ?: "Unknown Title"
                val artist = mediaItem.mediaMetadata.artist?.toString() ?: "Unknown Artist"
                val artUri = mediaItem.mediaMetadata.artworkUri?.toString()
                val db = EcoDotDatabase.getInstance(this@EcoDotSessionService)
                if (db.trackDao().getTrackById(trackId) == null) {
                    db.trackDao().insertTracks(
                        listOf(
                            Track(
                                id = trackId,
                                title = title,
                                artist = artist,
                                album = if (isVideoItem) "YouTube Video" else "Cached",
                                duration = 0L,
                                path = if (isVideoItem) "ytvideo://$trackId" else "yt://$trackId",
                                albumArtUri = artUri,
                                isYouTube = true
                            )
                        )
                    )
                }
            } catch (e: Exception) {
                // ignore
            }
        }
        if (trackId != currentPlayingTrackId) {
            currentPlayingTrackId = trackId
            // If user manually switched tracks mid-crossfade, abort the blend cleanly
            if (isCrossfading && trackId != activeCrossfadeIncomingId) {
                abortCrossfade(keepActivePlayer = true)
            } else if (!isCrossfading && stagedTargetMediaId != trackId) {
                clearStagedCrossfade()
            }
            updateNotification()

            // Load and inject artwork bytes for full-bleed notification
            loadArtworkBytesAndInject(mediaItem)

            // Load lyrics and reset state for notification updater
            val title = mediaItem.mediaMetadata.title?.toString() ?: ""
            val artist = mediaItem.mediaMetadata.artist?.toString() ?: ""
            originalArtistName = artist
            originalTitle = title
            loadLyricsForUpdater(
                trackId = mediaItem.mediaId,
                title = title,
                artist = artist,
                duration = player.duration
            )
            loadSponsorSegments(mediaItem.mediaId)

            // Prefetch the next track in queue to avoid runBlocking network delays during transition
            val nextIndex = player.nextMediaItemIndex
            if (nextIndex != C.INDEX_UNSET && nextIndex in 0 until player.mediaItemCount) {
                val nextItem = player.getMediaItemAt(nextIndex)
                val nextIsVideo = nextItem.localConfiguration?.uri?.scheme == "ytvideo"
                preFetchStreamUrl(nextItem.mediaId, nextIsVideo)
            }

            // Check and handle sleep timer end of track transition
            if (isSleepTimerAtEndOfTrack) {
                player.pause()
                isSleepTimerAtEndOfTrack = false
                val prefs = getSharedPreferences("sleep_timer_prefs", android.content.Context.MODE_PRIVATE)
                prefs.edit().clear().apply()
                Log.d(TAG, "Sleep Timer: Paused playback at the end of track.")
            }
        }
    }

    private fun diskCacheKeyFor(
        videoId: String,
        isVideo: Boolean,
        quality: com.example.ecodot.data.local.prefs.AudioQuality
    ): String = when {
        isVideo -> "ytvideo_$videoId"
        quality == com.example.ecodot.data.local.prefs.AudioQuality.HIGH -> "yt_$videoId"
        else -> "yt_${videoId}_${quality.name.lowercase()}"
    }

    private fun preFetchStreamUrl(videoId: String, isVideo: Boolean = false) {
        val activeQuality = prefsManager.audioQuality
        val cacheKey = "${videoId}_${isVideo}_${activeQuality.name}"
        if (urlCache.containsKey(cacheKey)) return // Already cached

        serviceScope.launch(Dispatchers.IO) {
            try {
                val streamUrl = repository.getYouTubeStreamUrl(
                    videoId = videoId,
                    quality = activeQuality,
                    videoQuality = prefsManager.videoQuality,
                    isVideo = isVideo
                )
                if (streamUrl != null) {
                    urlCache[cacheKey] = CachedUrl(streamUrl, System.currentTimeMillis())
                    Log.d(TAG, "Pre-fetched stream URL for next track: $videoId (isVideo=$isVideo, q=$activeQuality)")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to pre-fetch stream URL for: $videoId", e)
            }
        }
    }

    private var lastPreCachedTrackId: String? = null

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun preCacheTrack(videoId: String, isVideo: Boolean = false) {
        if (isVideo) return // Video streams use adaptive HLS/MPD segments rather than single audio byte pre-cache
        val activeQuality = prefsManager.audioQuality
        val preCacheToken = "${videoId}_${activeQuality.name}"
        if (lastPreCachedTrackId == preCacheToken) return
        lastPreCachedTrackId = preCacheToken

        serviceScope.launch(Dispatchers.IO) {
            try {
                val cacheKey = "${videoId}_false_${activeQuality.name}"
                var streamUrl = urlCache[cacheKey]?.url
                if (streamUrl == null) {
                    streamUrl = repository.getYouTubeStreamUrl(
                        videoId = videoId,
                        quality = activeQuality,
                        videoQuality = prefsManager.videoQuality,
                        isVideo = false
                    )
                    if (streamUrl != null) {
                        urlCache[cacheKey] = CachedUrl(streamUrl, System.currentTimeMillis())
                    }
                }

                if (streamUrl != null) {
                    Log.d(TAG, "Pre-caching bytes for next track: $videoId ($activeQuality)")
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
                        .setKey(diskCacheKeyFor(videoId, isVideo = false, quality = activeQuality))
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

    private fun assignAudioFocusTo(targetIndex: Int) {
        players[targetIndex]?.setAudioAttributes(musicAudioAttributes, true)
        players[1 - targetIndex]?.setAudioAttributes(musicAudioAttributes, false)
    }

    private fun publishCrossfadeState(active: Boolean, durationMs: Long = 0L) {
        isCrossfading = active
        lastCrossfadeDurationMs = durationMs
        if (!active) {
            activeCrossfadeIncomingId = null
        }
        emitSessionExtras()
    }

    @OptIn(UnstableApi::class)
    private fun publishAudioTelemetryFor(player: ExoPlayer) {
        val mediaId = player.currentMediaItem?.mediaId
        val selectedTrackFormat = player.currentTracks.groups
            .firstOrNull { it.type == C.TRACK_TYPE_AUDIO && it.isSelected }
            ?.let { group ->
                (0 until group.length)
                    .firstOrNull { group.isTrackSelected(it) }
                    ?.let { group.getTrackFormat(it) }
            }
        val decoderAudioFormat = player.audioFormat
        val resolverMeta = mediaId?.let {
            com.example.ecodot.data.remote.YouTubeStreamResolver.resolvedAudioTelemetry[it]
        }

        val rawSampleRate = selectedTrackFormat?.sampleRate?.takeIf { it > 0 }
            ?: decoderAudioFormat?.sampleRate?.takeIf { it > 0 }
            ?: resolverMeta?.sampleRateHz
            ?: 0
        // HE-AAC decoders report half-rate core sampling (22.05/24 kHz); prefer full SBR rate from container/resolver when known.
        val sampleRate = if (rawSampleRate in 1..24_000 && (resolverMeta?.sampleRateHz ?: 0) >= 44_100) {
            resolverMeta?.sampleRateHz ?: rawSampleRate
        } else if (rawSampleRate <= 0) {
            resolverMeta?.sampleRateHz ?: 0
        } else {
            rawSampleRate
        }

        val rawBitrate = selectedTrackFormat?.bitrate?.takeIf { it > 0 }
            ?: selectedTrackFormat?.averageBitrate?.takeIf { it > 0 }
            ?: decoderAudioFormat?.bitrate?.takeIf { it > 0 }
            ?: resolverMeta?.bitrateBps
            ?: 0
        var bitrate = com.example.ecodot.ui.components.player.PlayerStyleMath.normalizeBitrateBps(rawBitrate)

        if (bitrate <= 0 && mediaId != null && player.duration > 0) {
            val uriStr = player.currentMediaItem?.localConfiguration?.uri?.toString().orEmpty()
            val localPath = when {
                uriStr.startsWith("file://") -> android.net.Uri.parse(uriStr).path
                uriStr.startsWith("/") -> uriStr
                else -> File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "$mediaId.m4a")
                    .takeIf { it.exists() }?.absolutePath
            }
            if (localPath != null) {
                val f = File(localPath)
                if (f.exists() && f.length() > 0L) {
                    bitrate = ((f.length() * 8L * 1000L) / player.duration).toInt()
                }
            }
        }

        lastStreamBitrateBps = bitrate
        lastStreamSampleRateHz = sampleRate
        lastStreamMimeType = selectedTrackFormat?.sampleMimeType ?: decoderAudioFormat?.sampleMimeType
        lastStreamCodecs = selectedTrackFormat?.codecs ?: decoderAudioFormat?.codecs ?: resolverMeta?.codec
        emitSessionExtras()
    }

    private fun emitSessionExtras() {
        val extras = Bundle().apply {
            putBoolean(EXTRA_IS_CROSSFADING, isCrossfading)
            putLong(EXTRA_CROSSFADE_DURATION_MS, lastCrossfadeDurationMs)
            putInt(EXTRA_STREAM_BITRATE_BPS, lastStreamBitrateBps)
            putInt(EXTRA_STREAM_SAMPLE_RATE_HZ, lastStreamSampleRateHz)
            putString(EXTRA_STREAM_MIME_TYPE, lastStreamMimeType)
            putString(EXTRA_STREAM_CODECS, lastStreamCodecs)
        }
        mediaSession?.setSessionExtras(extras)
    }

    private fun getNextCrossfadeIndex(player: ExoPlayer): Int? {
        if (player.repeatMode == Player.REPEAT_MODE_ONE) return null
        val next = player.nextMediaItemIndex
        return if (next != C.INDEX_UNSET && next in 0 until player.mediaItemCount) next else null
    }

    private fun shouldSkipCrossfade(activePlayer: ExoPlayer, nextIndex: Int, durationMs: Long): Boolean {
        if (durationMs < 15_000L) return true
        val currentItem = activePlayer.currentMediaItem ?: return true
        val nextItem = activePlayer.getMediaItemAt(nextIndex)
        if (currentItem.localConfiguration?.uri?.scheme == "ytvideo" ||
            nextItem.localConfiguration?.uri?.scheme == "ytvideo"
        ) {
            return true
        }
        if (prefsManager.crossfadeSkipAlbum) {
            val currentAlbum = currentItem.mediaMetadata.albumTitle?.toString()?.trim().orEmpty()
            val nextAlbum = nextItem.mediaMetadata.albumTitle?.toString()?.trim().orEmpty()
            val isPlaceholder = currentAlbum.equals("YouTube", true) ||
                currentAlbum.equals("YouTube Music", true) ||
                currentAlbum.equals("YouTube Video", true) ||
                currentAlbum.equals("Cached", true) ||
                currentAlbum.equals("Unknown Album", true) ||
                currentAlbum.equals("Unknown", true)
            if (currentAlbum.isNotEmpty() && !isPlaceholder && currentAlbum.equals(nextAlbum, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    private fun resolveEffectiveCrossfadeMs(trackDurationMs: Long): Long {
        if (!prefsManager.crossfadeEnabled) return 0L
        val configuredSec = prefsManager.crossfadeDuration
        if (configuredSec > 0) {
            return (configuredSec * 1000L).coerceAtMost((trackDurationMs * 0.40).toLong().coerceAtLeast(1500L))
        }
        // AutoMix ("Auto"): musically scale fade by song length & outgoing RMS tail
        val outgoingRms = filterProcessors[activePlayerIndex]?.smoothedRms ?: 0.5f
        val baseAutoMs = when {
            trackDurationMs >= 210_000L -> 8_000L
            trackDurationMs >= 120_000L -> 6_000L
            trackDurationMs >= 60_000L -> 4_000L
            else -> 2_500L
        }
        val outroBonusMs = if (outgoingRms < 0.08f) 1_500L else 0L
        return (baseAutoMs + outroBonusMs).coerceIn(2_000L, (trackDurationMs * 0.30).toLong().coerceAtLeast(2_000L))
    }

    private fun clearStagedCrossfade() {
        stagingJob?.cancel()
        stagingJob = null
        stagedDeckIndex?.let { idx ->
            if (idx != activePlayerIndex && !isCrossfading) {
                players[idx]?.apply {
                    playWhenReady = false
                    stop()
                    clearMediaItems()
                    volume = 1f
                }
                filterProcessors[idx]?.resetFilter()
            }
        }
        stagedDeckIndex = null
        stagedTargetMediaId = null
    }

    private fun abortCrossfade(keepActivePlayer: Boolean = true) {
        crossfadeJob?.cancel()
        crossfadeJob = null
        val activeIdx = activePlayerIndex
        val otherIdx = 1 - activeIdx
        players[activeIdx]?.volume = 1f
        filterProcessors[activeIdx]?.resetFilter()
        if (keepActivePlayer) {
            players[otherIdx]?.apply {
                playWhenReady = false
                stop()
                clearMediaItems()
                volume = 1f
            }
            filterProcessors[otherIdx]?.resetFilter()
            assignAudioFocusTo(activeIdx)
        }
        publishCrossfadeState(false)
    }

    private fun startCrossfadeMonitor() {
        crossfadeMonitorJob?.cancel()
        crossfadeMonitorJob = serviceScope.launch {
            while (isActive) {
                delay(120)

                val activePlayer = players[activePlayerIndex] ?: continue
                if (activePlayer.playWhenReady && activePlayer.playbackState == Player.STATE_READY) {
                    val duration = activePlayer.duration
                    val currentPos = activePlayer.currentPosition
                    if (duration != C.TIME_UNSET && currentPos != C.TIME_UNSET && duration > 0L) {
                        // 0. SponsorBlock auto-skip
                        val enabledCategories = prefsManager.sponsorBlockCategories
                        if (prefsManager.sponsorBlockEnabled && enabledCategories.isNotEmpty() && currentSponsorSegments.isNotEmpty()) {
                            val segment = currentSponsorSegments.firstOrNull { seg ->
                                seg.category in enabledCategories && currentPos in seg.start until seg.end
                            }
                            if (segment != null) {
                                if (lastSkippedSegmentStart != segment.start) {
                                    lastSkippedSegmentStart = segment.start
                                    Log.i(TAG, "SponsorBlock: auto-skipping [${segment.category}] from ${segment.start}ms to ${segment.end}ms")
                                    activePlayer.seekTo(segment.end + 50L)
                                }
                            } else if (lastSkippedSegmentStart != null) {
                                val inAny = currentSponsorSegments.any { it.category in enabledCategories && currentPos in it.start until it.end }
                                if (!inAny) lastSkippedSegmentStart = null
                            }
                        }

                        val remaining = duration - currentPos
                        val nextIndex = getNextCrossfadeIndex(activePlayer)

                        // 1. Trigger pre-caching 16 seconds before the track ends
                        if (remaining in 1000L..16_000L && nextIndex != null) {
                            val nextItem = activePlayer.getMediaItemAt(nextIndex)
                            val nextIsVideo = nextItem.localConfiguration?.uri?.scheme == "ytvideo"
                            preCacheTrack(nextItem.mediaId, nextIsVideo)
                        }

                        // 2. SimpMusic-style Pre-Staging + Equal-Power / DJ Crossfade
                        if (!prefsManager.crossfadeEnabled) {
                            if (stagedDeckIndex != null) clearStagedCrossfade()
                            if (isCrossfading) abortCrossfade(keepActivePlayer = true)
                        } else if (!isCrossfading && nextIndex != null) {
                            val crossfadeMs = resolveEffectiveCrossfadeMs(duration)
                            val skip = shouldSkipCrossfade(activePlayer, nextIndex, duration)
                            val targetItem = activePlayer.getMediaItemAt(nextIndex)

                            if (skip) {
                                if (stagedDeckIndex != null) clearStagedCrossfade()
                            } else {
                                val stagingThresholdMs = crossfadeMs + 8_000L
                                if (remaining > stagingThresholdMs + 4_000L && stagedDeckIndex != null) {
                                    // User scrubbed backward before staging window
                                    clearStagedCrossfade()
                                } else if (remaining in 1000L..stagingThresholdMs && stagedTargetMediaId != targetItem.mediaId && currentPos > 2000L) {
                                    stageNextDeckForCrossfade(activePlayer, nextIndex, targetItem.mediaId)
                                }

                                if (remaining in 150L..crossfadeMs && currentPos > duration / 2 && currentPos > 4000L) {
                                    val clampedDuration = remaining.coerceIn(800L, crossfadeMs)
                                    triggerCrossfade(activePlayer, nextIndex, clampedDuration)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun stageNextDeckForCrossfade(oldPlayer: ExoPlayer, nextIndex: Int, targetMediaId: String) {
        val nextDeck = 1 - activePlayerIndex
        val standbyPlayer = players[nextDeck] ?: return
        clearStagedCrossfade()
        stagedDeckIndex = nextDeck
        stagedTargetMediaId = targetMediaId

        val fullQueue = List(oldPlayer.mediaItemCount) { idx -> oldPlayer.getMediaItemAt(idx) }
        if (nextIndex !in fullQueue.indices) {
            clearStagedCrossfade()
            return
        }

        stagingJob = serviceScope.launch {
            try {
                // Ensure stream URL is resolved off the main player thread before prepare()
                val targetItem = fullQueue[nextIndex]
                val activeQuality = prefsManager.audioQuality
                val cacheKey = "${targetItem.mediaId}_false_${activeQuality.name}"
                if (!urlCache.containsKey(cacheKey)) {
                    withContext(Dispatchers.IO) {
                        val url = repository.getYouTubeStreamUrl(
                            videoId = targetItem.mediaId,
                            quality = activeQuality,
                            videoQuality = prefsManager.videoQuality,
                            isVideo = false
                        )
                        if (url != null) {
                            urlCache[cacheKey] = CachedUrl(url, System.currentTimeMillis())
                        }
                    }
                }
                if (stagedTargetMediaId != targetMediaId || isCrossfading) return@launch

                standbyPlayer.apply {
                    setAudioAttributes(musicAudioAttributes, false)
                    repeatMode = oldPlayer.repeatMode
                    shuffleModeEnabled = false
                    playbackParameters = oldPlayer.playbackParameters
                    volume = 0f
                    playWhenReady = false
                    setMediaItems(fullQueue, nextIndex, 0L)
                    prepare()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Crossfade pre-stage failed for $targetMediaId", e)
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun triggerCrossfade(oldPlayer: ExoPlayer, nextIndex: Int, durationMs: Long) {
        isCrossfading = true
        val oldIndex = activePlayerIndex
        val nextIndexDeck = 1 - oldIndex
        val newPlayer = players[nextIndexDeck] ?: run {
            publishCrossfadeState(false)
            return
        }

        val fullQueue = List(oldPlayer.mediaItemCount) { i -> oldPlayer.getMediaItemAt(i) }
        if (nextIndex !in fullQueue.indices) {
            publishCrossfadeState(false)
            return
        }
        val incomingItem = fullQueue[nextIndex]
        activeCrossfadeIncomingId = incomingItem.mediaId

        // Prepare if not already staged for this exact track
        val alreadyStagedForTrack = stagedDeckIndex == nextIndexDeck && stagedTargetMediaId == incomingItem.mediaId
        stagingJob?.cancel()
        stagingJob = null
        stagedDeckIndex = null
        stagedTargetMediaId = null

        if (!alreadyStagedForTrack || newPlayer.currentMediaItem?.mediaId != incomingItem.mediaId) {
            newPlayer.apply {
                setAudioAttributes(musicAudioAttributes, false)
                repeatMode = oldPlayer.repeatMode
                shuffleModeEnabled = false
                playbackParameters = oldPlayer.playbackParameters
                volume = 0f
                playWhenReady = false
                setMediaItems(fullQueue, nextIndex, 0L)
                prepare()
            }
        }

        val djMode = prefsManager.crossfadeDjMode
        val outProcessor = filterProcessors[oldIndex]
        val inProcessor = filterProcessors[nextIndexDeck]

        crossfadeJob?.cancel()
        crossfadeJob = serviceScope.launch {
            val waitStart = android.os.SystemClock.elapsedRealtime()
            while (newPlayer.playbackState != Player.STATE_READY) {
                if (oldPlayer.playbackState == Player.STATE_ENDED || !oldPlayer.playWhenReady) {
                    abortCrossfade(keepActivePlayer = true)
                    return@launch
                }
                if (android.os.SystemClock.elapsedRealtime() - waitStart > 6000L) {
                    abortCrossfade(keepActivePlayer = true)
                    return@launch
                }
                delay(35)
            }

            if (oldPlayer.playbackState == Player.STATE_ENDED || !oldPlayer.playWhenReady || !prefsManager.crossfadeEnabled) {
                abortCrossfade(keepActivePlayer = true)
                return@launch
            }

            // Use exact remaining tail if pre-buffering ate part of the window
            val liveRemaining = (oldPlayer.duration - oldPlayer.currentPosition).takeIf {
                oldPlayer.duration != C.TIME_UNSET && oldPlayer.currentPosition != C.TIME_UNSET && it > 350L
            } ?: durationMs
            val actualFadeMs = liveRemaining.coerceIn(600L, durationMs)

            // CRITICAL: Release AudioFocus handling on outgoing player BEFORE incoming plays
            // so Android AudioManager doesn't send AUDIOFOCUS_LOSS to oldPlayer and silence it!
            activePlayerIndex = nextIndexDeck
            assignAudioFocusTo(nextIndexDeck)
            newPlayer.volume = 0f
            if (djMode) {
                outProcessor?.setFilter(CrossfadeFilterAudioProcessor.FilterMode.LOW_PASS, 18_000f)
                inProcessor?.setFilter(CrossfadeFilterAudioProcessor.FilterMode.HIGH_PASS, 750f)
            } else {
                outProcessor?.resetFilter()
                inProcessor?.resetFilter()
            }

            newPlayer.playWhenReady = true
            mediaSession?.player = newPlayer
            effectManager?.updateAudioSessionId(newPlayer.audioSessionId)
            publishCrossfadeState(true, actualFadeMs)
            onTrackActivated(newPlayer, incomingItem)
            startLyricsNotificationUpdater()
            updateNotification()

            var elapsedFadeMs = 0L
            var lastTickMs = android.os.SystemClock.elapsedRealtime()

            while (isActive && elapsedFadeMs < actualFadeMs) {
                val nowMs = android.os.SystemClock.elapsedRealtime()
                val deltaMs = (nowMs - lastTickMs).coerceIn(0L, 120L)
                lastTickMs = nowMs

                // Pause/resume outgoing deck in lockstep with incoming player
                if (!newPlayer.playWhenReady && oldPlayer.playWhenReady) {
                    oldPlayer.pause()
                } else if (newPlayer.playWhenReady && !oldPlayer.playWhenReady) {
                    oldPlayer.play()
                }

                if (newPlayer.playWhenReady) {
                    elapsedFadeMs += deltaMs
                    val progress = (elapsedFadeMs.toFloat() / actualFadeMs.toFloat()).coerceIn(0f, 1f)
                    val angle = progress * (kotlin.math.PI / 2.0)

                    // Equal-power fade prevents -6dB mid-fade energy dip
                    val oldGain = kotlin.math.cos(angle).toFloat().coerceIn(0f, 1f)
                    val newGain = kotlin.math.sin(angle).toFloat().coerceIn(0f, 1f)

                    if (oldPlayer.playbackState != Player.STATE_ENDED) {
                        oldPlayer.volume = oldGain
                    }
                    newPlayer.volume = newGain

                    if (djMode) {
                        val lpCutoff = (18_000.0 * Math.pow(280.0 / 18_000.0, progress.toDouble())).toFloat()
                        outProcessor?.setFilter(CrossfadeFilterAudioProcessor.FilterMode.LOW_PASS, lpCutoff)

                        val hpProgress = (progress / 0.80f).coerceIn(0f, 1f)
                        if (hpProgress < 0.99f) {
                            val hpCutoff = (750.0 * Math.pow(24.0 / 750.0, hpProgress.toDouble())).toFloat()
                            inProcessor?.setFilter(CrossfadeFilterAudioProcessor.FilterMode.HIGH_PASS, hpCutoff)
                        } else {
                            inProcessor?.setFilter(CrossfadeFilterAudioProcessor.FilterMode.NONE, 20f)
                        }
                    }
                }
                delay(30)
            }

            newPlayer.volume = 1f
            inProcessor?.resetFilter()
            outProcessor?.resetFilter()
            // Preserve any endless-queue items appended to oldPlayer after staging began
            if (oldPlayer.mediaItemCount > newPlayer.mediaItemCount) {
                val tailItems = List(oldPlayer.mediaItemCount - newPlayer.mediaItemCount) { idx ->
                    oldPlayer.getMediaItemAt(newPlayer.mediaItemCount + idx)
                }
                newPlayer.addMediaItems(tailItems)
            }
            oldPlayer.playWhenReady = false
            oldPlayer.stop()
            oldPlayer.clearMediaItems()
            oldPlayer.volume = 1f
            publishCrossfadeState(false)
        }
    }

    override fun onGetSession(info: MediaSession.ControllerInfo) = mediaSession

    override fun onDestroy() {
        try {
            unregisterReceiver(screenStateReceiver)
        } catch (e: Exception) {
            // Ignore
        }
        crossfadeJob?.cancel()
        stagingJob?.cancel()
        filterProcessors.forEach { it?.resetFilter() }
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

    private fun isTrustedExternalMediaController(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): Boolean {
        val pkg = controller.packageName
        if (pkg == packageName) return true
        if (session.isMediaNotificationController(controller) ||
            session.isAutomotiveController(controller) ||
            session.isAutoCompanionController(controller)
        ) {
            return true
        }
        if (pkg == "com.android.systemui" ||
            pkg == "android" ||
            pkg == "com.android.bluetooth" ||
            pkg == "com.google.android.projection.gearhead" ||
            pkg == "com.google.android.googlequicksearchbox" ||
            pkg == "com.google.android.wearable.app"
        ) {
            return true
        }
        return runCatching {
            val appInfo = packageManager.getApplicationInfo(pkg, 0)
            (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0 ||
                (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
        }.getOrDefault(false)
    }

    private inner class CommandCallback : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val isInternalClient = controller.packageName == packageName
            if (!isTrustedExternalMediaController(session, controller)) {
                Log.w(TAG, "Rejected untrusted MediaSession client: ${controller.packageName}")
                return MediaSession.ConnectionResult.reject()
            }

            val sessionCmdBuilder = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                .add(SessionCommand(ACTION_TOGGLE_LIKE, Bundle.EMPTY))
                .add(SessionCommand(ACTION_TOGGLE_SHUFFLE, Bundle.EMPTY))
                .add(SessionCommand(ACTION_CYCLE_REPEAT, Bundle.EMPTY))
                .add(SessionCommand(ACTION_FORWARD_10, Bundle.EMPTY))

            if (isInternalClient) {
                sessionCmdBuilder
                    .add(SessionCommand(ACTION_SET_EQ_BAND, Bundle.EMPTY))
                    .add(SessionCommand(ACTION_SET_VIRTUALIZER, Bundle.EMPTY))
                    .add(SessionCommand(ACTION_SET_BASS_BOOST, Bundle.EMPTY))
                    .add(SessionCommand(ACTION_SET_EQ_ENABLED, Bundle.EMPTY))
                    .add(SessionCommand(ACTION_SET_SKIP_SILENCE, Bundle.EMPTY))
                    .add(SessionCommand(ACTION_SET_NORMALIZE_VOLUME, Bundle.EMPTY))
                    .add(SessionCommand(ACTION_REFRESH_SPONSOR_SEGMENTS, Bundle.EMPTY))
                    .add(SessionCommand(ACTION_CLEAR_URL_CACHE, Bundle.EMPTY))
                    .add(SessionCommand(ACTION_UPDATE_QUEUE, Bundle.EMPTY))
                    .add(SessionCommand(ACTION_UPDATE_INDEX, Bundle.EMPTY))
                    .add(SessionCommand(ACTION_INVALIDATE_STAGED_NEXT, Bundle.EMPTY))
                    .add(SessionCommand(ACTION_SET_SLEEP_TIMER, Bundle.EMPTY))
                    .add(SessionCommand(ACTION_CANCEL_SLEEP_TIMER, Bundle.EMPTY))
            }

            val playerCmds = MediaSession.ConnectionResult.DEFAULT_PLAYER_COMMANDS.buildUpon()
                .add(Player.COMMAND_SEEK_TO_NEXT)
                .add(Player.COMMAND_SEEK_TO_PREVIOUS)
                .add(Player.COMMAND_SEEK_FORWARD)
                .add(Player.COMMAND_SEEK_BACK)
                .add(Player.COMMAND_SET_SHUFFLE_MODE)
                .add(Player.COMMAND_SET_REPEAT_MODE)
                .build()

            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(sessionCmdBuilder.build())
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
                ACTION_SET_BASS_BOOST  -> effectManager?.setBassBoostStrength(
                    args.getInt("strength").toShort())
                ACTION_SET_EQ_ENABLED  -> effectManager?.setEnabled(args.getBoolean("enabled"))
                ACTION_SET_SKIP_SILENCE -> {
                    val enabled = args.getBoolean("enabled", false)
                    players.forEach { it?.skipSilenceEnabled = enabled }
                }
                ACTION_SET_NORMALIZE_VOLUME -> effectManager?.setNormalizeVolume(args.getBoolean("enabled", false))
                ACTION_REFRESH_SPONSOR_SEGMENTS -> {
                    currentPlayingTrackId?.let { loadSponsorSegments(it) }
                }
                ACTION_CLEAR_URL_CACHE -> {
                    urlCache.clear()
                    lastPreCachedTrackId = null
                    clearStagedCrossfade()
                }
                ACTION_INVALIDATE_STAGED_NEXT -> {
                    clearStagedCrossfade()
                }
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
        // Calling replaceMediaItem during ytvideo:// HLS/DASH playback disrupts active video decoding.
        if (mediaItem.localConfiguration?.uri?.scheme.equals("ytvideo", ignoreCase = true)) {
            return
        }
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

    private fun loadSponsorSegments(videoId: String) {
        sponsorBlockJob?.cancel()
        currentSponsorSegments = emptyList()
        lastSkippedSegmentStart = null

        // Skip local file paths
        if (videoId.startsWith("/") || videoId.startsWith("content://") || videoId.contains(File.separator)) {
            return
        }

        sponsorBlockJob = serviceScope.launch(Dispatchers.IO) {
            try {
                val categories = prefsManager.sponsorBlockCategories
                if (!prefsManager.sponsorBlockEnabled || categories.isEmpty()) return@launch
                val segments = getSponsorSegmentsUseCase(videoId, categories)
                currentSponsorSegments = segments.sortedBy { it.start }
                if (segments.isNotEmpty()) {
                    Log.d(TAG, "Loaded ${segments.size} SponsorBlock segments for $videoId: ${segments.map { "${it.category}(${it.start}..${it.end})" }}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to load SponsorBlock segments for $videoId: ${e.message}")
            }
        }
    }
}
