package com.followmemobile.camidecavalls.presentation.home

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.followmemobile.camidecavalls.domain.model.Route
import com.followmemobile.camidecavalls.domain.usecase.route.GetAllRoutesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ScreenModel for the Home screen.
 * Displays the list of all 20 routes.
 */
class HomeScreenModel(
    private val getAllRoutesUseCase: GetAllRoutesUseCase
) : ScreenModel {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadRoutes()
    }

    private fun loadRoutes() {
        screenModelScope.launch {
            _uiState.value = HomeUiState.Loading
            getAllRoutesUseCase().collect { routes ->
                _uiState.value = if (routes.isEmpty()) {
                    HomeUiState.Empty
                } else {
                    HomeUiState.Success(routes)
                }
            }
        }
    }

    fun onRouteClick(route: Route) {
        // Navigation will be handled by the screen
    }
}

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object Empty : HomeUiState
    data class Success(val routes: List<Route>) : HomeUiState
}
