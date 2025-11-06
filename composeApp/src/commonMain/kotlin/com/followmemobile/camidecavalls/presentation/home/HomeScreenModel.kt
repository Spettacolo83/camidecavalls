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
 * ScreenModel for the Home screen.
 * Displays the list of all 20 routes.
 */
class HomeScreenModel(
    private val getAllRoutesUseCase: GetAllRoutesUseCase,
    private val languageRepository: LanguageRepository
) : ScreenModel {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadRoutes()
    }

    private fun loadRoutes() {
        screenModelScope.launch {
            _uiState.value = HomeUiState.Loading

            try {
                // Combine routes and language flows
                getAllRoutesUseCase().collect { routes ->
                    val currentLanguage = languageRepository.getCurrentLanguage()
                    val strings = LocalizedStrings(currentLanguage)

                    _uiState.value = if (routes.isEmpty()) {
                        HomeUiState.Empty(strings)
                    } else {
                        HomeUiState.Success(routes, currentLanguage, strings)
                    }
                }
            } catch (e: Exception) {
                println("HomeScreenModel: Error loading routes: ${e.message}")
                e.printStackTrace()
                _uiState.value = HomeUiState.Error("Failed to load routes: ${e.message}")
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
                    is HomeUiState.Empty -> {
                        _uiState.value = HomeUiState.Empty(newStrings)
                    }
                    is HomeUiState.Success -> {
                        _uiState.value = currentState.copy(
                            currentLanguage = newLanguage,
                            strings = newStrings
                        )
                    }
                    HomeUiState.Loading -> {
                        // Skip updates during loading
                    }
                    is HomeUiState.Error -> {
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

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Empty(val strings: LocalizedStrings) : HomeUiState
    data class Success(
        val routes: List<Route>,
        val currentLanguage: String,
        val strings: LocalizedStrings
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
