package com.example.ecodot.playback

import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.util.Log

class AudioEffectManager(private val context: Context, private var audioSessionId: Int) {
    private var equalizer: Equalizer? = null
    private var virtualizer: Virtualizer? = null

    private var isEnabled = false
    private val bandLevels = mutableMapOf<Short, Short>()
    private var vStrength: Short = 0

    init {
        initEffects()
    }

    private fun initEffects() {
        if (audioSessionId == 0) return

        try {
            equalizer?.release()
            virtualizer?.release()

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

            equalizer?.enabled = isEnabled
            virtualizer?.enabled = isEnabled
            
            broadcastSessionId(isEnabled)

            for ((band, level) in bandLevels) {
                try {
                    equalizer?.setBandLevel(band, level)
                } catch (e: Exception) {
                    Log.e("AudioEffectManager", "Error setting band \$band to \$level", e)
                }
            }

            try {
                virtualizer?.setStrength(vStrength)
            } catch (e: Exception) {
                Log.e("AudioEffectManager", "Error setting virtualizer strength", e)
            }
        } catch (e: Exception) {
            Log.e("AudioEffectManager", "Failed to init audio effects for session $audioSessionId", e)
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
        vStrength = strength
        try {
            virtualizer?.setStrength(strength)
        } catch (e: Exception) {
             Log.e("AudioEffectManager", "Error applying virtualizer strength", e)
        }
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        try {
            equalizer?.enabled = enabled
            virtualizer?.enabled = enabled
            broadcastSessionId(enabled)
        } catch (e: Exception) {
             Log.e("AudioEffectManager", "Error applying enabled state", e)
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
        equalizer = null
        virtualizer = null
    }
}
