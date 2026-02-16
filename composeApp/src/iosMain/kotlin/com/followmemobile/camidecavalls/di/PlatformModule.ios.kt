package com.followmemobile.camidecavalls.di

import com.followmemobile.camidecavalls.data.local.AppPreferences
import com.followmemobile.camidecavalls.data.local.DatabaseDriverFactory
import com.followmemobile.camidecavalls.data.service.IOSLocationService
import com.followmemobile.camidecavalls.data.service.IOSPermissionHandler
import com.followmemobile.camidecavalls.domain.service.BackgroundTrackingManager
import com.followmemobile.camidecavalls.domain.service.LocationService
import com.followmemobile.camidecavalls.domain.service.PermissionHandler
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import org.koin.dsl.bind
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

/**
 * iOS-specific Koin module.
 */
actual val platformModule = module {
    single { DatabaseDriverFactory() }
    single { AppPreferences() }

    // Settings for multiplatform-settings
    single<Settings> {
        NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
    }

    // Permission Handler
    single { IOSPermissionHandler() } bind PermissionHandler::class

    // Location Service
    single { IOSLocationService() } bind LocationService::class

    // Background Tracking Manager
    single { BackgroundTrackingManager() }
}
