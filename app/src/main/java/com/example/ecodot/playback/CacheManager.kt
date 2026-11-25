package com.example.ecodot.playback

import android.content.Context
import android.os.Environment
import android.os.StatFs
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import com.example.ecodot.data.local.prefs.UserPreferencesManager
import java.io.File

data class StorageStats(
    val downloadedBytes: Long = 0L,
    val playerCacheBytes: Long = 0L,
    val thumbnailCacheBytes: Long = 0L,
    val databaseBytes: Long = 0L,
    val freeBytes: Long = 0L,
    val totalBytes: Long = 0L
) {
    val otherAppBytes: Long
        get() = (totalBytes - freeBytes - downloadedBytes - playerCacheBytes - thumbnailCacheBytes - databaseBytes)
            .coerceAtLeast(0L)
}

object CacheManager {
    private var simpleCache: SimpleCache? = null

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    fun getCache(context: Context): SimpleCache {
        return simpleCache ?: synchronized(this) {
            simpleCache ?: run {
                val cacheDir = File(context.cacheDir, "media_cache")
                val prefs = UserPreferencesManager(context)
                val limitMb = prefs.cacheLimitMb
                // If limitMb > 0 use user preference; otherwise 20 GB (dynamic/unlimited)
                val maxBytes = if (limitMb > 0) limitMb * 1024L * 1024L else 20L * 1024L * 1024L * 1024L
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
            dirSize(File(context.cacheDir, "media_cache"))
        }
    }

    fun getStorageStats(context: Context): StorageStats {
        val playerCacheBytes = getCacheSize(context)
        val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val downloadedBytes = dirSize(downloadsDir)
        val imageCacheDir = File(context.cacheDir, "image_cache")
        val thumbnailCacheBytes = dirSize(imageCacheDir)

        val dbDir = context.getDatabasePath("ecodot_database")?.parentFile
        val databaseBytes = dbDir?.listFiles()
            ?.filter { it.isFile && (it.name.contains("ecodot") || it.name.endsWith(".db") || it.name.endsWith(".db-wal") || it.name.endsWith(".db-shm")) }
            ?.sumOf { it.length() }
            ?: 0L

        val (freeBytes, totalBytes) = try {
            val stat = StatFs(Environment.getDataDirectory().path)
            stat.availableBytes to stat.totalBytes
        } catch (e: Exception) {
            0L to 0L
        }

        return StorageStats(
            downloadedBytes = downloadedBytes,
            playerCacheBytes = playerCacheBytes,
            thumbnailCacheBytes = thumbnailCacheBytes,
            databaseBytes = databaseBytes,
            freeBytes = freeBytes,
            totalBytes = totalBytes
        )
    }

    private fun dirSize(file: File?): Long {
        if (file == null || !file.exists()) return 0L
        if (file.isFile) return file.length()
        var total = 0L
        file.listFiles()?.forEach { child ->
            total += dirSize(child)
        }
        return total
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

    @OptIn(ExperimentalCoilApi::class)
    fun clearThumbnailCache(context: Context) {
        try {
            context.imageLoader.diskCache?.clear()
            context.imageLoader.memoryCache?.clear()
        } catch (e: Exception) {
            // ignore
        }
        try {
            val imageCacheDir = File(context.cacheDir, "image_cache")
            if (imageCacheDir.exists()) {
                imageCacheDir.deleteRecursively()
            }
        } catch (e: Exception) {
            // ignore
        }
    }
}
