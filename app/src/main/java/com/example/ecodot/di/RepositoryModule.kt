package com.example.ecodot.di

import com.example.ecodot.data.repository.MusicRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val repositoryModule = module {
    single {
        MusicRepository(
            context = androidContext(),
            trackDao = get(),
            historyDao = get(),
            profileDao = get(),
            playlistDao = get(),
            recentSearchDao = get(),
            followedArtistDao = get(),
            streamResolver = get()
        )
    }
    single<com.example.ecodot.domain.repository.IMusicRepository> { get<MusicRepository>() }
}
