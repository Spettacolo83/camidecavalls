package com.followmemobile.camidecavalls.presentation.home

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.followmemobile.camidecavalls.domain.model.Route
import com.followmemobile.camidecavalls.domain.repository.LanguageRepository
import com.followmemobile.camidecavalls.domain.usecase.route.GetAllRoutesUseCase
import com.followmemobile.camidecavalls.domain.util.LocalizedStrings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ScreenModel for the Routes screen.
 * Displays the list of all 20 routes.
 */
class RoutesScreenModel(
    private val getAllRoutesUseCase: GetAllRoutesUseCase,
    private val languageRepository: LanguageRepository
) : ScreenModel {

    private val _uiState = MutableStateFlow<RoutesUiState>(RoutesUiState.Loading)
    val uiState: StateFlow<RoutesUiState> = _uiState.asStateFlow()

    init {
        loadRoutes()
    }

    private fun loadRoutes() {
        screenModelScope.launch {
            _uiState.value = RoutesUiState.Loading

            try {
                // Combine routes and language flows
                getAllRoutesUseCase().collect { routes ->
                    val currentLanguage = languageRepository.getCurrentLanguage()
                    val strings = LocalizedStrings(currentLanguage)

                    _uiState.value = if (routes.isEmpty()) {
                        RoutesUiState.Empty(strings)
                    } else {
                        RoutesUiState.Success(routes, currentLanguage, strings)
                    }
                }
            } catch (e: Exception) {
                println("RoutesScreenModel: Error loading routes: ${e.message}")
                e.printStackTrace()
                _uiState.value = RoutesUiState.Error("Failed to load routes: ${e.message}")
            }
        }
    }

    // Observe language changes separately
    init {
        screenModelScope.launch {
            languageRepository.observeCurrentLanguage().collect { newLanguage ->
                val currentState = _uiState.value
                val newStrings = LocalizedStrings(newLanguage)

                when (currentState) {
                    is RoutesUiState.Empty -> {
                        _uiState.value = RoutesUiState.Empty(newStrings)
                    }
                    is RoutesUiState.Success -> {
                        _uiState.value = currentState.copy(
                            currentLanguage = newLanguage,
                            strings = newStrings
                        )
                    }
                    RoutesUiState.Loading -> {
                        // Skip updates during loading
                    }
                    is RoutesUiState.Error -> {
                        // Skip updates during error state
                    }
                }
            }
        }
    }

    fun onRouteClick(route: Route) {
        // Navigation will be handled by the screen
    }
}

sealed interface RoutesUiState {
    data object Loading : RoutesUiState
    data class Empty(val strings: LocalizedStrings) : RoutesUiState
    data class Success(
        val routes: List<Route>,
        val currentLanguage: String,
        val strings: LocalizedStrings
    ) : RoutesUiState
    data class Error(val message: String) : RoutesUiState
}
