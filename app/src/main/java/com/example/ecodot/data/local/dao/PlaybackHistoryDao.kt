package com.example.ecodot.data.local.dao

import androidx.room.*
import com.example.ecodot.data.local.entities.PlaybackHistory
import com.example.ecodot.data.local.entities.PlaybackHistoryWithTrack
import com.example.ecodot.data.local.entities.Track
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaybackHistoryDao {
    @Insert
    suspend fun insertHistory(history: PlaybackHistory)

    @Query("SELECT * FROM playback_history ORDER BY timestamp DESC LIMIT 100")
    fun getFullHistory(): Flow<List<PlaybackHistory>>

    @Transaction
    @Query("SELECT * FROM playback_history ORDER BY timestamp DESC LIMIT 200")
    fun getHistoryWithTracks(): Flow<List<PlaybackHistoryWithTrack>>

    @Query("""
        SELECT t.* FROM playback_history h
        INNER JOIN tracks t ON h.trackId = t.id
        WHERE h.timestamp >= :sinceTimestamp
        GROUP BY h.trackId
        ORDER BY COUNT(h.trackId) DESC, MAX(h.timestamp) DESC
        LIMIT 10
    """)
    fun getTopTracksSince(sinceTimestamp: Long): Flow<List<Track>>

    @Query("DELETE FROM playback_history")
    suspend fun clearHistory()
}
