package com.example.ecodot.data.local.prefs

import android.content.Context
import android.content.SharedPreferences

enum class AudioQuality {
    LOW,    // ~48 kbps
    NORMAL, // ~128 kbps
    HIGH    // Max available
}

enum class VideoQuality {
    LOW,
    NORMAL,
    HIGH
}

class UserPreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("ecodot_prefs", Context.MODE_PRIVATE)

    var audioQuality: AudioQuality
        get() {
            val name = prefs.getString("audio_quality", AudioQuality.HIGH.name) ?: AudioQuality.HIGH.name
            return try {
                AudioQuality.valueOf(name)
            } catch (e: Exception) {
                AudioQuality.HIGH
            }
        }
        set(value) {
            prefs.edit().putString("audio_quality", value.name).apply()
        }

    var videoQuality: VideoQuality
        get() {
            val name = prefs.getString("video_quality", VideoQuality.NORMAL.name) ?: VideoQuality.NORMAL.name
            return try {
                VideoQuality.valueOf(name)
            } catch (e: Exception) {
                VideoQuality.NORMAL
            }
        }
        set(value) {
            prefs.edit().putString("video_quality", value.name).apply()
        }

    var offlineMode: Boolean
        get() = prefs.getBoolean("offline_mode", false)
        set(value) = prefs.edit().putBoolean("offline_mode", value).apply()

    var dataSaver: Boolean
        get() = prefs.getBoolean("data_saver", false)
        set(value) = prefs.edit().putBoolean("data_saver", value).apply()

    var searchHistory: List<String>
        get() = prefs.getString("search_history", "")?.split("|||")?.filter { it.isNotBlank() } ?: emptyList()
        set(value) = prefs.edit().putString("search_history", value.joinToString("|||")).apply()
        
    var killServiceOnExit: Boolean
        get() = prefs.getBoolean("kill_service_on_exit", false)
        set(value) = prefs.edit().putBoolean("kill_service_on_exit", value).apply()
        
    var crossfadeEnabled: Boolean
        get() = prefs.getBoolean("crossfade_enabled", false)
        set(value) = prefs.edit().putBoolean("crossfade_enabled", value).apply()
        
    var crossfadeDuration: Int
        get() = prefs.getInt("crossfade_duration", 3)
        set(value) = prefs.edit().putInt("crossfade_duration", value).apply()

    var autoCanvasEnabled: Boolean
        get() = prefs.getBoolean("auto_canvas_enabled", true)
        set(value) = prefs.edit().putBoolean("auto_canvas_enabled", value).apply()

    var lockscreenLyricsEnabled: Boolean
        get() = prefs.getBoolean("lockscreen_lyrics_enabled", true)
        set(value) = prefs.edit().putBoolean("lockscreen_lyrics_enabled", value).apply()
}
