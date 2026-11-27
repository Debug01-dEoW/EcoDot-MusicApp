package com.example.ecodot.data.remote

import android.util.Log
import com.example.ecodot.data.local.prefs.AudioQuality
import com.example.ecodot.data.local.prefs.VideoQuality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.AudioStream
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.VideoStream
import java.util.concurrent.atomic.AtomicBoolean

data class ResolvedAudioTelemetry(
    val bitrateBps: Int,
    val sampleRateHz: Int,
    val codec: String?,
    val mimeType: String? = null,
    val itag: Int? = null,
    val viewCount: Long = -1L,
    val likeCount: Long = -1L,
    val description: String? = null,
    val title: String? = null,
    val uploaderName: String? = null,
    val durationSec: Long = -1L
)

class YouTubeStreamResolver(private val client: OkHttpClient) {

    companion object {
        private const val TAG = "YouTubeStreamResolver"
        private val isInitialized = AtomicBoolean(false)

        val resolvedAudioTelemetry = java.util.concurrent.ConcurrentHashMap<String, ResolvedAudioTelemetry>()
    }

    init {
        ensureInitialized()
    }

    private fun ensureInitialized() {
        if (isInitialized.compareAndSet(false, true)) {
            try {
                NewPipe.init(NewPipeDownloader(client))
                Log.d(TAG, "NewPipeExtractor initialized successfully with OkHttpDownloader")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize NewPipeExtractor", e)
                isInitialized.set(false)
            }
        }
    }

    private fun getStreamBitrate(stream: AudioStream): Int {
        val avg = stream.averageBitrate
        return if (avg > 0) avg else stream.bitrate
    }

    private fun isAacStream(stream: AudioStream): Boolean {
        return stream.format == org.schabi.newpipe.extractor.MediaFormat.M4A ||
            stream.codec?.contains("mp4a", ignoreCase = true) == true ||
            stream.itag in setOf(139, 140, 141)
    }

    private fun isOpusStream(stream: AudioStream): Boolean {
        return stream.format == org.schabi.newpipe.extractor.MediaFormat.WEBMA_OPUS ||
            stream.format == org.schabi.newpipe.extractor.MediaFormat.WEBMA ||
            stream.format == org.schabi.newpipe.extractor.MediaFormat.OPUS ||
            stream.codec?.contains("opus", ignoreCase = true) == true
    }

    private fun pickAudioStream(audioStreams: List<AudioStream>, quality: AudioQuality): AudioStream? =
        when (quality) {
            AudioQuality.HIGH -> {
                audioStreams.filter(::isOpusStream).maxByOrNull { getStreamBitrate(it) }
                    ?: audioStreams.maxByOrNull { getStreamBitrate(it) }
            }
            AudioQuality.HIGH_AAC -> {
                audioStreams.filter(::isAacStream).maxByOrNull { getStreamBitrate(it) }
                    ?: audioStreams.maxByOrNull { getStreamBitrate(it) }
            }
            AudioQuality.NORMAL -> {
                audioStreams.minByOrNull {
                    kotlin.math.abs(getStreamBitrate(it) - 128000)
                } ?: audioStreams.firstOrNull()
            }
            AudioQuality.LOW -> {
                audioStreams.minByOrNull { getStreamBitrate(it) }
            }
        }

    private fun inferSampleRateHz(stream: AudioStream): Int {
        val itagRate = stream.itagItem?.sampleRate ?: 0
        if (itagRate >= 8_000) return itagRate
        return if (isAacStream(stream)) 44_100 else 48_000
    }

    private fun cacheTelemetry(
        videoId: String,
        streamInfo: StreamInfo,
        chosenStream: AudioStream
    ): ResolvedAudioTelemetry {
        val resolvedBitrate = getStreamBitrate(chosenStream)
        val mimeType = chosenStream.format?.mimeType
            ?: if (isAacStream(chosenStream)) "audio/mp4" else "audio/webm"
        val cleanCodec = chosenStream.codec?.takeIf { it.isNotBlank() }
            ?: if (isAacStream(chosenStream)) "mp4a.40.2" else "opus"
        val telemetry = ResolvedAudioTelemetry(
            bitrateBps = resolvedBitrate,
            sampleRateHz = inferSampleRateHz(chosenStream),
            codec = cleanCodec,
            mimeType = mimeType,
            itag = chosenStream.itag.takeIf { it > 0 },
            viewCount = streamInfo.viewCount,
            likeCount = streamInfo.likeCount,
            description = streamInfo.description?.content,
            title = streamInfo.name,
            uploaderName = streamInfo.uploaderName,
            durationSec = streamInfo.duration
        )
        resolvedAudioTelemetry[videoId] = telemetry
        return telemetry
    }

    private fun getVideoResolution(stream: VideoStream): Int {
        val res = stream.resolution ?: return 0
        return res.filter { it.isDigit() }.toIntOrNull() ?: 0
    }

    /**
     * Resolves an unthrottled, signature-deciphered audio streaming URL.
     * NewPipeExtractor handles the JavaScript `n`-parameter transform and signature deciphering on-the-fly.
     */
    suspend fun resolveAudioStream(videoId: String, quality: AudioQuality): String? = withContext(Dispatchers.IO) {
        try {
            ensureInitialized()
            val watchUrl = "https://www.youtube.com/watch?v=$videoId"
            val streamInfo = StreamInfo.getInfo(ServiceList.YouTube, watchUrl)
            val audioStreams = streamInfo.audioStreams

            if (audioStreams.isNullOrEmpty()) {
                Log.w(TAG, "No audio streams found by NewPipe for $videoId")
                return@withContext null
            }

            val chosenStream = pickAudioStream(audioStreams, quality)
            val url = chosenStream?.content
            if (!url.isNullOrEmpty()) {
                val meta = cacheTelemetry(videoId, streamInfo, chosenStream)
                Log.d(
                    TAG,
                    "Resolved unthrottled audio stream for $videoId: bitrate=${chosenStream.bitrate}, avg=${meta.bitrateBps}, itag=${meta.itag}, views=${meta.viewCount}"
                )
                return@withContext url
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "NewPipeExtractor failed to resolve stream for $videoId: ${e.message}", e)
            null
        }
    }

    /**
     * Returns synced NewPipe metadata + chosen audio stream specs (bitrate, codec, mime, itag, views, likes).
     */
    suspend fun resolveStreamMetadata(
        videoId: String,
        quality: AudioQuality
    ): ResolvedAudioTelemetry? = withContext(Dispatchers.IO) {
        val existing = resolvedAudioTelemetry[videoId]
        if (existing != null && existing.bitrateBps >= 16_000 && existing.itag != null && existing.viewCount >= 0L) {
            return@withContext existing
        }
        try {
            ensureInitialized()
            val watchUrl = "https://www.youtube.com/watch?v=$videoId"
            val streamInfo = StreamInfo.getInfo(ServiceList.YouTube, watchUrl)
            val audioStreams = streamInfo.audioStreams
            val chosenStream = if (!audioStreams.isNullOrEmpty()) pickAudioStream(audioStreams, quality) else null
            if (chosenStream != null) {
                cacheTelemetry(videoId, streamInfo, chosenStream)
            } else {
                ResolvedAudioTelemetry(
                    bitrateBps = existing?.bitrateBps ?: 0,
                    sampleRateHz = existing?.sampleRateHz ?: 48_000,
                    codec = existing?.codec,
                    mimeType = existing?.mimeType,
                    itag = existing?.itag,
                    viewCount = streamInfo.viewCount,
                    likeCount = streamInfo.likeCount,
                    description = streamInfo.description?.content,
                    title = streamInfo.name,
                    uploaderName = streamInfo.uploaderName,
                    durationSec = streamInfo.duration
                ).also { resolvedAudioTelemetry[videoId] = it }
            }
        } catch (e: Exception) {
            Log.w(TAG, "resolveStreamMetadata failed for $videoId: ${e.message}")
            existing
        }
    }

    /**
     * Resolves an unthrottled high-definition video stream URL.
     * Highest priority: HLS Master Manifest (multi-bitrate 1080p60/1080p/720p with audio).
     * Second priority: DASH Master Manifest (adaptive multi-bitrate).
     * Fallback: Progressive muxed stream.
     */
    suspend fun resolveVideoStream(videoId: String, quality: VideoQuality): String? = withContext(Dispatchers.IO) {
        try {
            ensureInitialized()
            val watchUrl = "https://www.youtube.com/watch?v=$videoId"
            val streamInfo = StreamInfo.getInfo(ServiceList.YouTube, watchUrl)

            // 1. Highest priority: Multi-bitrate 1080p60/1080p/720p HLS Master Manifest (with audio)
            if (!streamInfo.hlsUrl.isNullOrEmpty()) {
                Log.d(TAG, "Selected NewPipe HLS 1080p Master stream for $videoId: ${streamInfo.hlsUrl}")
                return@withContext streamInfo.hlsUrl
            }

            // 2. Secondary priority: DASH Master MPD (1080p adaptive)
            if (!streamInfo.dashMpdUrl.isNullOrEmpty()) {
                Log.d(TAG, "Selected NewPipe DASH 1080p Master stream for $videoId: ${streamInfo.dashMpdUrl}")
                return@withContext streamInfo.dashMpdUrl
            }

            // 3. Fallback: Progressive muxed video streams
            val videoStreams = streamInfo.videoStreams
            if (!videoStreams.isNullOrEmpty()) {
                val chosen: VideoStream? = when (quality) {
                    VideoQuality.HIGH -> videoStreams.maxByOrNull { getVideoResolution(it) }
                    VideoQuality.NORMAL -> videoStreams.minByOrNull {
                        kotlin.math.abs(getVideoResolution(it) - 720)
                    } ?: videoStreams.maxByOrNull { getVideoResolution(it) }
                    VideoQuality.LOW -> videoStreams.minByOrNull {
                        kotlin.math.abs(getVideoResolution(it) - 360)
                    } ?: videoStreams.minByOrNull { getVideoResolution(it) }
                }
                if (chosen?.content != null) {
                    Log.d(TAG, "Selected progressive video stream for $videoId: resolution=${chosen.resolution}")
                    return@withContext chosen.content
                }
            }

            null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resolve video stream for $videoId: ${e.message}", e)
            null
        }
    }
}
