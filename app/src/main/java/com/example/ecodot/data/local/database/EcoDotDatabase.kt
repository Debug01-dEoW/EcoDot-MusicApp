package com.example.ecodot.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.ecodot.data.local.dao.*
import com.example.ecodot.data.local.entities.*

@Database(
    entities = [
        Track::class, 
        Playlist::class, 
        PlaylistTrack::class, 
        PlaybackHistory::class, 
        UserProfile::class,
        RecentSearchItem::class,
        FollowedArtist::class
    ],
    version = 13,
    exportSchema = false
)
abstract class EcoDotDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun historyDao(): PlaybackHistoryDao
    abstract fun profileDao(): UserProfileDao
    abstract fun recentSearchDao(): RecentSearchDao
    abstract fun followedArtistDao(): FollowedArtistDao

    companion object {
        const val DATABASE_NAME = "ecodot_db"

        @Volatile
        private var INSTANCE: EcoDotDatabase? = null

        fun getInstance(context: android.content.Context): EcoDotDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    EcoDotDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
