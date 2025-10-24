package com.followmemobile.camidecavalls

import android.app.Application
import com.followmemobile.camidecavalls.di.appModule
import com.followmemobile.camidecavalls.di.platformModule
import com.followmemobile.camidecavalls.domain.usecase.route.InitializeDatabaseUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Android Application class for initializing Koin with Android Context
 * and populating the database on first launch.
 */
class CamiApp : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val initializeDatabaseUseCase: InitializeDatabaseUseCase by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@CamiApp)
            modules(appModule, platformModule)
        }

        // Initialize database with route data on first launch
        applicationScope.launch {
            val initialized = initializeDatabaseUseCase()
            if (initialized) {
                println("Database initialized with ${com.followmemobile.camidecavalls.data.RouteData.routes.size} routes")
            }
        }
    }
}
