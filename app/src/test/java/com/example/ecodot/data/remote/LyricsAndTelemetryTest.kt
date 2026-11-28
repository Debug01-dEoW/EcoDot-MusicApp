package com.example.ecodot.data.remote

import com.example.ecodot.ui.components.player.PlayerStyleMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LyricsAndTelemetryTest {

    @Test
    fun `normalizeBitrateBps converts NewPipe kbps to bps while preserving bps`() {
        assertEquals(0, PlayerStyleMath.normalizeBitrateBps(0))
        assertEquals(0, PlayerStyleMath.normalizeBitrateBps(-120))
        assertEquals(132_000, PlayerStyleMath.normalizeBitrateBps(132))
        assertEquals(256_000, PlayerStyleMath.normalizeBitrateBps(256))
        assertEquals(134_215, PlayerStyleMath.normalizeBitrateBps(134_215))
    }

    @Test
    fun `formatBitrateKbpsLabel formats both kbps and bps cleanly`() {
        assertNull(PlayerStyleMath.formatBitrateKbpsLabel(0))
        assertEquals("132 kbps", PlayerStyleMath.formatBitrateKbpsLabel(132))
        assertEquals("256 kbps", PlayerStyleMath.formatBitrateKbpsLabel(256_000))
    }

    @Test
    fun `formatRealTimeAudioDetail never emits 1 kbps for NewPipe kbps values`() {
        assertEquals(
            "132 kbps · 48 kHz",
            PlayerStyleMath.formatRealTimeAudioDetail(bitrateBps = 132, sampleRateHz = 48_000)
        )
        assertEquals(
            "134 kbps · 44.1 kHz",
            PlayerStyleMath.formatRealTimeAudioDetail(bitrateBps = 134_000, sampleRateHz = 44_100)
        )
        assertEquals(
            "48 kHz",
            PlayerStyleMath.formatRealTimeAudioDetail(bitrateBps = 0, sampleRateHz = 48_000)
        )
    }

    @Test
    fun `parseLrcLines skips metadata tags and deduplicates accidental tight timestamps`() {
        val raw = """
            [ti:Die With A Smile]
            [ar:Lady Gaga & Bruno Mars]
            [al:MAYHEM]
            [length:04:11]
            [00:10.00][01:02.50]Repeated line
            [01:02.60]Repeated line
            [03:38.00]If the world was ending
            [03:44.00]If the world was ending
        """.trimIndent()

        val parsed = LyricsRepository.parseLrcLines(raw)
        assertEquals(4, parsed.size)
        assertEquals(10_000L, parsed[0].timeMs)
        assertEquals("Repeated line", parsed[0].text)
        assertEquals(62_500L, parsed[1].timeMs)
        assertEquals(218_000L, parsed[2].timeMs)
        assertEquals(224_000L, parsed[3].timeMs)
    }
}
