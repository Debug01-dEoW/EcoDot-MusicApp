package com.example.ecodot.data.remote

import android.util.Log
import com.example.ecodot.data.local.prefs.AudioQuality
import com.example.ecodot.data.local.prefs.VideoQuality
import com.example.ecodot.ui.components.player.PlayerStyleMath
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
    val itag: Int? = null
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

    private fun getStreamBitrateBps(stream: AudioStream): Int {
        val rawAvg = stream.averageBitrate
        val raw = if (rawAvg > 0) rawAvg else stream.bitrate
        val fallbackItagBitrate = stream.itagItem?.avgBitrate ?: 0
        return PlayerStyleMath.normalizeBitrateBps(if (raw > 0) raw else fallbackItagBitrate)
    }

    private fun getStreamSampleRateHz(stream: AudioStream): Int {
        val itagRate = stream.itagItem?.sampleRate?.takeIf { it > 0 }
        return itagRate ?: if (isOpusStream(stream)) 48_000 else 44_100
    }

    private fun selectBestAudioStream(
        audioStreams: List<AudioStream>,
        quality: AudioQuality
    ): AudioStream? = when (quality) {
        AudioQuality.HIGH -> {
            audioStreams.filter(::isOpusStream).maxByOrNull { getStreamBitrateBps(it) }
                ?: audioStreams.maxByOrNull { getStreamBitrateBps(it) }
        }
        AudioQuality.HIGH_AAC -> {
            audioStreams.filter(::isAacStream).maxByOrNull { getStreamBitrateBps(it) }
                ?: audioStreams.maxByOrNull { getStreamBitrateBps(it) }
        }
        AudioQuality.NORMAL -> {
            audioStreams.minByOrNull {
                kotlin.math.abs(getStreamBitrateBps(it) - 128_000)
            } ?: audioStreams.firstOrNull()
        }
        AudioQuality.LOW -> {
            audioStreams.minByOrNull { getStreamBitrateBps(it) }
        }
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

            val chosenStream = selectBestAudioStream(audioStreams, quality)

            val url = chosenStream?.content
            if (!url.isNullOrEmpty()) {
                val resolvedBitrate = getStreamBitrateBps(chosenStream)
                val resolvedRate = getStreamSampleRateHz(chosenStream)
                if (resolvedBitrate > 0 || chosenStream.itag > 0) {
                    resolvedAudioTelemetry[videoId] = ResolvedAudioTelemetry(
                        bitrateBps = resolvedBitrate,
                        sampleRateHz = resolvedRate,
                        codec = chosenStream.codec,
                        mimeType = chosenStream.format?.mimeType,
                        itag = chosenStream.itag.takeIf { it > 0 }
                    )
                }
                Log.d(TAG, "Resolved unthrottled audio stream for $videoId: rawAvg=${chosenStream.averageBitrate}, bps=$resolvedBitrate, rate=$resolvedRate, itag=${chosenStream.itag}")
                return@withContext url
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "NewPipeExtractor failed to resolve stream for $videoId: ${e.message}", e)
            null
        }
    }

    /**
     * Fetches real YouTube metadata + matching audio stream specs via NewPipeExtractor for Song Info.
     */
    suspend fun resolveTrackMetadata(videoId: String, quality: AudioQuality): VideoInfo? = withContext(Dispatchers.IO) {
        try {
            ensureInitialized()
            val watchUrl = "https://www.youtube.com/watch?v=$videoId"
            val streamInfo = StreamInfo.getInfo(ServiceList.YouTube, watchUrl)
            val audioStreams = streamInfo.audioStreams.orEmpty()
            val chosenStream = selectBestAudioStream(audioStreams, quality)

            val cachedTelemetry = resolvedAudioTelemetry[videoId]
            val bitrateBps = chosenStream?.let { getStreamBitrateBps(it) }?.takeIf { it > 0 }
                ?: cachedTelemetry?.bitrateBps?.takeIf { it > 0 }
                ?: 0
            val sampleRateHz = chosenStream?.let { getStreamSampleRateHz(it) }?.takeIf { it > 0 }
                ?: cachedTelemetry?.sampleRateHz?.takeIf { it > 0 }
                ?: 48_000
            val codec = chosenStream?.codec?.takeIf { it.isNotBlank() }
                ?: cachedTelemetry?.codec?.takeIf { it.isNotBlank() }
                ?: if (quality == AudioQuality.HIGH_AAC) "mp4a.40.2" else "opus"
            val mimeType = chosenStream?.format?.mimeType?.takeIf { it.isNotBlank() }
                ?: cachedTelemetry?.mimeType?.takeIf { it.isNotBlank() }
                ?: if (codec.contains("mp4a", ignoreCase = true)) "audio/mp4" else "audio/webm"
            val itagStr = chosenStream?.itag?.takeIf { it > 0 }?.toString()
                ?: cachedTelemetry?.itag?.takeIf { it > 0 }?.toString()
                ?: "—"

            val viewCountRaw = streamInfo.viewCount
            val likeCountRaw = streamInfo.likeCount
            val durationSec = streamInfo.duration.coerceAtLeast(0L)
            val rawDesc = streamInfo.description?.content.orEmpty()
                .replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
                .replace(Regex("<[^>]+>"), "")
                .replace("&amp;", "&")
                .replace("&#39;", "'")
                .replace("&quot;", "\"")
                .trim()

            VideoInfo(
                videoId = videoId,
                title = streamInfo.name.orEmpty(),
                artist = streamInfo.uploaderName.orEmpty(),
                album = "",
                viewCount = if (viewCountRaw >= 0L) "%,d".format(viewCountRaw) else "—",
                likeCount = if (likeCountRaw > 0L) "%,d".format(likeCountRaw) else "—",
                description = rawDesc,
                mimeType = mimeType,
                codec = codec,
                bitrate = PlayerStyleMath.formatBitrateKbpsLabel(bitrateBps) ?: "—",
                itag = itagStr,
                durationSeconds = if (durationSec > 0L) "%d:%02d".format(durationSec / 60, durationSec % 60) else "—",
                sampleRate = if (sampleRateHz % 1000 == 0) "${sampleRateHz / 1000} kHz" else "${(sampleRateHz / 100) / 10.0} kHz",
                channelName = streamInfo.uploaderName.orEmpty(),
                likeCountRaw = likeCountRaw,
                viewCountRaw = viewCountRaw
            )
        } catch (e: Exception) {
            Log.w(TAG, "resolveTrackMetadata via NewPipe failed for $videoId: ${e.message}")
            null
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
