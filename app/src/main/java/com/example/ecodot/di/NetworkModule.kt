package com.example.ecodot.di

import com.example.ecodot.data.remote.LyricsRepository
import com.example.ecodot.data.remote.YouTubeApiService
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

val networkModule = module {
    single {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl("https://music.youtube.com/youtubei/v1/")
            .client(get<OkHttpClient>())
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    single<YouTubeApiService> {
        get<Retrofit>().create(YouTubeApiService::class.java)
    }

    single { LyricsRepository() }
    single { com.example.ecodot.data.remote.SponsorBlockService(get()) }
    single { com.example.ecodot.data.remote.YouTubeStreamResolver(get()) }
}
