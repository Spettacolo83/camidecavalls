package com.followmemobile.camidecavalls.presentation.detail

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.followmemobile.camidecavalls.domain.model.Route
import com.followmemobile.camidecavalls.domain.usecase.route.GetRouteByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ScreenModel for the Route Detail screen.
 * Displays detailed information about a specific route.
 */
class RouteDetailScreenModel(
    private val routeId: Int,
    private val getRouteByIdUseCase: GetRouteByIdUseCase
) : ScreenModel {

    private val _uiState = MutableStateFlow<RouteDetailUiState>(RouteDetailUiState.Loading)
    val uiState: StateFlow<RouteDetailUiState> = _uiState.asStateFlow()

    init {
        loadRoute()
    }

    private fun loadRoute() {
        screenModelScope.launch {
            _uiState.value = RouteDetailUiState.Loading
            val route = getRouteByIdUseCase(routeId)
            _uiState.value = if (route != null) {
                RouteDetailUiState.Success(route)
            } else {
                RouteDetailUiState.Error("Route not found")
            }
        }
    }
}

sealed interface RouteDetailUiState {
    data object Loading : RouteDetailUiState
    data class Success(val route: Route) : RouteDetailUiState
    data class Error(val message: String) : RouteDetailUiState
}
