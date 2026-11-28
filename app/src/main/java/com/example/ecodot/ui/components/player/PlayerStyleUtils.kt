package com.example.ecodot.ui.components.player

import android.content.Context
import android.database.ContentObserver
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.ecodot.data.local.prefs.AudioQuality
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Pure formatting and layout math helpers shared across ProfileScreen and all Now Playing styles.
 * Kept platform-independent where possible so JVM unit tests verify math directly.
 */
object PlayerStyleMath {

    /**
     * Smoothstep curve matching SimpMusic's Apple Music scrim and mask easing: 3t^2 - 2t^3.
     */
    fun smoothstep(t: Float): Float {
        val x = t.coerceIn(0f, 1f)
        return x * x * (3f - 2f * x)
    }

    /**
     * Computes responsive video frame dimensions inside an available container above controls,
     * preserving true video aspect ratio while respecting [topChromeDp] and [bottomGapDp].
     */
    fun computeResponsiveVideoFrame(
        maxWidthDp: Float,
        maxHeightDp: Float,
        topChromeDp: Float,
        bottomGapDp: Float,
        aspectRatio: Float
    ): VideoFrameBounds {
        val safeRatio = aspectRatio.coerceIn(0.45f, 2.4f)
        val availHeight = (maxHeightDp - topChromeDp - bottomGapDp).coerceAtLeast(0f)
        if (maxWidthDp <= 0f || availHeight <= 0f) {
            return VideoFrameBounds(widthDp = 0f, heightDp = 0f, topDp = topChromeDp, isPortrait = safeRatio < 1f)
        }
        val frameHeight = minOf(maxWidthDp / safeRatio, availHeight)
        val frameWidth = frameHeight * safeRatio
        val centeredTop = (maxHeightDp - frameHeight) / 2f
        val frameTop = maxOf(centeredTop, topChromeDp)
        return VideoFrameBounds(
            widthDp = frameWidth,
            heightDp = frameHeight,
            topDp = frameTop,
            isPortrait = safeRatio < 1f
        )
    }

    data class VideoFrameBounds(
        val widthDp: Float,
        val heightDp: Float,
        val topDp: Float,
        val isPortrait: Boolean
    )

    /**
     * Computes Vinyl tonearm sweep angle (degrees).
     * Lifted off record at [restAngle] (0f) when paused; when playing, sweeps smoothly
     * from outer groove ([startPlayAngle] = 22f) to inner groove ([endPlayAngle] = 35f).
     */
    fun computeVinylTonearmAngle(
        isPlaying: Boolean,
        playbackFraction: Float,
        restAngle: Float = 0f,
        startPlayAngle: Float = 22f,
        endPlayAngle: Float = 35f
    ): Float {
        if (!isPlaying) return restAngle
        val clamped = playbackFraction.coerceIn(0f, 1f)
        return startPlayAngle + (endPlayAngle - startPlayAngle) * clamped
    }

    /**
     * Builds a smooth multi-stop vertical gradient matching SimpMusic's smoothScrimBrush.
     */
    fun smoothScrimBrush(
        from: Color,
        to: Color,
        startFraction: Float = 0f,
        endFraction: Float = 1f,
        steps: Int = 18
    ): Brush {
        val safeStart = startFraction.coerceIn(0f, 1f)
        val safeEnd = endFraction.coerceIn(safeStart + 0.001f, 1f)
        val stops = Array(steps + 1) { i ->
            val p = i / steps.toFloat()
            val pos = safeStart + (safeEnd - safeStart) * p
            val eased = smoothstep(p)
            pos to Color(
                red = from.red + (to.red - from.red) * eased,
                green = from.green + (to.green - from.green) * eased,
                blue = from.blue + (to.blue - from.blue) * eased,
                alpha = from.alpha + (to.alpha - from.alpha) * eased
            )
        }
        return Brush.verticalGradient(colorStops = stops)
    }

    /**
     * Derives Apple Music's 3-stop vertical backdrop tone from a seed color:
     * fraction 0f -> vivid top, 0.48f -> medium darkened body, 1f -> deep warm near-black.
     */
    fun appleMusicGradientColorAt(seedColor: Color, fraction: Float): Color {
        val t = fraction.coerceIn(0f, 1f)
        val darkening = when {
            t <= 0.48f -> {
                val local = smoothstep(t / 0.48f)
                0.78f - (0.78f - 0.38f) * local
            }
            else -> {
                val local = smoothstep((t - 0.48f) / 0.52f)
                0.38f - (0.38f - 0.14f) * local
            }
        }
        val r = (seedColor.red * darkening).coerceIn(0.03f, 1f)
        val g = (seedColor.green * darkening).coerceIn(0.03f, 1f)
        val b = (seedColor.blue * darkening).coerceIn(0.03f, 1f)
        return Color(red = r, green = g, blue = b, alpha = 1f)
    }

    /**
     * Normalizes stream bitrate values from mixed extractors/muxers into bits per second (bps).
     * NewPipeExtractor reports `AudioStream.averageBitrate`/`bitrate` in kbps (e.g. 128, 160),
     * whereas InnerTube and ExoPlayer Formats use bps (e.g. 128_000, 160_000).
     */
    fun normalizeBitrateBps(rawBitrate: Int): Int {
        if (rawBitrate <= 0) return 0
        return if (rawBitrate in 1..2000) rawBitrate * 1000 else rawBitrate
    }

    /**
     * Formats normalized bitrate into human-readable `"160 kbps"` labels.
     */
    fun formatBitrateKbpsLabel(rawBitrateBps: Int): String? {
        val bps = normalizeBitrateBps(rawBitrateBps)
        if (bps <= 0) return null
        return "${(bps / 1000).coerceAtLeast(1)} kbps"
    }

    /**
     * Formats ExoPlayer audio stream telemetry into a crisp Apple Music / Hi-Fi badge label.
     */
    fun formatAudioStreamBadge(
        bitrateBps: Int,
        sampleRateHz: Int,
        mimeType: String?,
        codecs: String?,
        fallbackQuality: AudioQuality,
        isLocalOrDownloaded: Boolean = false
    ): String {
        val normalizedBps = normalizeBitrateBps(bitrateBps)
        val codecTag = when {
            codecs?.contains("opus", ignoreCase = true) == true || mimeType?.contains("opus", true) == true -> "Opus"
            codecs?.contains("mp4a", ignoreCase = true) == true || mimeType?.contains("mp4a", true) == true -> "AAC"
            mimeType?.contains("flac", true) == true -> "Lossless"
            mimeType?.contains("mpeg", true) == true -> "MP3"
            else -> when (fallbackQuality) {
                AudioQuality.HIGH_AAC -> "AAC"
                AudioQuality.HIGH -> "Opus"
                AudioQuality.NORMAL -> "AAC"
                AudioQuality.LOW -> "Opus"
            }
        }
        val kbps = when {
            normalizedBps > 0 -> "${(normalizedBps / 1000).coerceAtLeast(32)} kbps"
            fallbackQuality == AudioQuality.HIGH_AAC -> "256 kbps"
            fallbackQuality == AudioQuality.HIGH -> "256 kbps"
            fallbackQuality == AudioQuality.NORMAL -> "128 kbps"
            else -> "48 kbps"
        }
        val rateTag = if (sampleRateHz >= 44100) {
            val khz = if (sampleRateHz % 1000 == 0) "${sampleRateHz / 1000} kHz" else "${(sampleRateHz / 100) / 10.0} kHz"
            " · $khz"
        } else ""
        val prefix = if (isLocalOrDownloaded) "Downloaded · " else ""
        return "$prefix$kbps $codecTag$rateTag"
    }

    /**
     * Formats real-time playing stream bitrate and sample rate (`140 kbps · 48 kHz`).
     * Normalizes mixed kbps/bps units and returns null when neither value is available.
     */
    fun formatRealTimeAudioDetail(bitrateBps: Int, sampleRateHz: Int): String? {
        val parts = mutableListOf<String>()
        formatBitrateKbpsLabel(bitrateBps)?.let { parts.add(it) }
        if (sampleRateHz >= 8000) {
            val khz = if (sampleRateHz % 1000 == 0) {
                "${sampleRateHz / 1000} kHz"
            } else {
                "${(sampleRateHz / 100) / 10.0} kHz"
            }
            parts.add(khz)
        }
        return if (parts.isEmpty()) null else parts.joinToString(" · ")
    }

    /**
     * Formats listening duration minutes for ProfileScreen insights cards.
     */
    fun formatListeningTime(totalMinutes: Int): String {
        val safe = totalMinutes.coerceAtLeast(0)
        val hours = safe / 60
        val mins = safe % 60
        return when {
            hours >= 100 -> "${hours}h"
            hours > 0 -> "${hours}h ${mins}m"
            else -> "${mins}m"
        }
    }

    /**
     * Parses comma-separated favorite genres into clean unique display pills.
     */
    fun parseFavoriteGenres(rawGenres: String): List<String> =
        rawGenres.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinctBy { it.lowercase() }

    /**
     * Bilinear smoothstep resample + HSL saturation lift used by AppleMusicMeshBackdrop.
     * Takes row-major ARGB pixels ([srcWidth] x [srcHeight]) and builds a [gridSize]x[gridSize]
     * mesh smoothed into [outSize]x[outSize] ARGB pixels.
     */
    fun buildSmoothedMeshPixels(
        srcPixels: IntArray,
        srcWidth: Int,
        srcHeight: Int,
        gridSize: Int = 6,
        outSize: Int = 32
    ): IntArray {
        if (srcPixels.isEmpty() || srcWidth <= 0 || srcHeight <= 0 || gridSize <= 0 || outSize <= 0) {
            return IntArray(outSize.coerceAtLeast(1) * outSize.coerceAtLeast(1)) { 0xFF121216.toInt() }
        }
        val grid = IntArray(gridSize * gridSize)
        val cellW = max(1, srcWidth / gridSize)
        val cellH = max(1, srcHeight / gridSize)
        for (gy in 0 until gridSize) {
            val yStart = gy * cellH
            val yEnd = if (gy == gridSize - 1) srcHeight else min(srcHeight, yStart + cellH)
            for (gx in 0 until gridSize) {
                val xStart = gx * cellW
                val xEnd = if (gx == gridSize - 1) srcWidth else min(srcWidth, xStart + cellW)
                var rSum = 0L
                var gSum = 0L
                var bSum = 0L
                var count = 0L
                for (y in yStart until yEnd) {
                    val rowOffset = y * srcWidth
                    for (x in xStart until xEnd) {
                        val c = srcPixels[rowOffset + x]
                        rSum += (c shr 16) and 0xFF
                        gSum += (c shr 8) and 0xFF
                        bSum += c and 0xFF
                        count++
                    }
                }
                val safeCount = count.coerceAtLeast(1L)
                val avgArgb = (0xFF shl 24) or
                    (((rSum / safeCount).toInt() and 0xFF) shl 16) or
                    (((gSum / safeCount).toInt() and 0xFF) shl 8) or
                    ((bSum / safeCount).toInt() and 0xFF)
                grid[gy * gridSize + gx] = liftMeshArgb(avgArgb)
            }
        }

        val out = IntArray(outSize * outSize)
        val maxIdx = (gridSize - 1).toFloat()
        for (oy in 0 until outSize) {
            val fy = if (outSize == 1) 0f else (oy / (outSize - 1).toFloat()) * maxIdx
            val y0 = fy.toInt().coerceIn(0, gridSize - 1)
            val y1 = min(gridSize - 1, y0 + 1)
            val ty = smoothstep(fy - y0)
            for (ox in 0 until outSize) {
                val fx = if (outSize == 1) 0f else (ox / (outSize - 1).toFloat()) * maxIdx
                val x0 = fx.toInt().coerceIn(0, gridSize - 1)
                val x1 = min(gridSize - 1, x0 + 1)
                val tx = smoothstep(fx - x0)

                val top = lerpArgb(grid[y0 * gridSize + x0], grid[y0 * gridSize + x1], tx)
                val bot = lerpArgb(grid[y1 * gridSize + x0], grid[y1 * gridSize + x1], tx)
                out[oy * outSize + ox] = lerpArgb(top, bot, ty)
            }
        }
        return out
    }

    private fun lerpArgb(from: Int, to: Int, t: Float): Int {
        if (t <= 0f) return from
        if (t >= 1f) return to
        fun ch(shift: Int): Int {
            val a = (from shr shift) and 0xFF
            val b = (to shr shift) and 0xFF
            return (a + (b - a) * t).roundToInt().coerceIn(0, 255)
        }
        return (0xFF shl 24) or (ch(16) shl 16) or (ch(8) shl 8) or ch(0)
    }

    private fun liftMeshArgb(argb: Int): Int {
        val r = ((argb shr 16) and 0xFF) / 255f
        val g = ((argb shr 8) and 0xFF) / 255f
        val b = (argb and 0xFF) / 255f
        val maxC = max(r, max(g, b))
        val minC = min(r, min(g, b))
        val lightness = ((maxC + minC) / 2f).coerceIn(0.055f, 0.88f)
        val delta = maxC - minC
        if (delta == 0f) {
            val v = (lightness * 255f).roundToInt()
            return (0xFF shl 24) or (v shl 16) or (v shl 8) or v
        }
        val saturation = (delta / (1f - abs(2f * lightness - 1f))).let { (it * 1.15f).coerceIn(0f, 1f) }
        val hue = when (maxC) {
            r -> ((g - b) / delta).let { if (it < 0f) it + 6f else it }
            g -> (b - r) / delta + 2f
            else -> (r - g) / delta + 4f
        } * 60f
        val c = Color.hsl(hue.mod(360f), saturation, lightness)
        return (0xFF shl 24) or
            (((c.red * 255f).roundToInt().coerceIn(0, 255)) shl 16) or
            (((c.green * 255f).roundToInt().coerceIn(0, 255)) shl 8) or
            ((c.blue * 255f).roundToInt().coerceIn(0, 255))
    }
}

/**
 * Resolves the active hardware audio output device label for Apple Music / Spotify footers.
 */
fun queryActiveAudioOutputLabel(context: Context): String {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        ?: return "Phone Speaker"
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val outputs = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
        val btDevice = outputs.firstOrNull {
            it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                it.type == AudioDeviceInfo.TYPE_BLE_HEADSET ||
                it.type == AudioDeviceInfo.TYPE_BLE_SPEAKER ||
                it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
        }
        if (btDevice != null) {
            val name = btDevice.productName?.toString()?.trim()
            return name?.takeIf { it.isNotEmpty() } ?: "Bluetooth Audio"
        }
        val wiredOrUsb = outputs.firstOrNull {
            it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                it.type == AudioDeviceInfo.TYPE_USB_HEADSET ||
                it.type == AudioDeviceInfo.TYPE_USB_DEVICE
        }
        if (wiredOrUsb != null) {
            return if (wiredOrUsb.type == AudioDeviceInfo.TYPE_USB_HEADSET || wiredOrUsb.type == AudioDeviceInfo.TYPE_USB_DEVICE) {
                wiredOrUsb.productName?.toString()?.trim()?.takeIf { it.isNotEmpty() } ?: "USB Audio"
            } else {
                "Wired Headphones"
            }
        }
    }
    val deviceModel = Build.MODEL?.trim()?.takeIf { it.isNotEmpty() }
    return deviceModel ?: "Phone Speaker"
}

/**
 * Observes real Android media volume (`AudioManager.STREAM_MUSIC`) with live system observer.
 */
@Composable
fun rememberSystemMediaVolumeController(): Pair<Float, (Float) -> Unit> {
    val context = LocalContext.current
    val audioManager = remember(context) {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    val maxVol = remember(audioManager) { audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1) }
    var volumeFraction by remember {
        mutableFloatStateOf(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxVol.toFloat())
    }

    DisposableEffect(context, audioManager) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                volumeFraction = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat() / maxVol.toFloat()
            }
        }
        context.contentResolver.registerContentObserver(Settings.System.CONTENT_URI, true, observer)
        onDispose {
            context.contentResolver.unregisterContentObserver(observer)
        }
    }

    val setVolume: (Float) -> Unit = { fraction ->
        val clamped = fraction.coerceIn(0f, 1f)
        volumeFraction = clamped
        val targetStep = (clamped * maxVol).roundToInt().coerceIn(0, maxVol)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetStep, 0)
    }

    return volumeFraction to setVolume
}

@Composable
fun rememberActiveAudioOutputName(): State<String> {
    val context = LocalContext.current
    val outputNameState = remember { mutableStateOf(queryActiveAudioOutputLabel(context)) }
    DisposableEffect(context) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        val callback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && audioManager != null) {
            object : android.media.AudioDeviceCallback() {
                override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
                    outputNameState.value = queryActiveAudioOutputLabel(context)
                }

                override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
                    outputNameState.value = queryActiveAudioOutputLabel(context)
                }
            }
        } else null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && callback != null) {
            audioManager?.registerAudioDeviceCallback(callback, Handler(Looper.getMainLooper()))
        }
        onDispose {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && callback != null) {
                audioManager?.unregisterAudioDeviceCallback(callback)
            }
        }
    }
    return outputNameState
}
