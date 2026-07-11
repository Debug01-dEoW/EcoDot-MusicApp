package com.example.ecodot.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class Track(
    @PrimaryKey val id: String, // Changed to String to support YouTube IDs
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val path: String,
    val albumArtUri: String? = null,
    val isYouTube: Boolean = false,
    val artistId: String? = null,
    val albumId: String? = null,
    val genre: String? = null,
    val playCount: Int = 0,
    val lastPlayedAt: Long? = null,
    val isExplicit: Boolean = false,
    val canvasUrl: String? = null  // Looping Canvas video URL (Spotify-style)
)
