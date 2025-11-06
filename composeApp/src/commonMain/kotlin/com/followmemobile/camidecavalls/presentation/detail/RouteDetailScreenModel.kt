package com.followmemobile.camidecavalls.presentation.detail

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.followmemobile.camidecavalls.domain.model.Route
import com.followmemobile.camidecavalls.domain.repository.LanguageRepository
import com.followmemobile.camidecavalls.domain.usecase.route.GetRouteByIdUseCase
import com.followmemobile.camidecavalls.domain.util.LocalizedStrings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * ScreenModel for the Route Detail screen.
 * Displays detailed information about a specific route.
 */
class RouteDetailScreenModel(
    private val routeId: Int,
    private val getRouteByIdUseCase: GetRouteByIdUseCase,
    private val languageRepository: LanguageRepository
) : ScreenModel {

    private val _uiState = MutableStateFlow<RouteDetailUiState>(RouteDetailUiState.Loading)
    val uiState: StateFlow<RouteDetailUiState> = _uiState.asStateFlow()

    init {
        loadRoute()
        observeLanguageChanges()
    }

    private fun loadRoute() {
        screenModelScope.launch {
            _uiState.value = RouteDetailUiState.Loading
            val route = getRouteByIdUseCase(routeId)
            val currentLanguage = languageRepository.getCurrentLanguage()
            val strings = LocalizedStrings(currentLanguage)
            _uiState.value = if (route != null) {
                RouteDetailUiState.Success(route, currentLanguage, strings)
            } else {
                RouteDetailUiState.Error("Route not found")
            }
        }
    }

    private fun observeLanguageChanges() {
        screenModelScope.launch {
            languageRepository.observeCurrentLanguage().collect { newLanguage ->
                val currentState = _uiState.value
                if (currentState is RouteDetailUiState.Success) {
                    val newStrings = LocalizedStrings(newLanguage)
                    _uiState.value = currentState.copy(
                        currentLanguage = newLanguage,
                        strings = newStrings
                    )
                }
            }
        }
    }
}

sealed interface RouteDetailUiState {
    data object Loading : RouteDetailUiState
    data class Success(
        val route: Route,
        val currentLanguage: String,
        val strings: LocalizedStrings
    ) : RouteDetailUiState
    data class Error(val message: String) : RouteDetailUiState
}
