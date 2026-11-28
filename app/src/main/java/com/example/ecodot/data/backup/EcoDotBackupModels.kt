package com.example.ecodot.data.backup

import com.example.ecodot.data.local.entities.FollowedArtist
import com.example.ecodot.data.local.entities.PlaybackHistory
import com.example.ecodot.data.local.entities.Playlist
import com.example.ecodot.data.local.entities.PlaylistTrack
import com.example.ecodot.data.local.entities.RecentSearchItem
import com.example.ecodot.data.local.entities.Track
import com.example.ecodot.data.local.entities.UserProfile
import com.squareup.moshi.JsonClass

enum class BackupSchedule(val key: String, val label: String, val intervalMs: Long) {
    OFF("OFF", "Off (Manual only)", 0L),
    DAILY("DAILY", "Every day", 24L * 60L * 60L * 1000L),
    EVERY_3_DAYS("EVERY_3_DAYS", "Every 3 days", 3L * 24L * 60L * 60L * 1000L),
    WEEKLY("WEEKLY", "Once a week", 7L * 24L * 60L * 60L * 1000L),
    MONTHLY("MONTHLY", "Once a month", 30L * 24L * 60L * 60L * 1000L);

    companion object {
        fun fromKey(key: String?): BackupSchedule =
            entries.firstOrNull { it.key == key } ?: OFF

        fun isBackupDue(schedule: BackupSchedule, lastBackupMs: Long, nowMs: Long): Boolean {
            if (schedule == OFF) return false
            if (lastBackupMs <= 0L) return true
            return (nowMs - lastBackupMs) >= schedule.intervalMs
        }
    }
}

enum class RestoreMode {
    REPLACE,
    MERGE
}

@JsonClass(generateAdapter = true)
data class BackupTrackDto(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val path: String,
    val albumArtUri: String? = null,
    val isYouTube: Boolean = false,
    val artistId: String? = null,
    val albumId: String? = null,
    val genre: String? = null,
    val playCount: Int = 0,
    val lastPlayedAt: Long? = null,
    val isExplicit: Boolean = false,
    val canvasUrl: String? = null
) {
    fun toEntity(localFileExists: (String) -> Boolean): Track {
        val restoredIsYt = if (!isYouTube && path.contains("/Android/data/", ignoreCase = true) && !localFileExists(path)) {
            true
        } else {
            isYouTube
        }
        val restoredPath = if (restoredIsYt && !isYouTube) {
            "https://www.youtube.com/watch?v=$id"
        } else {
            path
        }
        return Track(
            id = id,
            title = title,
            artist = artist,
            album = album,
            duration = duration,
            path = restoredPath,
            albumArtUri = albumArtUri,
            isYouTube = restoredIsYt,
            artistId = artistId,
            albumId = albumId,
            genre = genre,
            playCount = playCount,
            lastPlayedAt = lastPlayedAt,
            isExplicit = isExplicit,
            canvasUrl = canvasUrl
        )
    }

    companion object {
        fun fromEntity(t: Track) = BackupTrackDto(
            id = t.id,
            title = t.title,
            artist = t.artist,
            album = t.album,
            duration = t.duration,
            path = t.path,
            albumArtUri = t.albumArtUri,
            isYouTube = t.isYouTube,
            artistId = t.artistId,
            albumId = t.albumId,
            genre = t.genre,
            playCount = t.playCount,
            lastPlayedAt = t.lastPlayedAt,
            isExplicit = t.isExplicit,
            canvasUrl = t.canvasUrl
        )
    }
}

@JsonClass(generateAdapter = true)
data class BackupPlaylistDto(
    val id: Long,
    val name: String,
    val description: String = "",
    val coverArtUri: String? = null,
    val folder: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toEntity() = Playlist(
        id = id,
        name = name,
        description = description,
        coverArtUri = coverArtUri,
        folder = folder,
        createdAt = createdAt
    )

    companion object {
        fun fromEntity(p: Playlist) = BackupPlaylistDto(
            id = p.id,
            name = p.name,
            description = p.description,
            coverArtUri = p.coverArtUri,
            folder = p.folder,
            createdAt = p.createdAt
        )
    }
}

@JsonClass(generateAdapter = true)
data class BackupPlaylistTrackDto(
    val playlistId: Long,
    val trackId: String,
    val addedAt: Long
) {
    fun toEntity(mappedPlaylistId: Long = playlistId) = PlaylistTrack(
        playlistId = mappedPlaylistId,
        trackId = trackId,
        addedAt = addedAt
    )

    companion object {
        fun fromEntity(pt: PlaylistTrack) = BackupPlaylistTrackDto(
            playlistId = pt.playlistId,
            trackId = pt.trackId,
            addedAt = pt.addedAt
        )
    }
}

@JsonClass(generateAdapter = true)
data class BackupFollowedArtistDto(
    val artistId: String,
    val artistName: String,
    val imageUrl: String? = null,
    val followedAt: Long
) {
    fun toEntity() = FollowedArtist(
        artistId = artistId,
        artistName = artistName,
        imageUrl = imageUrl,
        followedAt = followedAt
    )

    companion object {
        fun fromEntity(f: FollowedArtist) = BackupFollowedArtistDto(
            artistId = f.artistId,
            artistName = f.artistName,
            imageUrl = f.imageUrl,
            followedAt = f.followedAt
        )
    }
}

@JsonClass(generateAdapter = true)
data class BackupHistoryDto(
    val trackId: String,
    val timestamp: Long
) {
    fun toEntity() = PlaybackHistory(
        id = 0,
        trackId = trackId,
        timestamp = timestamp
    )

    companion object {
        fun fromEntity(h: PlaybackHistory) = BackupHistoryDto(
            trackId = h.trackId,
            timestamp = h.timestamp
        )
    }
}

@JsonClass(generateAdapter = true)
data class BackupRecentSearchDto(
    val query: String,
    val type: String,
    val itemId: String? = null,
    val imageUrl: String? = null,
    val subtitle: String? = null,
    val timestamp: Long
) {
    fun toEntity() = RecentSearchItem(
        id = 0,
        query = query,
        type = type,
        itemId = itemId,
        imageUrl = imageUrl,
        subtitle = subtitle,
        timestamp = timestamp
    )

    companion object {
        fun fromEntity(r: RecentSearchItem) = BackupRecentSearchDto(
            query = r.query,
            type = r.type,
            itemId = r.itemId,
            imageUrl = r.imageUrl,
            subtitle = r.subtitle,
            timestamp = r.timestamp
        )
    }
}

@JsonClass(generateAdapter = true)
data class BackupProfileDto(
    val name: String,
    val favoriteGenres: String = "",
    val favoriteArtists: String = "",
    val avatarUri: String? = null,
    val avatarBase64: String? = null,
    val isPro: Boolean = true
)

@JsonClass(generateAdapter = true)
data class BackupPreferencesDto(
    val audioQuality: String = "HIGH",
    val videoQuality: String = "AUTO",
    val offlineMode: Boolean = false,
    val dataSaver: Boolean = false,
    val searchHistory: List<String> = emptyList(),
    val killServiceOnExit: Boolean = false,
    val crossfadeEnabled: Boolean = false,
    val crossfadeDuration: Int = 5,
    val crossfadeDjMode: Boolean = false,
    val crossfadeSkipAlbum: Boolean = false,
    val normalizeVolume: Boolean = false,
    val skipSilent: Boolean = false,
    val equalizerEnabled: Boolean = false,
    val virtualizerStrength: Int = 0,
    val bassBoostStrength: Int = 0,
    val selectedEqPreset: String = "Flat",
    val customEqBandsRaw: String = "",
    val customEqPresetsRaw: String = "",
    val autoCanvasEnabled: Boolean = true,
    val lockscreenLyricsEnabled: Boolean = true,
    val sponsorBlockEnabled: Boolean = false,
    val sponsorBlockCategories: List<String> = emptyList(),
    val themeMode: String = "DARK",
    val themeColorSource: String = "ALBUM_ART",
    val customThemeColorHex: String = "FF1ED760",
    val liquidGlassEnabled: Boolean = true,
    val playerStyle: String = "ECODOT",
    val libraryIsGrid: Boolean = true,
    val librarySort: String = "RECENTLY_ADDED",
    val pinnedPlaylistIds: List<String> = emptyList(),
    val cacheLimitMb: Long = 500L,
    val autoCheckUpdates: Boolean = true,
    val selectedHomeCountry: String = "Global",
    val autoBackupFrequency: String = "OFF"
)

@JsonClass(generateAdapter = true)
data class EcoDotBackupArchive(
    val schemaVersion: Int = 1,
    val appVersion: String = "1.28v",
    val createdAt: Long = System.currentTimeMillis(),
    val tracks: List<BackupTrackDto> = emptyList(),
    val playlists: List<BackupPlaylistDto> = emptyList(),
    val playlistTracks: List<BackupPlaylistTrackDto> = emptyList(),
    val followedArtists: List<BackupFollowedArtistDto> = emptyList(),
    val history: List<BackupHistoryDto> = emptyList(),
    val recentSearches: List<BackupRecentSearchDto> = emptyList(),
    val profile: BackupProfileDto? = null,
    val preferences: BackupPreferencesDto = BackupPreferencesDto()
)

data class BackupOperationResult(
    val success: Boolean,
    val message: String,
    val trackCount: Int = 0,
    val playlistCount: Int = 0,
    val artistCount: Int = 0,
    val destinationLabel: String = ""
)
