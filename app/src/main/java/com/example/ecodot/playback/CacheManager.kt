package com.example.ecodot.playback

import android.content.Context
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

object CacheManager {
    private var simpleCache: SimpleCache? = null

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun getCache(context: Context): SimpleCache {
        return simpleCache ?: synchronized(this) {
            simpleCache ?: run {
                val cacheDir = File(context.cacheDir, "media_cache")
                val evictor = LeastRecentlyUsedCacheEvictor(500 * 1024 * 1024) // 500 MB limit
                val databaseProvider = StandaloneDatabaseProvider(context)
                SimpleCache(cacheDir, evictor, databaseProvider).also {
                    simpleCache = it
                }
            }
        }
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun getCacheSize(context: Context): Long {
        return getCache(context).cacheSpace
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun clearCache(context: Context) {
        val cache = getCache(context)
        cache.keys.forEach { key ->
            cache.removeResource(key)
        }
    }
}
