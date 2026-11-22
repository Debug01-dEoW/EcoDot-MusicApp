package com.example.ecodot.di

import com.example.ecodot.ui.viewmodel.AlbumViewModel
import com.example.ecodot.ui.viewmodel.ArtistViewModel
import com.example.ecodot.ui.viewmodel.MusicViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MusicViewModel(androidApplication()) }
    viewModel { AlbumViewModel(androidApplication()) }
    viewModel { ArtistViewModel(androidApplication()) }
    viewModel { com.example.ecodot.ui.viewmodel.HomeViewModel(get(), get()) }
    viewModel { com.example.ecodot.ui.viewmodel.SearchViewModel(get(), get(), get()) }
}
