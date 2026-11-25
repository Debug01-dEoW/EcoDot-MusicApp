package com.example.ecodot.recognition

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.random.Random

sealed interface Recognition {
    data object Listening : Recognition
    data object Identifying : Recognition
    data class Found(
        val title: String,
        val artist: String,
        val coverArt: String?
    ) : Recognition
    data object NoMatch : Recognition
    data object Failed : Recognition
}

object SongRecognizer {
    private const val TAG = "SongRecognizer"

    private const val RECORD_SAMPLE_RATE = 44_100
    private const val RECORD_SECONDS = 10
    private const val MAX_ATTEMPTS = 3

    private val userAgents = listOf(
        "Dalvik/2.1.0 (Linux; U; Android 5.0.2; VS980 4G Build/LRX22G)",
        "Dalvik/1.6.0 (Linux; U; Android 4.4.2; SM-T210 Build/KOT49H)",
        "Dalvik/2.1.0 (Linux; U; Android 5.1.1; SM-P905V Build/LMY47X)",
        "Dalvik/2.1.0 (Linux; U; Android 6.0.1; SM-G920F Build/MMB29K)",
        "Dalvik/2.1.0 (Linux; U; Android 5.0; SM-G900F Build/LRX21T)"
    )

    private val timezones = listOf(
        "Europe/Paris", "Europe/London", "America/New_York", "America/Los_Angeles", "Asia/Tokyo", "Asia/Dubai", "Asia/Kolkata"
    )

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(25, TimeUnit.SECONDS)
            .build()
    }

    suspend fun recognize(onIdentifying: () -> Unit): Recognition = try {
        val pcm = record()
        onIdentifying()
        val signature = withContext(Dispatchers.Default) {
            ShazamSignature.fromPcm(pcm, RECORD_SAMPLE_RATE)
        }
        ask(signature, sampleMs = pcm.size * 1000L / RECORD_SAMPLE_RATE)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.e(TAG, "Recognition failed", e)
        Recognition.Failed
    }

    @SuppressLint("MissingPermission")
    private suspend fun record(): ShortArray = withContext(Dispatchers.IO) {
        val format = AudioFormat.ENCODING_PCM_16BIT
        val channel = AudioFormat.CHANNEL_IN_MONO
        val bufferSize = AudioRecord.getMinBufferSize(RECORD_SAMPLE_RATE, channel, format)
        val recorder = AudioRecord(MediaRecorder.AudioSource.MIC, RECORD_SAMPLE_RATE, channel, format, bufferSize)
        if (recorder.state != AudioRecord.STATE_INITIALIZED) {
            recorder.release()
            error("Microphone unavailable")
        }
        val pcm = ShortArray(RECORD_SAMPLE_RATE * RECORD_SECONDS)
        var filled = 0
        try {
            recorder.startRecording()
            while (filled < pcm.size && isActive) {
                val read = recorder.read(pcm, filled, minOf(bufferSize / 2, pcm.size - filled))
                if (read < 0) error("AudioRecord.read failed: $read")
                filled += read
            }
        } finally {
            try {
                recorder.stop()
            } catch (_: Exception) {}
            recorder.release()
        }
        pcm.copyOf(filled)
    }

    private suspend fun ask(signature: String, sampleMs: Long): Recognition = withContext(Dispatchers.IO) {
        repeat(MAX_ATTEMPTS) { attempt ->
            try {
                val url = "https://amp.shazam.com/discovery/v5/en/US/android/-/tag/" +
                        "${UUID.randomUUID().toString().uppercase()}/${UUID.randomUUID()}" +
                        "?sync=true&webv3=true&sampling=true&connected=&shazamapiversion=v3&sharehub=true&video=v3"

                val bodyStr = requestBody(signature, sampleMs)
                val requestBody = bodyStr.toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .addHeader("User-Agent", userAgents.random())
                    .addHeader("Content-Language", "en_US")
                    .build()

                val response = client.newCall(request).execute()
                val code = response.code
                val responseText = response.body?.string().orEmpty()

                when {
                    response.isSuccessful -> return@withContext parse(responseText)
                    code == 404 -> {
                        Log.i(TAG, "No match found")
                        return@withContext Recognition.NoMatch
                    }
                    code == 429 && attempt < MAX_ATTEMPTS - 1 -> {
                        Log.w(TAG, "Rate limited, retrying in ${2000L shl attempt}ms")
                        delay(2000L shl attempt)
                    }
                    else -> {
                        Log.w(TAG, "Shazam answered HTTP $code: $responseText")
                        return@withContext Recognition.Failed
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.w(TAG, "Attempt $attempt error: ${e.message}")
            }
        }
        Recognition.Failed
    }

    private fun requestBody(signature: String, sampleMs: Long): String {
        val now = System.currentTimeMillis() / 1000
        val geo = JSONObject().apply {
            put("altitude", Random.nextDouble() * 400 + 100)
            put("latitude", Random.nextDouble() * 180 - 90)
            put("longitude", Random.nextDouble() * 360 - 180)
        }
        val sig = JSONObject().apply {
            put("samplems", sampleMs)
            put("timestamp", now)
            put("uri", signature)
        }
        val root = JSONObject().apply {
            put("geolocation", geo)
            put("signature", sig)
            put("timestamp", now)
            put("timezone", timezones.random())
        }
        return root.toString()
    }

    private fun parse(body: String): Recognition {
        return try {
            val json = JSONObject(body)
            val track = json.optJSONObject("track")
            val title = track?.optString("title")
            if (track == null || title.isNullOrBlank()) {
                Log.i(TAG, "No track in response")
                return Recognition.NoMatch
            }

            val subtitle = track.optString("subtitle", "")
            val images = track.optJSONObject("images")
            val coverArt = images?.optString("coverarthq")?.takeIf { it.isNotEmpty() }
                ?: images?.optString("coverart")?.takeIf { it.isNotEmpty() }

            Log.i(TAG, "Recognized song: $title by $subtitle")
            Recognition.Found(
                title = title,
                artist = subtitle,
                coverArt = coverArt
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Shazam response", e)
            Recognition.Failed
        }
    }
}
