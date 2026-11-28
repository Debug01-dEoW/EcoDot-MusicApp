package com.example.ecodot.data.local.prefs

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

enum class AudioQuality {
    LOW,      // ~48 kbps
    NORMAL,   // ~128 kbps
    HIGH,     // High Opus (up to 256 kbps)
    HIGH_AAC  // High AAC / M4A (up to 256 kbps)
}

enum class VideoQuality {
    LOW,
    NORMAL,
    HIGH
}

enum class ThemeMode(val label: String) {
    OLED_BLACK("OLED Black"),
    DARK_SLATE("Dark Slate")
}

enum class ThemeColorSource(val label: String) {
    ALBUM_ART("Dynamic (Album Art)"),
    SIMP_DEFAULT("SimpMusic Default"),
    WALLPAPER("Wallpaper (Android 12+)"),
    CUSTOM("Custom Color")
}

enum class SponsorBlockCategory(val key: String, val displayName: String) {
    SPONSOR("sponsor", "Sponsor"),
    SELF_PROMO("selfpromo", "Unpaid / Self Promotion"),
    INTERACTION("interaction", "Interaction Reminder"),
    INTRO("intro", "Intermission / Intro Animation"),
    OUTRO("outro", "Endcards / Credits"),
    PREVIEW("preview", "Preview / Recap"),
    MUSIC_OFF_TOPIC("music_offtopic", "Music: Non-Music Section"),
    FILLER("filler", "Filler Tangent / Jokes"),
    POI_HIGHLIGHT("poi_highlight", "Highlight");

    companion object {
        val defaultKeys: Set<String> = setOf(
            SPONSOR.key,
            INTRO.key,
            OUTRO.key,
            MUSIC_OFF_TOPIC.key,
            PREVIEW.key
        )
    }
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
            val name = prefs.getString("video_quality", VideoQuality.HIGH.name) ?: VideoQuality.HIGH.name
            return try {
                VideoQuality.valueOf(name)
            } catch (e: Exception) {
                VideoQuality.HIGH
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

    // Duration in seconds; 0 = Auto (SimpMusic AutoMix)
    var crossfadeDuration: Int
        get() = prefs.getInt("crossfade_duration", 3)
        set(value) = prefs.edit().putInt("crossfade_duration", value).apply()

    var crossfadeDjMode: Boolean
        get() = prefs.getBoolean("crossfade_dj_mode", false)
        set(value) = prefs.edit().putBoolean("crossfade_dj_mode", value).apply()

    var crossfadeSkipAlbum: Boolean
        get() = prefs.getBoolean("crossfade_skip_album", true)
        set(value) = prefs.edit().putBoolean("crossfade_skip_album", value).apply()

    var normalizeVolume: Boolean
        get() = prefs.getBoolean("normalize_volume", false)
        set(value) = prefs.edit().putBoolean("normalize_volume", value).apply()

    var skipSilent: Boolean
        get() = prefs.getBoolean("skip_silent", false)
        set(value) = prefs.edit().putBoolean("skip_silent", value).apply()

    var equalizerEnabled: Boolean
        get() = prefs.getBoolean("equalizer_enabled", false)
        set(value) = prefs.edit().putBoolean("equalizer_enabled", value).apply()

    var virtualizerStrength: Int
        get() = prefs.getInt("virtualizer_strength", 0)
        set(value) = prefs.edit().putInt("virtualizer_strength", value).apply()

    var bassBoostStrength: Int
        get() = prefs.getInt("bass_boost_strength", 0).coerceIn(0, 1000)
        set(value) = prefs.edit().putInt("bass_boost_strength", value.coerceIn(0, 1000)).apply()

    var autoCanvasEnabled: Boolean
        get() = prefs.getBoolean("auto_canvas_enabled", false)
        set(value) = prefs.edit().putBoolean("auto_canvas_enabled", value).apply()

    var lockscreenLyricsEnabled: Boolean
        get() = prefs.getBoolean("lockscreen_lyrics_enabled", true)
        set(value) = prefs.edit().putBoolean("lockscreen_lyrics_enabled", value).apply()

    var sponsorBlockEnabled: Boolean
        get() = prefs.getBoolean("sponsor_block_enabled", true)
        set(value) = prefs.edit().putBoolean("sponsor_block_enabled", value).apply()

    var sponsorBlockCategories: Set<String>
        get() {
            val saved = prefs.getStringSet("sponsor_block_categories", null)
            return if (saved == null) {
                SponsorBlockCategory.defaultKeys
            } else {
                saved.map { if (it == "music_off_topic") "music_offtopic" else it }.toSet()
            }
        }
        set(value) = prefs.edit().putStringSet("sponsor_block_categories", HashSet(value)).apply()

    var themeMode: ThemeMode
        get() {
            val name = prefs.getString("theme_mode", ThemeMode.OLED_BLACK.name) ?: ThemeMode.OLED_BLACK.name
            return try {
                ThemeMode.valueOf(name)
            } catch (e: Exception) {
                ThemeMode.OLED_BLACK
            }
        }
        set(value) = prefs.edit().putString("theme_mode", value.name).apply()

    var themeColorSource: ThemeColorSource
        get() {
            val name = prefs.getString("theme_color_source", ThemeColorSource.ALBUM_ART.name) ?: ThemeColorSource.ALBUM_ART.name
            return try {
                ThemeColorSource.valueOf(name)
            } catch (e: Exception) {
                ThemeColorSource.ALBUM_ART
            }
        }
        set(value) = prefs.edit().putString("theme_color_source", value.name).apply()

    var customThemeColorHex: String
        get() = prefs.getString("custom_theme_color_hex", "FF1ED760") ?: "FF1ED760"
        set(value) = prefs.edit().putString("custom_theme_color_hex", value).apply()

    var liquidGlassEnabled: Boolean
        get() = prefs.getBoolean("liquid_glass_enabled", true)
        set(value) = prefs.edit().putBoolean("liquid_glass_enabled", value).apply()

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

    var customEqBandsRaw: String
        get() = prefs.getString("custom_eq_bands", "") ?: ""
        set(value) = prefs.edit().putString("custom_eq_bands", value).apply()

    var customEqPresetsRaw: String
        get() = prefs.getString("custom_eq_presets", "") ?: ""
        set(value) = prefs.edit().putString("custom_eq_presets", value).apply()

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

    var autoCheckUpdates: Boolean
        get() = prefs.getBoolean("auto_check_updates", true)
        set(value) = prefs.edit().putBoolean("auto_check_updates", value).apply()

    var lastUpdateCheckTime: Long
        get() = prefs.getLong("last_update_check_time", 0L)
        set(value) = prefs.edit().putLong("last_update_check_time", value).apply()

    companion object {
        const val DEFAULT_GITHUB_OWNER = "Debug01-dEoW"
        const val DEFAULT_GITHUB_REPO = "EcoDot-MusicApp"
        const val DEFAULT_GITHUB_PROFILE_URL = "https://github.com/Debug01-dEoW"
        const val DEFAULT_GITHUB_REPO_URL = "https://github.com/Debug01-dEoW/EcoDot-MusicApp"
        const val DEFAULT_GITHUB_RELEASES_URL = "https://github.com/Debug01-dEoW/EcoDot-MusicApp/releases"
        const val DEFAULT_UPDATE_JSON_URL = "https://raw.githubusercontent.com/Debug01-dEoW/EcoDot-MusicApp/main/version.json"
        const val APP_DESCRIPTION = "A vibrant 120Hz Android music & video player featuring crystal-clear Liquid Glass optics, Spotify-inspired playlists, Apple Music / Vinyl / Spotify Now Playing styles, full scrollable track tools, interactive circular profile photo cropper, seamless 1080p video playback, AutoMix DJ crossfade, and YouTube Music streaming — crafted by Debug01-dEoW aka Deep."
    }

    var updateJsonUrl: String
        get() {
            val saved = prefs.getString("update_json_url", DEFAULT_UPDATE_JSON_URL)
            return if (saved.isNullOrBlank() || saved.contains("deep082008/EcoDot/")) {
                DEFAULT_UPDATE_JSON_URL
            } else {
                saved
            }
        }
        set(value) = prefs.edit().putString("update_json_url", value).apply()

    var githubOwner: String
        get() {
            val saved = prefs.getString("github_owner", DEFAULT_GITHUB_OWNER)
            return if (saved.isNullOrBlank() || saved == "deep082008") DEFAULT_GITHUB_OWNER else saved
        }
        set(value) = prefs.edit().putString("github_owner", value).apply()

    var githubRepo: String
        get() {
            val saved = prefs.getString("github_repo", DEFAULT_GITHUB_REPO)
            return if (saved.isNullOrBlank() || saved == "EcoDot") DEFAULT_GITHUB_REPO else saved
        }
        set(value) = prefs.edit().putString("github_repo", value).apply()

    var selectedHomeCountry: String
        get() = prefs.getString("selected_home_country", "Global") ?: "Global"
        set(value) = prefs.edit().putString("selected_home_country", value).apply()

    var autoBackupFrequency: String
        get() = prefs.getString("auto_backup_frequency", "OFF") ?: "OFF"
        set(value) = prefs.edit().putString("auto_backup_frequency", value).apply()

    var autoBackupTreeUri: String?
        get() = prefs.getString("auto_backup_tree_uri", null)
        set(value) {
            if (value.isNullOrBlank()) prefs.edit().remove("auto_backup_tree_uri").apply()
            else prefs.edit().putString("auto_backup_tree_uri", value).apply()
        }

    var lastBackupTimestamp: Long
        get() = prefs.getLong("last_backup_timestamp", 0L)
        set(value) = prefs.edit().putLong("last_backup_timestamp", value).apply()

    var lastBackupSummary: String
        get() = prefs.getString("last_backup_summary", "") ?: ""
        set(value) = prefs.edit().putString("last_backup_summary", value).apply()
}

enum class PlayerStyle {
    ECODOT,
    APPLE_MUSIC,
    SPOTIFY,
    VINYL
}
