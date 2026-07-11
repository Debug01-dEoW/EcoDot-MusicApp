package com.example.ecodot.data.local.entities

import androidx.room.*

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Only one user for now
    val name: String = "EcoDot User",
    val favoriteGenres: String = "",
    val favoriteArtists: String = "",
    val avatarUri: String? = null,
    val isPro: Boolean = true
)
