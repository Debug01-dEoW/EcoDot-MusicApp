package com.example.ecodot.playback

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import java.nio.ByteBuffer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Media3 PCM AudioProcessor that powers two SimpMusic AutoMix / Crossfade features with
 * zero per-buffer allocations:
 * 1. DJ-Style Transition: smooth real-time biquad low-pass / high-pass sweeps during crossfade.
 * 2. Live RMS envelope tracking: lets Auto crossfade detect quiet outros for natural phrasing.
 */
@UnstableApi
class CrossfadeFilterAudioProcessor : BaseAudioProcessor() {

    enum class FilterMode {
        NONE,
        LOW_PASS,
        HIGH_PASS
    }

    @Volatile
    private var filterMode: FilterMode = FilterMode.NONE

    @Volatile
    private var targetCutoffHz: Float = 18_000f

    @Volatile
    var smoothedRms: Float = 0.5f
        private set

    // Biquad history per channel (up to 8 channels)
    private val maxChannels = 8
    private val x1 = DoubleArray(maxChannels)
    private val x2 = DoubleArray(maxChannels)
    private val y1 = DoubleArray(maxChannels)
    private val y2 = DoubleArray(maxChannels)

    private var cachedMode: FilterMode = FilterMode.NONE
    private var cachedCutoffHz: Float = -1f
    private var cachedSampleRate: Int = 0
    private var filterBlend: Double = 0.0

    // Normalized Butterworth biquad coefficients (Q = 0.7071)
    private var b0 = 1.0
    private var b1 = 0.0
    private var b2 = 0.0
    private var a1 = 0.0
    private var a2 = 0.0

    fun setFilter(mode: FilterMode, cutoffHz: Float) {
        if (mode == FilterMode.NONE && filterMode != FilterMode.NONE) {
            // Reset filter state cleanly when disabling
            clearFilterHistory()
            filterBlend = 0.0
        }
        filterMode = mode
        targetCutoffHz = cutoffHz
    }

    fun resetFilter() {
        filterMode = FilterMode.NONE
        cachedMode = FilterMode.NONE
        cachedCutoffHz = -1f
        filterBlend = 0.0
        smoothedRms = 0.5f
        clearFilterHistory()
    }

    private fun clearFilterHistory() {
        x1.fill(0.0)
        x2.fill(0.0)
        y1.fill(0.0)
        y2.fill(0.0)
    }

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        if (inputAudioFormat.encoding == AudioProcessor.AudioFormat.NOT_SET.encoding) {
            return AudioProcessor.AudioFormat.NOT_SET
        }
        if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT) {
            throw AudioProcessor.UnhandledAudioFormatException(inputAudioFormat)
        }
        clearFilterHistory()
        cachedCutoffHz = -1f
        return inputAudioFormat
    }

    override fun isActive(): Boolean {
        return outputAudioFormat != AudioProcessor.AudioFormat.NOT_SET
    }

    private fun updateCoefficientsIfNeeded(mode: FilterMode, cutoffHz: Float, sampleRate: Int) {
        if (mode == cachedMode && cutoffHz == cachedCutoffHz && sampleRate == cachedSampleRate) {
            return
        }
        cachedMode = mode
        cachedCutoffHz = cutoffHz
        cachedSampleRate = sampleRate

        val safeRate = sampleRate.coerceAtLeast(8000).toDouble()
        val nyquistLimit = (safeRate * 0.45).toFloat()
        val clampedCutoff = cutoffHz.coerceIn(25f, nyquistLimit).toDouble()

        val omega = 2.0 * PI * clampedCutoff / safeRate
        val sinW = sin(omega)
        val cosW = cos(omega)
        val q = 0.7071067811865476 // 1 / sqrt(2) Butterworth
        val alpha = sinW / (2.0 * q)
        val a0 = 1.0 + alpha

        when (mode) {
            FilterMode.LOW_PASS -> {
                val c = (1.0 - cosW) / 2.0
                b0 = c / a0
                b1 = (1.0 - cosW) / a0
                b2 = c / a0
                a1 = (-2.0 * cosW) / a0
                a2 = (1.0 - alpha) / a0
            }
            FilterMode.HIGH_PASS -> {
                val c = (1.0 + cosW) / 2.0
                b0 = c / a0
                b1 = -(1.0 + cosW) / a0
                b2 = c / a0
                a1 = (-2.0 * cosW) / a0
                a2 = (1.0 - alpha) / a0
            }
            FilterMode.NONE -> {
                b0 = 1.0; b1 = 0.0; b2 = 0.0; a1 = 0.0; a2 = 0.0
            }
        }
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val remaining = inputBuffer.remaining()
        if (remaining == 0) return

        val channels = inputAudioFormat.channelCount.coerceIn(1, maxChannels)
        val bytesPerFrame = inputAudioFormat.bytesPerFrame.coerceAtLeast(channels * 2)
        val frames = remaining / bytesPerFrame
        if (frames <= 0) {
            inputBuffer.position(inputBuffer.limit())
            return
        }

        val outBuffer = replaceOutputBuffer(frames * bytesPerFrame)
        val activeMode = filterMode

        var energySumSq = 0.0
        var energySamples = 0

        if (activeMode == FilterMode.NONE) {
            val startPos = inputBuffer.position()
            // Fast decimated RMS sampling without disturbing PCM bytes
            var frameIdx = 0
            while (frameIdx < frames) {
                val offset = startPos + frameIdx * bytesPerFrame
                val sample = inputBuffer.getShort(offset).toDouble() / 32768.0
                energySumSq += sample * sample
                energySamples++
                frameIdx += 8
            }
            outBuffer.put(inputBuffer)
            outBuffer.flip()
        } else {
            updateCoefficientsIfNeeded(activeMode, targetCutoffHz, inputAudioFormat.sampleRate)
            for (f in 0 until frames) {
                // Soft click-free wet engagement over ~128 frames
                if (filterBlend < 1.0) {
                    filterBlend = (filterBlend + 0.008).coerceAtMost(1.0)
                }
                for (ch in 0 until channels) {
                    val sampleShort = inputBuffer.short
                    val dry = sampleShort.toDouble()
                    if (ch == 0 && (f and 7) == 0) {
                        val norm = dry / 32768.0
                        energySumSq += norm * norm
                        energySamples++
                    }
                    val wet = b0 * dry + b1 * x1[ch] + b2 * x2[ch] - a1 * y1[ch] - a2 * y2[ch]
                    x2[ch] = x1[ch]
                    x1[ch] = dry
                    y2[ch] = y1[ch]
                    y1[ch] = wet

                    val mixed = dry + filterBlend * (wet - dry)
                    val outShort = mixed.coerceIn(-32768.0, 32767.0).toInt().toShort()
                    outBuffer.putShort(outShort)
                }
            }
            outBuffer.flip()
        }

        if (energySamples > 0) {
            val rms = sqrt(energySumSq / energySamples).toFloat().coerceIn(0f, 1f)
            // Exponential moving average (~150ms responsiveness)
            smoothedRms = smoothedRms * 0.85f + rms * 0.15f
        }
    }

    override fun onFlush() {
        clearFilterHistory()
        filterBlend = 0.0
        smoothedRms = 0.5f
    }

    override fun onReset() {
        resetFilter()
    }
}
