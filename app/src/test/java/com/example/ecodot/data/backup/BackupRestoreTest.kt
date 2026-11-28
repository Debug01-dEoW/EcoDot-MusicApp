package com.example.ecodot.data.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupRestoreTest {

    @Test
    fun `backup schedule due calculation respects intervals across all frequencies`() {
        val dayMs = 24L * 60L * 60L * 1000L
        val now = 1_795_800_000_000L

        assertFalse(BackupSchedule.isBackupDue(BackupSchedule.OFF, 0L, now))
        assertFalse(BackupSchedule.isBackupDue(BackupSchedule.OFF, now - 10 * dayMs, now))

        assertTrue(BackupSchedule.isBackupDue(BackupSchedule.DAILY, 0L, now))
        assertFalse(BackupSchedule.isBackupDue(BackupSchedule.DAILY, now - (dayMs - 60_000L), now))
        assertTrue(BackupSchedule.isBackupDue(BackupSchedule.DAILY, now - dayMs, now))

        assertFalse(BackupSchedule.isBackupDue(BackupSchedule.EVERY_3_DAYS, now - 2 * dayMs, now))
        assertTrue(BackupSchedule.isBackupDue(BackupSchedule.EVERY_3_DAYS, now - 3 * dayMs, now))

        assertFalse(BackupSchedule.isBackupDue(BackupSchedule.WEEKLY, now - 6 * dayMs, now))
        assertTrue(BackupSchedule.isBackupDue(BackupSchedule.WEEKLY, now - 7 * dayMs, now))

        assertFalse(BackupSchedule.isBackupDue(BackupSchedule.MONTHLY, now - 29 * dayMs, now))
        assertTrue(BackupSchedule.isBackupDue(BackupSchedule.MONTHLY, now - 30 * dayMs, now))
    }

    @Test
    fun `backup archive JSON round-trip preserves playlists, liked songs, artists, EQ, and prefs`() {
        val original = EcoDotBackupArchive(
            schemaVersion = 1,
            appVersion = "1.27v",
            createdAt = 1_795_850_000_000L,
            tracks = listOf(
                BackupTrackDto(
                    id = "abc123",
                    title = "Die With A Smile",
                    artist = "Lady Gaga & Bruno Mars",
                    album = "MAYHEM",
                    duration = 251_000L,
                    path = "https://www.youtube.com/watch?v=abc123",
                    albumArtUri = "https://lh3.googleusercontent.com/art",
                    isYouTube = true,
                    artistId = "UCartist1",
                    albumId = "MPREbalbum1",
                    playCount = 18,
                    lastPlayedAt = 1_795_840_000_000L
                )
            ),
            playlists = listOf(
                BackupPlaylistDto(id = 1L, name = "Liked Songs", description = "Favorites"),
                BackupPlaylistDto(id = 4L, name = "Late Night Vibes", description = "Custom mix")
            ),
            playlistTracks = listOf(
                BackupPlaylistTrackDto(playlistId = 1L, trackId = "abc123", addedAt = 1_795_810_000_000L),
                BackupPlaylistTrackDto(playlistId = 4L, trackId = "abc123", addedAt = 1_795_812_000_000L)
            ),
            followedArtists = listOf(
                BackupFollowedArtistDto(
                    artistId = "UCartist1",
                    artistName = "Lady Gaga",
                    imageUrl = "https://example.com/gaga.jpg",
                    followedAt = 1_795_820_000_000L
                )
            ),
            history = listOf(BackupHistoryDto(trackId = "abc123", timestamp = 1_795_840_000_000L)),
            recentSearches = listOf(BackupRecentSearchDto(query = "Die With A Smile", type = "Song", itemId = "abc123", timestamp = 1_795_805_000_000L)),
            profile = BackupProfileDto(
                name = "Deep",
                favoriteGenres = "Pop, Synthwave",
                avatarUri = "file:///data/avatar.jpg",
                avatarBase64 = "ZmFrZUF2YXRhcg==",
                isPro = true
            ),
            preferences = BackupPreferencesDto(
                audioQuality = "HIGH",
                equalizerEnabled = true,
                virtualizerStrength = 450,
                bassBoostStrength = 620,
                selectedEqPreset = "My Bass Curve",
                customEqBandsRaw = "0:800,1:500,2:100,3:300,4:600",
                customEqPresetsRaw = "My Bass Curve|800,500,100,300,600",
                pinnedPlaylistIds = listOf("4"),
                autoBackupFrequency = "WEEKLY"
            )
        )

        val json = EcoDotBackupManager.encodeArchiveToJson(original)
        val restored = EcoDotBackupManager.decodeArchiveFromJson(json)

        assertNotNull(restored)
        assertEquals(original, restored)
        assertNull(EcoDotBackupManager.decodeArchiveFromJson("{ invalid json"))
    }

    @Test
    fun `missing app-private downloaded track converts cleanly back to streamable YouTube URL on restore`() {
        val appPrivateDeletedDownload = BackupTrackDto(
            id = "dQw4w9WgXcQ",
            title = "Downloaded Song Before Uninstall",
            artist = "Artist",
            album = "Downloads",
            duration = 210_000L,
            path = "/storage/emulated/0/Android/data/com.example.ecodot/files/Music/dQw4w9WgXcQ.m4a",
            isYouTube = false
        )

        val entityMissingFile = appPrivateDeletedDownload.toEntity(localFileExists = { false })
        assertTrue(entityMissingFile.isYouTube)
        assertEquals("https://www.youtube.com/watch?v=dQw4w9WgXcQ", entityMissingFile.path)

        val entityPresentFile = appPrivateDeletedDownload.toEntity(localFileExists = { true })
        assertFalse(entityPresentFile.isYouTube)
        assertEquals(appPrivateDeletedDownload.path, entityPresentFile.path)
    }
}
