package com.example.ecodot.data.local.dao

import androidx.room.*
import com.example.ecodot.data.local.entities.Track
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks")
    fun getAllTracks(): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE id = :trackId")
    suspend fun getTrackById(trackId: String): Track?

    @Query("SELECT * FROM tracks WHERE lastPlayedAt IS NOT NULL ORDER BY lastPlayedAt DESC LIMIT 20")
    fun getRecentlyPlayed(): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE playCount > 0 ORDER BY playCount DESC LIMIT 20")
    fun getMostPlayed(): Flow<List<Track>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTracksIgnore(tracks: List<Track>): List<Long>

    @Query("UPDATE tracks SET title = :title, artist = :artist, album = :album, albumArtUri = :albumArtUri, isExplicit = :isExplicit, artistId = :artistId, albumId = :albumId, duration = :duration, path = :path, isYouTube = :isYouTube WHERE id = :id")
    suspend fun updateTrackMetadata(
        id: String,
        title: String,
        artist: String,
        album: String,
        albumArtUri: String?,
        isExplicit: Boolean,
        artistId: String?,
        albumId: String?,
        duration: Long,
        path: String,
        isYouTube: Boolean
    )

    @Transaction
    suspend fun insertTracks(tracks: List<Track>) {
        tracks.forEach { track ->
            val existing = getTrackById(track.id)
            if (existing != null) {
                val genericAlbums = setOf("Cached", "Cache", "Up Next", "Unknown", "")
                val incomingIsGeneric = track.album in genericAlbums
                val existingIsYtVideo = existing.path.startsWith("ytvideo://", ignoreCase = true)
                val incomingIsYtVideo = track.path.startsWith("ytvideo://", ignoreCase = true)

                val path = when {
                    !existing.isYouTube -> existing.path
                    // Never allow generic background inserts ("Cached", "Up Next", "Unknown") to wipe out a persisted video path
                    existingIsYtVideo && !incomingIsYtVideo && incomingIsGeneric -> existing.path
                    else -> track.path
                }
                val album = when {
                    existingIsYtVideo && path.startsWith("ytvideo://", ignoreCase = true) && incomingIsGeneric ->
                        existing.album.takeIf { it !in genericAlbums } ?: "YouTube Video"
                    existing.album !in genericAlbums && incomingIsGeneric -> existing.album
                    else -> track.album
                }
                val isYouTube = if (!existing.isYouTube) false else track.isYouTube
                updateTrackMetadata(
                    id = track.id,
                    title = track.title.ifBlank { existing.title },
                    artist = track.artist.takeIf { it.isNotBlank() && it != "Unknown" && it != "Unknown Artist" } ?: existing.artist,
                    album = album,
                    albumArtUri = track.albumArtUri ?: existing.albumArtUri,
                    isExplicit = track.isExplicit || existing.isExplicit,
                    artistId = track.artistId ?: existing.artistId,
                    albumId = track.albumId ?: existing.albumId,
                    duration = if (track.duration > 0L) track.duration else existing.duration,
                    path = path,
                    isYouTube = isYouTube
                )
            } else {
                insertTracksIgnore(listOf(track))
            }
        }
    }

    @Query("UPDATE tracks SET playCount = playCount + 1, lastPlayedAt = :timestamp WHERE id = :trackId")
    suspend fun incrementPlayCount(trackId: String, timestamp: Long)

    @Delete
    suspend fun deleteTrack(track: Track)

    @Query("SELECT * FROM tracks WHERE title LIKE :query OR artist LIKE :query OR album LIKE :query LIMIT 15")
    suspend fun searchTracksLocally(query: String): List<Track>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTrack(track: Track)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTracks(tracks: List<Track>)

    @Query("SELECT * FROM tracks")
    suspend fun getAllTracksOnce(): List<Track>

    @Query("DELETE FROM tracks")
    suspend fun clearAllTracks()

    @Query("UPDATE tracks SET canvasUrl = :canvasUrl WHERE id = :trackId")
    suspend fun updateCanvasUrl(trackId: String, canvasUrl: String?)

    /**
     * Fix stale DB entries: audio-only YouTube tracks (square googleusercontent album art)
     * that were incorrectly saved with album = "YouTube Video" or view/like strings due to old parser bugs.
     */
    @Query("""
        UPDATE tracks SET album = 'Unknown', path = 'https://www.youtube.com/watch?v=' || id
        WHERE isYouTube = 1
        AND (albumArtUri LIKE '%googleusercontent.com%' OR (albumArtUri NOT LIKE '%i.ytimg.com%' AND (path LIKE 'yt://%' OR path LIKE 'https://%')))
        AND (album IN ('YouTube Video', 'Video', 'Music Video', 'Video Chart') OR LOWER(album) LIKE '%view%' OR LOWER(album) LIKE '%like%')
    """)
    suspend fun fixMisclassifiedAudioTracks()

    /**
     * Fix stale DB entries: YouTube Video tracks (16:9 i.ytimg.com thumbnails) that were
     * previously stored with 'Up Next', 'Music Video', 'Video Chart', 'Cached', or view/like strings
     * as their album and 'https://'/'yt://' instead of 'ytvideo://'.
     */
    @Query("""
        UPDATE tracks SET album = 'YouTube Video', path = 'ytvideo://' || id
        WHERE isYouTube = 1
        AND albumArtUri LIKE '%i.ytimg.com/vi/%'
        AND (
            album IN ('Up Next', 'Music Video', 'Video Chart', 'Video', 'Cached', 'Cache', 'Unknown', '')
            OR LOWER(album) LIKE '%views%'
            OR LOWER(album) LIKE '%likes%'
        )
    """)
    suspend fun fixMisclassifiedVideoTracks()
}
