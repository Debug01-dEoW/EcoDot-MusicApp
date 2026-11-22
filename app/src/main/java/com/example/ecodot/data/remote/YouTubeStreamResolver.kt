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

class YouTubeStreamResolver(private val client: OkHttpClient) {

    companion object {
        private const val TAG = "YouTubeStreamResolver"
        private val isInitialized = AtomicBoolean(false)
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

            val chosenStream: AudioStream? = when (quality) {
                AudioQuality.HIGH -> {
                    audioStreams.maxByOrNull { getStreamBitrate(it) }
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

            val url = chosenStream?.content
            if (!url.isNullOrEmpty()) {
                Log.d(TAG, "Resolved unthrottled audio stream for $videoId: bitrate=${chosenStream.bitrate}, format=${chosenStream.format}")
                return@withContext url
            }
            null
        } catch (e: Exception) {
            Log.e(TAG, "NewPipeExtractor failed to resolve stream for $videoId: ${e.message}", e)
            null
        }
    }

    /**
     * Resolves an unthrottled video stream URL.
     */
    suspend fun resolveVideoStream(videoId: String, quality: VideoQuality): String? = withContext(Dispatchers.IO) {
        try {
            ensureInitialized()
            val watchUrl = "https://www.youtube.com/watch?v=$videoId"
            val streamInfo = StreamInfo.getInfo(ServiceList.YouTube, watchUrl)
            val videoStreams = streamInfo.videoStreams

            if (videoStreams.isNullOrEmpty()) {
                return@withContext null
            }

            val chosen: VideoStream? = when (quality) {
                VideoQuality.HIGH -> videoStreams.maxByOrNull { getVideoResolution(it) }
                VideoQuality.NORMAL -> videoStreams.minByOrNull {
                    kotlin.math.abs(getVideoResolution(it) - 720)
                }
                VideoQuality.LOW -> videoStreams.minByOrNull {
                    kotlin.math.abs(getVideoResolution(it) - 360)
                }
            }

            chosen?.content
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resolve video stream for $videoId: ${e.message}", e)
            null
        }
    }
}
