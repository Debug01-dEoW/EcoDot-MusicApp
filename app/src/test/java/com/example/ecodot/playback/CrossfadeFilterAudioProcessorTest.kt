package com.example.ecodot.playback

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.sqrt

class CrossfadeFilterAudioProcessorTest {

    private fun generateSinePcm16Stereo(freqHz: Double, sampleRate: Int, frames: Int, amplitude: Double = 16000.0): ByteBuffer {
        val buf = ByteBuffer.allocateDirect(frames * 4).order(ByteOrder.LITTLE_ENDIAN)
        for (i in 0 until frames) {
            val sample = (sin(2.0 * PI * freqHz * i / sampleRate) * amplitude).toInt().coerceIn(-32768, 32767).toShort()
            buf.putShort(sample)
            buf.putShort(sample)
        }
        buf.flip()
        return buf
    }

    private fun measureRmsTail(buffer: ByteBuffer, skipInitialFrames: Int = 200): Double {
        val dup = buffer.duplicate().order(ByteOrder.LITTLE_ENDIAN)
        val totalFrames = dup.remaining() / 4
        var sumSq = 0.0
        var count = 0
        for (f in 0 until totalFrames) {
            val left = dup.short.toDouble()
            dup.short // right
            if (f >= skipInitialFrames) {
                sumSq += left * left
                count++
            }
        }
        return if (count > 0) sqrt(sumSq / count) else 0.0
    }

    @Test
    fun passthroughPreservesSignalAndTracksRms() {
        val processor = CrossfadeFilterAudioProcessor()
        val format = AudioProcessor.AudioFormat(44100, 2, C.ENCODING_PCM_16BIT)
        processor.configure(format)
        processor.flush()

        assertTrue(processor.isActive())

        val input = generateSinePcm16Stereo(freqHz = 440.0, sampleRate = 44100, frames = 2048, amplitude = 12000.0)
        val original = input.duplicate().order(ByteOrder.LITTLE_ENDIAN)
        processor.queueInput(input)
        val output = processor.getOutput().order(ByteOrder.LITTLE_ENDIAN)

        assertEquals(original.remaining(), output.remaining())
        assertEquals(measureRmsTail(original, 0), measureRmsTail(output, 0), 1e-3)
        assertTrue("Expected smoothedRms to rise on signal", processor.smoothedRms > 0.05f)
    }

    @Test
    fun lowPassFilterAttenuatesHighFrequenciesWhileKeepingBass() {
        val processor = CrossfadeFilterAudioProcessor()
        processor.configure(AudioProcessor.AudioFormat(44100, 2, C.ENCODING_PCM_16BIT))
        processor.flush()
        processor.setFilter(CrossfadeFilterAudioProcessor.FilterMode.LOW_PASS, 400f)

        val bassIn = generateSinePcm16Stereo(freqHz = 100.0, sampleRate = 44100, frames = 2048)
        val bassInRms = measureRmsTail(bassIn)
        processor.queueInput(bassIn)
        val bassOutRms = measureRmsTail(processor.getOutput())

        processor.flush()
        processor.setFilter(CrossfadeFilterAudioProcessor.FilterMode.LOW_PASS, 400f)
        val trebleIn = generateSinePcm16Stereo(freqHz = 6000.0, sampleRate = 44100, frames = 2048)
        val trebleInRms = measureRmsTail(trebleIn)
        processor.queueInput(trebleIn)
        val trebleOutRms = measureRmsTail(processor.getOutput())

        assertTrue("Low-pass should preserve >80% of 100Hz bass", bassOutRms > bassInRms * 0.80)
        assertTrue("Low-pass at 400Hz should attenuate 6kHz below 5%", trebleOutRms < trebleInRms * 0.05)
    }

    @Test
    fun highPassFilterAttenuatesBassWhileKeepingTreble() {
        val processor = CrossfadeFilterAudioProcessor()
        processor.configure(AudioProcessor.AudioFormat(44100, 2, C.ENCODING_PCM_16BIT))
        processor.flush()
        processor.setFilter(CrossfadeFilterAudioProcessor.FilterMode.HIGH_PASS, 900f)

        val bassIn = generateSinePcm16Stereo(freqHz = 90.0, sampleRate = 44100, frames = 2048)
        val bassInRms = measureRmsTail(bassIn)
        processor.queueInput(bassIn)
        val bassOutRms = measureRmsTail(processor.getOutput())

        processor.flush()
        processor.setFilter(CrossfadeFilterAudioProcessor.FilterMode.HIGH_PASS, 900f)
        val trebleIn = generateSinePcm16Stereo(freqHz = 6000.0, sampleRate = 44100, frames = 2048)
        val trebleInRms = measureRmsTail(trebleIn)
        processor.queueInput(trebleIn)
        val trebleOutRms = measureRmsTail(processor.getOutput())

        assertTrue("High-pass at 900Hz should attenuate 90Hz bass below 5%", bassOutRms < bassInRms * 0.05)
        assertTrue("High-pass should preserve >85% of 6kHz treble", trebleOutRms > trebleInRms * 0.85)
    }
}
