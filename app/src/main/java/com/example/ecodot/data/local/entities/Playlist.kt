package com.example.ecodot.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val coverArtUri: String? = null,
    val folder: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
