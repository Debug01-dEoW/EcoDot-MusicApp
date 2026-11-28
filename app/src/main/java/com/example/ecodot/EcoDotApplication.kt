package com.example.ecodot

import android.app.Application
import android.content.Context
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.example.ecodot.di.*
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class EcoDotApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        ensureKoinStarted(this)
    }

    companion object {
        private val koinLock = Any()

        /**
         * Ensures Koin is initialized even if a background MediaSessionService wakes before Application.onCreate.
         */
        fun ensureKoinStarted(context: Context) {
            if (GlobalContext.getOrNull() != null) return
            synchronized(koinLock) {
                if (GlobalContext.getOrNull() != null) return
                startKoin {
                    androidLogger(Level.ERROR)
                    androidContext(context.applicationContext)
                    modules(
                        appModule,
                        databaseModule,
                        networkModule,
                        repositoryModule,
                        useCaseModule,
                        viewModelModule
                    )
                }
            }
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(250L * 1024 * 1024)
                    .build()
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .crossfade(true)
            .allowHardware(true)
            .build()
    }
}
