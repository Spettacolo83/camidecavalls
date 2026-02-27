package com.followmemobile.camidecavalls.presentation.main

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton for cross-tab POI navigation communication.
 * Used when a POI proximity notification is tapped to navigate
 * back to MainScreen's MAP tab and show the POI popup.
 */
class PoiNavigationManager {
    private val _selectedPoiId = MutableStateFlow<Int?>(null)
    val selectedPoiId: StateFlow<Int?> = _selectedPoiId.asStateFlow()

    fun navigateToPoi(poiId: Int) {
        _selectedPoiId.value = poiId
    }

    fun consume() {
        _selectedPoiId.value = null
    }
}
