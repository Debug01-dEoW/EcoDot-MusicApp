package com.example.ecodot.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EqualizerUtilsTest {

    @Test
    fun `encode and decode EQ bands preserves band indices and clamps missing to zero`() {
        val original = mapOf(2 to -300, 0 to 800, 4 to 600, 1 to 250)
        val encoded = encodeEqBands(original)
        assertEquals("0:800,1:250,2:-300,4:600", encoded)

        val decoded5 = decodeEqBands(encoded, bandCount = 5)
        assertEquals(mapOf(0 to 800, 1 to 250, 2 to -300, 3 to 0, 4 to 600), decoded5)

        val decoded8 = decodeEqBands(encoded, bandCount = 8)
        assertEquals(8, decoded8.size)
        assertEquals(0, decoded8[7])
    }

    @Test
    fun `decodeEqBands handles blank and malformed entries gracefully`() {
        assertEquals(mapOf(0 to 0, 1 to 0, 2 to 0, 3 to 0, 4 to 0), decodeEqBands("", 5))
        assertEquals(mapOf(0 to 0, 1 to 0, 2 to 0, 3 to 0, 4 to 0), decodeEqBands(null, 5))

        val partial = decodeEqBands("0:400,bad,-1:100,2:abc,3:-200", 5)
        assertEquals(mapOf(0 to 400, 1 to 0, 2 to 0, 3 to -200, 4 to 0), partial)
    }

    @Test
    fun `encode and decode custom EQ presets preserves names and bands safely`() {
        val presets = listOf(
            EqPreset("Late | Night\nBass", "Custom", listOf(700, 400, 0, 200, 500), isCustom = true),
            EqPreset("Studio Vocal", "Custom", listOf(-200, 100, 650, 450, 150), isCustom = true)
        )
        val encoded = encodeCustomEqPresets(presets)
        val decoded = decodeCustomEqPresets(encoded)

        assertEquals(2, decoded.size)
        assertEquals("Late / Night Bass", decoded[0].name)
        assertEquals(listOf(700, 400, 0, 200, 500), decoded[0].bands)
        assertTrue(decoded[0].isCustom)
        assertEquals("Studio Vocal", decoded[1].name)
        assertEquals(listOf(-200, 100, 650, 450, 150), decoded[1].bands)
    }

    @Test
    fun `formatEqFrequency renders Hz and kHz labels accurately`() {
        assertEquals("60 Hz", formatEqFrequency(60))
        assertEquals("910 Hz", formatEqFrequency(910))
        assertEquals("1 kHz", formatEqFrequency(1000))
        assertEquals("3.6 kHz", formatEqFrequency(3600))
        assertEquals("14 kHz", formatEqFrequency(14000))
    }
}
