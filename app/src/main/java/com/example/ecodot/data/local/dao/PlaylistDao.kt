package com.example.ecodot.data.local.dao

import androidx.room.*
import com.example.ecodot.data.local.entities.Playlist
import com.example.ecodot.data.local.entities.PlaylistTrack
import com.example.ecodot.data.local.entities.Track
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Query("SELECT * FROM playlists WHERE id = :playlistId LIMIT 1")
    suspend fun getPlaylistById(playlistId: Long): Playlist?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist): Long

    @Update
    suspend fun updatePlaylist(playlist: Playlist)

    @Delete
    suspend fun deletePlaylist(playlist: Playlist)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTrackToPlaylist(playlistTrack: PlaylistTrack)

    @Delete
    suspend fun removeTrackFromPlaylist(playlistTrack: PlaylistTrack)

    @Query("""
        SELECT tracks.* FROM tracks 
        INNER JOIN playlist_tracks ON tracks.id = playlist_tracks.trackId 
        WHERE playlist_tracks.playlistId = :playlistId
        ORDER BY playlist_tracks.addedAt DESC
    """)
    fun getTracksInPlaylist(playlistId: Long): Flow<List<Track>>

    @Query("SELECT COUNT(*) FROM playlist_tracks WHERE playlistId = :playlistId")
    fun getPlaylistTrackCount(playlistId: Long): Flow<Int>

    @Query("SELECT * FROM playlists WHERE id IN (SELECT playlistId FROM playlist_tracks WHERE trackId = :trackId)")
    fun getPlaylistsContainingTrack(trackId: String): Flow<List<Playlist>>

    @Query("SELECT * FROM playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId LIMIT 1")
    suspend fun getPlaylistTrack(playlistId: Long, trackId: String): PlaylistTrack?

    /**
     * Reactively emits true if the given track is in the "Liked Songs" playlist.
     * This is the single source of truth for the like state.
     */
    @Query("""
        SELECT COUNT(*) > 0 FROM playlist_tracks pt
        INNER JOIN playlists p ON p.id = pt.playlistId
        WHERE p.name = 'Liked Songs' AND pt.trackId = :trackId
    """)
    fun isTrackLiked(trackId: String): Flow<Boolean>

    @Query("SELECT * FROM playlists WHERE name = :name LIMIT 1")
    suspend fun getPlaylistByName(name: String): Playlist?

    @Query("SELECT DISTINCT trackId FROM playlist_tracks")
    fun getAllPlaylistTrackIds(): Flow<List<String>>

    @Query("""
        SELECT COUNT(*) > 0 FROM playlist_tracks pt
        INNER JOIN playlists p ON p.id = pt.playlistId
        WHERE p.name = 'Liked Songs' AND pt.trackId = :trackId
    """)
    suspend fun isTrackLikedSuspended(trackId: String): Boolean

    @Query("SELECT * FROM playlists WHERE id = :playlistId LIMIT 1")
    fun getPlaylistByIdFlow(playlistId: Long): Flow<Playlist?>

    @Query("UPDATE playlist_tracks SET addedAt = :addedAt WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun updateTrackAddedAt(playlistId: Long, trackId: String, addedAt: Long)

    @Transaction
    suspend fun reorderTracks(playlistId: Long, trackIdsInOrder: List<String>) {
        val now = System.currentTimeMillis()
        trackIdsInOrder.forEachIndexed { index, trackId ->
            updateTrackAddedAt(playlistId, trackId, now - index * 1000L)
        }
    }
}

