package com.followmemobile.camidecavalls.di

import com.followmemobile.camidecavalls.data.local.DatabaseDriverFactory
import org.koin.dsl.module

/**
 * iOS-specific Koin module.
 */
actual val platformModule = module {
    single { DatabaseDriverFactory() }
}
