package com.example.ecodot.data.local.dao

import androidx.room.*
import com.example.ecodot.data.local.entities.RecentSearchItem
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentSearchDao {
    @Query("SELECT * FROM recent_searches ORDER BY timestamp DESC LIMIT 20")
    fun getRecentSearches(): Flow<List<RecentSearchItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentSearch(item: RecentSearchItem)

    @Query("DELETE FROM recent_searches WHERE `query` = :query AND type = :type")
    suspend fun deleteByQueryAndType(query: String, type: String)

    @Delete
    suspend fun deleteRecentSearch(item: RecentSearchItem)

    @Query("DELETE FROM recent_searches")
    suspend fun clearAllRecentSearches()
}
