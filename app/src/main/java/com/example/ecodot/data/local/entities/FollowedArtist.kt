package com.example.ecodot.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "followed_artists")
data class FollowedArtist(
    @PrimaryKey val artistId: String,
    val artistName: String,
    val imageUrl: String? = null,
    val followedAt: Long = System.currentTimeMillis()
)
