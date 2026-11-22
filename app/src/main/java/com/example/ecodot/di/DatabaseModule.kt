package com.example.ecodot.di

import com.example.ecodot.data.local.database.EcoDotDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single { EcoDotDatabase.getInstance(androidContext()) }
    single { get<EcoDotDatabase>().trackDao() }
    single { get<EcoDotDatabase>().playlistDao() }
    single { get<EcoDotDatabase>().historyDao() }
    single { get<EcoDotDatabase>().profileDao() }
    single { get<EcoDotDatabase>().recentSearchDao() }
    single { get<EcoDotDatabase>().followedArtistDao() }
}
