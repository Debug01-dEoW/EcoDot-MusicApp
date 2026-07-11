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
                val path = if (!existing.isYouTube) existing.path else track.path
                val isYouTube = if (!existing.isYouTube) false else track.isYouTube
                updateTrackMetadata(
                    id = track.id,
                    title = track.title,
                    artist = track.artist,
                    album = track.album,
                    albumArtUri = track.albumArtUri ?: existing.albumArtUri,
                    isExplicit = track.isExplicit,
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

    @Query("UPDATE tracks SET canvasUrl = :canvasUrl WHERE id = :trackId")
    suspend fun updateCanvasUrl(trackId: String, canvasUrl: String?)
}
