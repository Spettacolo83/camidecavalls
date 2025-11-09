package com.followmemobile.camidecavalls.di

import com.followmemobile.camidecavalls.data.local.CamiDatabaseWrapper
import com.followmemobile.camidecavalls.data.repository.LanguageRepositoryImpl
import com.followmemobile.camidecavalls.data.repository.POIRepositoryImpl
import com.followmemobile.camidecavalls.data.repository.RouteRepositoryImpl
import com.followmemobile.camidecavalls.data.repository.TrackingRepositoryImpl
import com.followmemobile.camidecavalls.domain.repository.LanguageRepository
import com.followmemobile.camidecavalls.domain.repository.POIRepository
import com.followmemobile.camidecavalls.domain.repository.RouteRepository
import com.followmemobile.camidecavalls.domain.repository.TrackingRepository
import com.followmemobile.camidecavalls.domain.usecase.poi.GetAllPOIsUseCase
import com.followmemobile.camidecavalls.domain.usecase.poi.GetPOIsByRouteUseCase
import com.followmemobile.camidecavalls.domain.usecase.poi.GetPOIsByTypeUseCase
import com.followmemobile.camidecavalls.domain.usecase.poi.GetPOIsNearLocationUseCase
import com.followmemobile.camidecavalls.domain.usecase.poi.InitializePOIsUseCase
import com.followmemobile.camidecavalls.domain.usecase.poi.SavePOIsUseCase
import com.followmemobile.camidecavalls.domain.usecase.GetSimplifiedRoutesUseCase
import com.followmemobile.camidecavalls.domain.usecase.route.GetAllRoutesUseCase
import com.followmemobile.camidecavalls.domain.usecase.route.GetRouteByIdUseCase
import com.followmemobile.camidecavalls.domain.usecase.route.GetRouteByNumberUseCase
import com.followmemobile.camidecavalls.domain.usecase.route.InitializeDatabaseUseCase
import com.followmemobile.camidecavalls.domain.usecase.route.SaveRoutesUseCase
import com.followmemobile.camidecavalls.domain.usecase.tracking.AddTrackPointUseCase
import com.followmemobile.camidecavalls.domain.usecase.tracking.CalculateSessionStatsUseCase
import com.followmemobile.camidecavalls.domain.usecase.tracking.DeleteSessionUseCase
import com.followmemobile.camidecavalls.domain.usecase.tracking.GetActiveSessionUseCase
import com.followmemobile.camidecavalls.domain.usecase.tracking.GetAllSessionsUseCase
import com.followmemobile.camidecavalls.domain.usecase.tracking.GetSessionByIdUseCase
import com.followmemobile.camidecavalls.domain.usecase.tracking.StartTrackingSessionUseCase
import com.followmemobile.camidecavalls.domain.usecase.tracking.StopTrackingSessionUseCase
import com.followmemobile.camidecavalls.domain.usecase.tracking.TrackingManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import com.followmemobile.camidecavalls.presentation.detail.RouteDetailScreenModel
import com.followmemobile.camidecavalls.presentation.fullmap.FullMapScreenModel
import com.followmemobile.camidecavalls.presentation.home.HomeScreenModel
import com.followmemobile.camidecavalls.presentation.pois.POIsScreenModel
import com.followmemobile.camidecavalls.presentation.settings.SettingsScreenModel
import com.followmemobile.camidecavalls.presentation.tracking.TrackingScreenModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
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
    singleOf(::LanguageRepositoryImpl) bind LanguageRepository::class

    // Route Use Cases
    factoryOf(::GetAllRoutesUseCase)
    factoryOf(::GetRouteByIdUseCase)
    factoryOf(::GetRouteByNumberUseCase)
    factoryOf(::SaveRoutesUseCase)
    factoryOf(::InitializeDatabaseUseCase)
    factoryOf(::GetSimplifiedRoutesUseCase)

    // POI Use Cases
    factoryOf(::GetAllPOIsUseCase)
    factoryOf(::GetPOIsByTypeUseCase)
    factoryOf(::GetPOIsNearLocationUseCase)
    factoryOf(::GetPOIsByRouteUseCase)
    factoryOf(::SavePOIsUseCase)
    factoryOf(::InitializePOIsUseCase)

    // Tracking Use Cases
    factoryOf(::CalculateSessionStatsUseCase)
    factoryOf(::StartTrackingSessionUseCase)
    factoryOf(::StopTrackingSessionUseCase)
    factoryOf(::AddTrackPointUseCase)
    factoryOf(::GetActiveSessionUseCase)
    factoryOf(::GetAllSessionsUseCase)
    factoryOf(::GetSessionByIdUseCase)
    factoryOf(::DeleteSessionUseCase)

    // Tracking Manager - Singleton with dedicated coroutine scope
    single {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        TrackingManager(
            locationService = get(),
            startTrackingSessionUseCase = get(),
            stopTrackingSessionUseCase = get(),
            addTrackPointUseCase = get(),
            getActiveSessionUseCase = get(),
            scope = scope
        )
    }

    // ScreenModels
    factoryOf(::HomeScreenModel)
    factoryOf(::FullMapScreenModel)
    factoryOf(::POIsScreenModel)
    factoryOf(::SettingsScreenModel)
    factory { params -> RouteDetailScreenModel(params.get(), get(), get()) }
    factory { params ->
        TrackingScreenModel(
            trackingManager = get(),
            permissionHandler = get(),
            getRouteByIdUseCase = get(),
            getActiveSessionUseCase = get(),
            routeId = params.getOrNull()
        )
    }
}

/**
 * Expect/Actual for platform-specific dependencies.
 */
expect val platformModule: Module
