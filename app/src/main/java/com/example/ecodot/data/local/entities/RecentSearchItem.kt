package com.example.ecodot.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_searches")
data class RecentSearchItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String, // Display title/text (e.g. "Ed Sheeran" or "Shape of You")
    val type: String,  // "Query", "Song", "Artist", "Album", "Video"
    val itemId: String? = null, // The trackId, artistId, or albumId
    val imageUrl: String? = null,
    val subtitle: String? = null, // Artist name for songs/albums, etc.
    val timestamp: Long = System.currentTimeMillis()
)
