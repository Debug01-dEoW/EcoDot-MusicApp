package com.example.ecodot.ui.components.player

import androidx.compose.ui.graphics.Color
import com.example.ecodot.data.local.prefs.AudioQuality
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerStyleAndProfileTest {

    @Test
    fun `smoothstep clamps inputs and follows cubic easing`() {
        assertEquals(0f, PlayerStyleMath.smoothstep(-0.5f), 0.0001f)
        assertEquals(0f, PlayerStyleMath.smoothstep(0f), 0.0001f)
        assertEquals(0.5f, PlayerStyleMath.smoothstep(0.5f), 0.0001f)
        assertEquals(1f, PlayerStyleMath.smoothstep(1f), 0.0001f)
        assertEquals(1f, PlayerStyleMath.smoothstep(2.0f), 0.0001f)
    }

    @Test
    fun `computeResponsiveVideoFrame preserves landscape 16-9 proportions above controls`() {
        val bounds = PlayerStyleMath.computeResponsiveVideoFrame(
            maxWidthDp = 400f,
            maxHeightDp = 500f,
            topChromeDp = 64f,
            bottomGapDp = 12f,
            aspectRatio = 16f / 9f
        )
        assertFalse(bounds.isPortrait)
        assertEquals(400f, bounds.widthDp, 0.5f)
        assertEquals(225f, bounds.heightDp, 0.5f)
        assertTrue(bounds.topDp >= 64f)
    }

    @Test
    fun `computeResponsiveVideoFrame fits tall 9-16 video within height budget`() {
        val bounds = PlayerStyleMath.computeResponsiveVideoFrame(
            maxWidthDp = 400f,
            maxHeightDp = 420f,
            topChromeDp = 60f,
            bottomGapDp = 12f,
            aspectRatio = 9f / 16f
        )
        assertTrue(bounds.isPortrait)
        // Available height = 420 - 60 - 12 = 348
        assertEquals(348f, bounds.heightDp, 0.5f)
        assertEquals(348f * (9f / 16f), bounds.widthDp, 0.5f)
        assertTrue(bounds.topDp >= 60f)
    }

    @Test
    fun `vinyl tonearm sweeps from outer groove to inner label during playback`() {
        val pausedAngle = PlayerStyleMath.computeVinylTonearmAngle(isPlaying = false, playbackFraction = 0.7f, restAngle = -24f)
        assertEquals(-24f, pausedAngle, 0.001f)

        val startAngle = PlayerStyleMath.computeVinylTonearmAngle(isPlaying = true, playbackFraction = 0f, startPlayAngle = 14f, endPlayAngle = 29f)
        val midAngle = PlayerStyleMath.computeVinylTonearmAngle(isPlaying = true, playbackFraction = 0.5f, startPlayAngle = 14f, endPlayAngle = 29f)
        val endAngle = PlayerStyleMath.computeVinylTonearmAngle(isPlaying = true, playbackFraction = 1f, startPlayAngle = 14f, endPlayAngle = 29f)

        assertEquals(14f, startAngle, 0.001f)
        assertEquals(21.5f, midAngle, 0.001f)
        assertEquals(29f, endAngle, 0.001f)
    }

    @Test
    fun `formatAudioStreamBadge formats AAC, Opus, and downloaded prefixes accurately`() {
        val aac = PlayerStyleMath.formatAudioStreamBadge(
            bitrateBps = 256_000,
            sampleRateHz = 48_000,
            mimeType = "audio/mp4a-latm",
            codecs = "mp4a.40.2",
            fallbackQuality = AudioQuality.HIGH
        )
        assertEquals("256 kbps AAC · 48 kHz", aac)

        val opusOffline = PlayerStyleMath.formatAudioStreamBadge(
            bitrateBps = 160_000,
            sampleRateHz = 44_100,
            mimeType = "audio/opus",
            codecs = "opus",
            fallbackQuality = AudioQuality.HIGH,
            isLocalOrDownloaded = true
        )
        assertEquals("Downloaded · 160 kbps Opus · 44.1 kHz", opusOffline)

        val fallbackOnly = PlayerStyleMath.formatAudioStreamBadge(
            bitrateBps = 0,
            sampleRateHz = 0,
            mimeType = null,
            codecs = null,
            fallbackQuality = AudioQuality.HIGH_AAC
        )
        assertEquals("256 kbps AAC", fallbackOnly)

        val opusFallback = PlayerStyleMath.formatAudioStreamBadge(
            bitrateBps = 0,
            sampleRateHz = 0,
            mimeType = null,
            codecs = null,
            fallbackQuality = AudioQuality.HIGH
        )
        assertEquals("256 kbps Opus", opusFallback)
    }

    @Test
    fun `formatRealTimeAudioDetail formats real kbps and khz cleanly`() {
        assertEquals("140 kbps · 48 kHz", PlayerStyleMath.formatRealTimeAudioDetail(140_000, 48_000))
        assertEquals("129 kbps · 44.1 kHz", PlayerStyleMath.formatRealTimeAudioDetail(129_000, 44_100))
        assertEquals("48 kHz", PlayerStyleMath.formatRealTimeAudioDetail(0, 48_000))
        assertEquals(null, PlayerStyleMath.formatRealTimeAudioDetail(0, 0))
    }

    @Test
    fun `formatListeningTime handles zero, minutes, hours and 100plus hours`() {
        assertEquals("0m", PlayerStyleMath.formatListeningTime(-5))
        assertEquals("42m", PlayerStyleMath.formatListeningTime(42))
        assertEquals("2h 15m", PlayerStyleMath.formatListeningTime(135))
        assertEquals("105h", PlayerStyleMath.formatListeningTime(105 * 60 + 20))
    }

    @Test
    fun `parseFavoriteGenres trims whitespace and removes case-insensitive duplicates`() {
        val parsed = PlayerStyleMath.parseFavoriteGenres(" Lo-Fi , pop, Pop , Indie Rock ,, ambient ")
        assertEquals(listOf("Lo-Fi", "pop", "Indie Rock", "ambient"), parsed)
    }

    @Test
    fun `buildSmoothedMeshPixels produces smooth gradient across output dimensions`() {
        val srcWidth = 12
        val srcHeight = 12
        val src = IntArray(srcWidth * srcHeight) { idx ->
            if (idx < (srcWidth * srcHeight) / 2) 0xFFFF2244.toInt() else 0xFF2244FF.toInt()
        }
        val out = PlayerStyleMath.buildSmoothedMeshPixels(src, srcWidth, srcHeight, gridSize = 6, outSize = 32)
        assertEquals(32 * 32, out.size)
        // Top and bottom rows should differ distinctly from source red vs blue split
        assertTrue(out.first() != out.last())
    }

    @Test
    fun `appleMusicGradientColorAt darkens progressively from top to bottom`() {
        val seed = Color(0.8f, 0.4f, 0.3f)
        val top = PlayerStyleMath.appleMusicGradientColorAt(seed, 0f)
        val mid = PlayerStyleMath.appleMusicGradientColorAt(seed, 0.48f)
        val bottom = PlayerStyleMath.appleMusicGradientColorAt(seed, 1f)

        assertTrue(top.red > mid.red)
        assertTrue(mid.red > bottom.red)
    }
}
