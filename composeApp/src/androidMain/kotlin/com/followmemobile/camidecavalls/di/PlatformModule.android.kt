package com.followmemobile.camidecavalls.di

import com.followmemobile.camidecavalls.data.local.AppPreferences
import com.followmemobile.camidecavalls.data.local.DatabaseDriverFactory
import com.followmemobile.camidecavalls.data.service.AndroidLocationService
import com.followmemobile.camidecavalls.data.service.AndroidPermissionHandler
import com.followmemobile.camidecavalls.domain.service.LocationService
import com.followmemobile.camidecavalls.domain.service.PermissionHandler
import com.google.android.gms.location.LocationServices
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Android-specific Koin module.
 */
actual val platformModule = module {
    single { DatabaseDriverFactory(androidContext()) }
    single { AppPreferences(androidContext()) }

    // Permission Handler
    single { AndroidPermissionHandler(androidContext()) } bind PermissionHandler::class

    // Location Service
    single {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(androidContext())
        AndroidLocationService(androidContext(), fusedLocationClient)
    } bind LocationService::class
}
