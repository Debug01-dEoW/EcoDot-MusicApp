package com.example.ecodot.playback

import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DeviceEqSpec(
    val bandCount: Int = 5,
    val centerFreqsHz: List<Int> = listOf(60, 230, 910, 3600, 14000),
    val minLevelMb: Int = -1500,
    val maxLevelMb: Int = 1500
)

class AudioEffectManager(private val context: Context, private var audioSessionId: Int) {
    private var equalizer: Equalizer? = null
    private var virtualizer: Virtualizer? = null
    private var bassBoost: BassBoost? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null

    private var isEnabled = false
    private var isNormalizeEnabled = false
    private val bandLevels = mutableMapOf<Short, Short>()
    private var vStrength: Short = 0
    private var bbStrength: Short = 0

    companion object {
        private val _deviceEqSpec = MutableStateFlow(DeviceEqSpec())
        val deviceEqSpec: StateFlow<DeviceEqSpec> = _deviceEqSpec.asStateFlow()
    }

    init {
        initEffects()
    }

    private fun initEffects() {
        if (audioSessionId == 0) return

        try {
            equalizer?.release()
            virtualizer?.release()
            bassBoost?.release()
            loudnessEnhancer?.release()

            try {
                equalizer = Equalizer(1, audioSessionId)
            } catch (e: Exception) {
                Log.w("AudioEffectManager", "Failed to init Equalizer with priority 1, falling back to 0")
                equalizer = Equalizer(0, audioSessionId)
            }

            try {
                virtualizer = Virtualizer(1, audioSessionId)
            } catch (e: Exception) {
                Log.w("AudioEffectManager", "Failed to init Virtualizer with priority 1, falling back to 0")
                virtualizer = Virtualizer(0, audioSessionId)
            }

            try {
                bassBoost = BassBoost(1, audioSessionId)
            } catch (e: Exception) {
                Log.w("AudioEffectManager", "Failed to init BassBoost with priority 1, falling back to 0")
                bassBoost = runCatching { BassBoost(0, audioSessionId) }.getOrNull()
            }

            try {
                loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
                    setTargetGain(if (isNormalizeEnabled) 550 else 0)
                    enabled = isNormalizeEnabled
                }
            } catch (e: Exception) {
                Log.w("AudioEffectManager", "Failed to init LoudnessEnhancer: ${e.message}")
                loudnessEnhancer = null
            }

            equalizer?.enabled = isEnabled
            virtualizer?.enabled = isEnabled
            bassBoost?.enabled = isEnabled

            publishDeviceSpec()
            broadcastSessionId(isEnabled)

            val range = getBandLevelRange()
            val minMb = range.getOrNull(0)?.toInt() ?: -1500
            val maxMb = range.getOrNull(1)?.toInt() ?: 1500
            val count = equalizer?.numberOfBands?.toInt() ?: 5
            for (idx in 0 until count) {
                val band = idx.toShort()
                val level = (bandLevels[band]?.toInt() ?: 0).coerceIn(minMb, maxMb).toShort()
                bandLevels[band] = level
                try {
                    equalizer?.setBandLevel(band, level)
                } catch (e: Exception) {
                    Log.e("AudioEffectManager", "Error setting band $band to $level", e)
                }
            }

            try {
                virtualizer?.setStrength(vStrength.coerceIn(0, 1000).toShort())
            } catch (e: Exception) {
                Log.e("AudioEffectManager", "Error setting virtualizer strength", e)
            }

            try {
                if (bassBoost?.strengthSupported == true) {
                    bassBoost?.setStrength(bbStrength.coerceIn(0, 1000).toShort())
                }
            } catch (e: Exception) {
                Log.e("AudioEffectManager", "Error setting bass boost strength", e)
            }
        } catch (e: Exception) {
            Log.e("AudioEffectManager", "Failed to init audio effects for session $audioSessionId", e)
        }
    }

    private fun publishDeviceSpec() {
        val eq = equalizer ?: return
        try {
            val count = eq.numberOfBands.toInt().coerceAtLeast(1)
            val freqs = (0 until count).map { idx ->
                (eq.getCenterFreq(idx.toShort()) / 1000).coerceAtLeast(20)
            }
            val range = eq.bandLevelRange
            val minMb = range.getOrNull(0)?.toInt() ?: -1500
            val maxMb = range.getOrNull(1)?.toInt() ?: 1500
            _deviceEqSpec.value = DeviceEqSpec(
                bandCount = count,
                centerFreqsHz = freqs,
                minLevelMb = minMb,
                maxLevelMb = maxMb
            )
        } catch (e: Exception) {
            Log.w("AudioEffectManager", "Could not query device EQ spec", e)
        }
    }

    fun updateAudioSessionId(newSessionId: Int) {
        if (this.audioSessionId == newSessionId && equalizer != null) return
        this.audioSessionId = newSessionId
        Log.d("AudioEffectManager", "Audio session ID changed to $newSessionId, re-initializing effects")
        initEffects()
    }

    fun setBandLevel(band: Short, level: Short) {
        bandLevels[band] = level
        try {
            equalizer?.setBandLevel(band, level)
        } catch (e: Exception) {
            Log.e("AudioEffectManager", "Error applying band level", e)
        }
    }

    fun getBandLevel(band: Short): Short {
        return bandLevels[band] ?: equalizer?.getBandLevel(band) ?: 0
    }

    fun getNumberOfBands(): Short {
        return equalizer?.numberOfBands ?: 5
    }

    fun getCenterFreq(band: Short): Int {
        return equalizer?.getCenterFreq(band) ?: 0
    }

    fun getBandLevelRange(): ShortArray {
        return equalizer?.bandLevelRange ?: shortArrayOf(-1500, 1500)
    }

    fun setVirtualizerStrength(strength: Short) {
        val clamped = strength.toInt().coerceIn(0, 1000).toShort()
        vStrength = clamped
        try {
            virtualizer?.setStrength(clamped)
        } catch (e: Exception) {
            Log.e("AudioEffectManager", "Error applying virtualizer strength", e)
        }
    }

    fun setBassBoostStrength(strength: Short) {
        val clamped = strength.toInt().coerceIn(0, 1000).toShort()
        bbStrength = clamped
        try {
            if (bassBoost?.strengthSupported == true) {
                bassBoost?.setStrength(clamped)
            }
        } catch (e: Exception) {
            Log.e("AudioEffectManager", "Error applying bass boost strength", e)
        }
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        try {
            equalizer?.enabled = enabled
            virtualizer?.enabled = enabled
            bassBoost?.enabled = enabled
            broadcastSessionId(enabled)
        } catch (e: Exception) {
            Log.e("AudioEffectManager", "Error applying enabled state", e)
        }
    }

    fun setNormalizeVolume(enabled: Boolean) {
        isNormalizeEnabled = enabled
        try {
            if (loudnessEnhancer == null && audioSessionId != 0) {
                loudnessEnhancer = LoudnessEnhancer(audioSessionId)
            }
            loudnessEnhancer?.setTargetGain(if (enabled) 550 else 0)
            loudnessEnhancer?.enabled = enabled
        } catch (e: Exception) {
            Log.e("AudioEffectManager", "Error applying loudness normalization", e)
        }
    }

    private fun broadcastSessionId(enabled: Boolean) {
        if (audioSessionId == 0) return
        val action = if (enabled) AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION else AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION
        val intent = Intent(action).apply {
            putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId)
            putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
        }
        context.sendBroadcast(intent)
    }

    fun release() {
        broadcastSessionId(false)
        equalizer?.release()
        virtualizer?.release()
        bassBoost?.release()
        loudnessEnhancer?.release()
        equalizer = null
        virtualizer = null
        bassBoost = null
        loudnessEnhancer = null
    }
}
