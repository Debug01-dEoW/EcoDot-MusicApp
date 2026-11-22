package com.example.ecodot.di

import com.example.ecodot.data.local.prefs.UserPreferencesManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}

class DefaultDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher = Dispatchers.Main
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
}

val appModule = module {
    single<DispatcherProvider> { DefaultDispatcherProvider() }
    single { UserPreferencesManager(androidContext()) }
}
