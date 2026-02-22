package com.followmemobile.camidecavalls.presentation.main

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton for cross-tab route selection communication.
 * Used when RouteDetailScreen's flag FAB is tapped to navigate
 * back to MainScreen's MAP tab with a specific route selected.
 */
class RouteSelectionManager {
    private val _selectedRouteId = MutableStateFlow<Int?>(null)
    val selectedRouteId: StateFlow<Int?> = _selectedRouteId.asStateFlow()

    fun selectRoute(routeId: Int?) {
        _selectedRouteId.value = routeId
    }

    fun consume() {
        _selectedRouteId.value = null
    }
}
