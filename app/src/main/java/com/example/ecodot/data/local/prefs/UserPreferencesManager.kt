package com.example.ecodot.data.local.prefs

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

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
        get() = prefs.getBoolean("auto_canvas_enabled", false)
        set(value) = prefs.edit().putBoolean("auto_canvas_enabled", value).apply()

    var lockscreenLyricsEnabled: Boolean
        get() = prefs.getBoolean("lockscreen_lyrics_enabled", true)
        set(value) = prefs.edit().putBoolean("lockscreen_lyrics_enabled", value).apply()

    var sponsorBlockEnabled: Boolean
        get() = prefs.getBoolean("sponsor_block_enabled", true)
        set(value) = prefs.edit().putBoolean("sponsor_block_enabled", value).apply()

    var playerStyle: PlayerStyle
        get() {
            val name = prefs.getString("player_style", PlayerStyle.ECODOT.name) ?: PlayerStyle.ECODOT.name
            return try {
                PlayerStyle.valueOf(name)
            } catch (e: Exception) {
                PlayerStyle.ECODOT
            }
        }
        set(value) = prefs.edit().putString("player_style", value.name).apply()

    var selectedEqPreset: String
        get() = prefs.getString("selected_eq_preset", "Flat") ?: "Flat"
        set(value) = prefs.edit().putString("selected_eq_preset", value).apply()

    var libraryIsGrid: Boolean
        get() = prefs.getBoolean("library_is_grid", true)
        set(value) = prefs.edit().putBoolean("library_is_grid", value).apply()

    var librarySort: String
        get() = prefs.getString("library_sort", "RECENTLY_ADDED") ?: "RECENTLY_ADDED"
        set(value) = prefs.edit().putString("library_sort", value).apply()

    var pinnedPlaylistIds: Set<String>
        get() = prefs.getStringSet("pinned_playlist_ids", emptySet()) ?: emptySet()
        set(value) = prefs.edit().putStringSet("pinned_playlist_ids", value).apply()

    fun isPlaylistPinned(playlistId: Long): Boolean {
        return pinnedPlaylistIds.contains(playlistId.toString())
    }

    fun togglePinPlaylist(playlistId: Long): Boolean {
        val current = pinnedPlaylistIds.toMutableSet()
        val key = playlistId.toString()
        val willBePinned = if (current.contains(key)) {
            current.remove(key)
            false
        } else {
            current.add(key)
            true
        }
        pinnedPlaylistIds = current
        return willBePinned
    }

    var cachedTrackIds: Set<String>
        get() = prefs.getStringSet("cached_track_ids", emptySet())?.toSet() ?: emptySet()
        set(value) {
            prefs.edit().putStringSet("cached_track_ids", HashSet(value)).apply()
        }

    val cachedTrackIdsFlow: Flow<Set<String>> = callbackFlow {
        trySend(cachedTrackIds)
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "cached_track_ids") {
                trySend(cachedTrackIds)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    fun addCachedTrackId(trackId: String) {
        val current = HashSet(cachedTrackIds)
        if (current.add(trackId)) {
            cachedTrackIds = current
        }
    }

    fun removeCachedTrackId(trackId: String) {
        val current = HashSet(cachedTrackIds)
        if (current.remove(trackId)) {
            cachedTrackIds = current
        }
    }

    fun clearCachedTrackIds() {
        cachedTrackIds = emptySet()
    }

    var cacheLimitMb: Long
        get() = prefs.getLong("cache_limit_mb", 0L) // 0 = dynamic / unlimited (no artificial limit)
        set(value) = prefs.edit().putLong("cache_limit_mb", value).apply()
}

enum class PlayerStyle {
    ECODOT,
    APPLE_MUSIC,
    SPOTIFY,
    VINYL
}

