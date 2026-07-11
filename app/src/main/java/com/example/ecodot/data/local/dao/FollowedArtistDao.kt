package com.example.ecodot.data.local.dao

import androidx.room.*
import com.example.ecodot.data.local.entities.FollowedArtist
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowedArtistDao {
    @Query("SELECT * FROM followed_artists ORDER BY followedAt DESC")
    fun getFollowedArtists(): Flow<List<FollowedArtist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun followArtist(artist: FollowedArtist)

    @Query("DELETE FROM followed_artists WHERE artistId = :artistId")
    suspend fun unfollowArtist(artistId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM followed_artists WHERE artistId = :artistId)")
    fun isArtistFollowed(artistId: String): Flow<Boolean>
}
