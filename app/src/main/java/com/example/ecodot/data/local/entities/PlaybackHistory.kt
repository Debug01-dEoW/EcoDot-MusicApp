package com.example.ecodot.data.local.entities

import androidx.room.*

@Entity(
    tableName = "playback_history",
    foreignKeys = [
        ForeignKey(
            entity = Track::class,
            parentColumns = ["id"],
            childColumns = ["trackId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("trackId")]
)
data class PlaybackHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val trackId: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class PlaybackHistoryWithTrack(
    @Embedded val history: PlaybackHistory,
    @Relation(
        parentColumn = "trackId",
        entityColumn = "id"
    )
    val track: Track
)

