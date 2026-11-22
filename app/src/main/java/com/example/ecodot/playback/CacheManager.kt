package com.example.ecodot.playback

import android.content.Context
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import com.example.ecodot.data.local.prefs.UserPreferencesManager
import java.io.File

object CacheManager {
    private var simpleCache: SimpleCache? = null

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun getCache(context: Context): SimpleCache {
        return simpleCache ?: synchronized(this) {
            simpleCache ?: run {
                val cacheDir = File(context.cacheDir, "media_cache")
                val prefs = UserPreferencesManager(context)
                val limitMb = prefs.cacheLimitMb
                // If limitMb > 0 use user preference; otherwise 20 GB (dynamic/unlimited, no arbitrary 500MB cap)
                val maxBytes = if (limitMb > 0) limitMb * 1024 * 1024 else 20L * 1024 * 1024 * 1024
                val evictor = LeastRecentlyUsedCacheEvictor(maxBytes)
                val databaseProvider = StandaloneDatabaseProvider(context)
                SimpleCache(cacheDir, evictor, databaseProvider).also {
                    simpleCache = it
                }
            }
        }
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun getCacheSize(context: Context): Long {
        return try {
            getCache(context).cacheSpace
        } catch (e: Exception) {
            0L
        }
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun removeCachedSong(context: Context, key: String) {
        try {
            val cache = getCache(context)
            cache.removeResource("yt_$key")
            cache.removeResource(key)
            cache.removeResource("yt://$key")
            cache.keys.filter { it.contains(key) }.forEach { resKey ->
                try {
                    cache.removeResource(resKey)
                } catch (e: Exception) {
                    // ignore
                }
            }
        } catch (e: Exception) {
            // ignore
        }
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun clearCache(context: Context) {
        try {
            val cache = getCache(context)
            val keys = cache.keys.toList()
            keys.forEach { key ->
                try {
                    cache.removeResource(key)
                } catch (e: Exception) {
                    // ignore
                }
            }
        } catch (e: Exception) {
            // ignore
        }
        try {
            val cacheDir = File(context.cacheDir, "media_cache")
            if (cacheDir.exists()) {
                cacheDir.listFiles()?.forEach { file ->
                    if (!file.name.endsWith(".uid") && !file.name.endsWith(".db")) {
                        file.deleteRecursively()
                    }
                }
            }
        } catch (e: Exception) {
            // ignore
        }
    }
}
