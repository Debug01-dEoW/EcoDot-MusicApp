package com.example.ecodot.di

import com.example.ecodot.domain.usecase.*
import org.koin.dsl.module

val useCaseModule = module {
    factory { GetHomeFeedUseCase(get()) }
    factory { SearchMusicUseCase(get()) }
    factory { GetLyricsUseCase(get()) }
    factory { GetSponsorSegmentsUseCase(get()) }
}
