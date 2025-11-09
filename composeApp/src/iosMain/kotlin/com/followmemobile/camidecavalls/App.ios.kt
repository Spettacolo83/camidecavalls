package com.followmemobile.camidecavalls

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.followmemobile.camidecavalls.data.RouteData
import com.followmemobile.camidecavalls.di.appModule
import com.followmemobile.camidecavalls.di.platformModule
import com.followmemobile.camidecavalls.domain.usecase.route.InitializeDatabaseUseCase
import com.followmemobile.camidecavalls.domain.usecase.poi.InitializePOIsUseCase
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

/**
 * iOS implementation of KoinInitializer.
 * Initializes Koin using KoinApplication and populates the database on first launch.
 */
@Composable
actual fun KoinInitializer(content: @Composable () -> Unit) {
    KoinApplication(
        application = {
            modules(appModule, platformModule)
        }
    ) {
        // Initialize database on first launch
        val initializeDatabaseUseCase: InitializeDatabaseUseCase = koinInject()
        val initializePOIsUseCase: InitializePOIsUseCase = koinInject()

        LaunchedEffect(Unit) {
            val initialized = initializeDatabaseUseCase()
            if (initialized) {
                println("Database initialized with ${RouteData.routes.size} routes")
            }

            // Initialize POIs from JSON file
            val poisInitialized = initializePOIsUseCase()
            if (poisInitialized) {
                println("POIs initialized successfully")
            }
        }

        content()
    }
}
