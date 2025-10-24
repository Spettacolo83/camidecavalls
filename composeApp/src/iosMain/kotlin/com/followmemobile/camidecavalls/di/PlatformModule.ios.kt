package com.followmemobile.camidecavalls.di

import com.followmemobile.camidecavalls.data.local.DatabaseDriverFactory
import com.followmemobile.camidecavalls.data.service.IOSLocationService
import com.followmemobile.camidecavalls.data.service.IOSPermissionHandler
import com.followmemobile.camidecavalls.domain.service.LocationService
import com.followmemobile.camidecavalls.domain.service.PermissionHandler
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * iOS-specific Koin module.
 */
actual val platformModule = module {
    single { DatabaseDriverFactory() }

    // Permission Handler
    single { IOSPermissionHandler() } bind PermissionHandler::class

    // Location Service
    single { IOSLocationService() } bind LocationService::class
}
