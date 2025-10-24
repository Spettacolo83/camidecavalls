package com.followmemobile.camidecavalls.di

import com.followmemobile.camidecavalls.data.local.DatabaseDriverFactory
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Android-specific Koin module.
 */
actual val platformModule = module {
    single { DatabaseDriverFactory(androidContext()) }
}
