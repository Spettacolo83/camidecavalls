package com.followmemobile.camidecavalls.di

import com.followmemobile.camidecavalls.data.local.CamiDatabaseWrapper
import com.followmemobile.camidecavalls.data.local.DatabaseDriverFactory
import com.followmemobile.camidecavalls.data.repository.POIRepositoryImpl
import com.followmemobile.camidecavalls.data.repository.RouteRepositoryImpl
import com.followmemobile.camidecavalls.data.repository.TrackingRepositoryImpl
import com.followmemobile.camidecavalls.domain.repository.POIRepository
import com.followmemobile.camidecavalls.domain.repository.RouteRepository
import com.followmemobile.camidecavalls.domain.repository.TrackingRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Koin module for dependency injection.
 * Platform-specific modules (platformModule) are defined in androidMain and iosMain.
 */
val appModule = module {
    // Database
    single { CamiDatabaseWrapper(get()) }

    // Repositories
    singleOf(::RouteRepositoryImpl) bind RouteRepository::class
    singleOf(::POIRepositoryImpl) bind POIRepository::class
    singleOf(::TrackingRepositoryImpl) bind TrackingRepository::class

    // Use cases will be added here as we create them
}

/**
 * Expect/Actual for platform-specific dependencies.
 */
expect val platformModule: Module
