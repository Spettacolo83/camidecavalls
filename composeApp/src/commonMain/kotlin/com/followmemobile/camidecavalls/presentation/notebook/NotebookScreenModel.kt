package com.followmemobile.camidecavalls.presentation.notebook

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.followmemobile.camidecavalls.domain.model.TrackingSession
import com.followmemobile.camidecavalls.domain.repository.LanguageRepository
import com.followmemobile.camidecavalls.domain.usecase.tracking.DeleteSessionUseCase
import com.followmemobile.camidecavalls.domain.usecase.tracking.GetAllSessionsUseCase
import com.followmemobile.camidecavalls.domain.util.LocalizedStrings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ScreenModel for the Notebook/Diary screen.
 * Shows list of recorded tracking sessions.
 */
class NotebookScreenModel(
    private val getAllSessionsUseCase: GetAllSessionsUseCase,
    private val deleteSessionUseCase: DeleteSessionUseCase,
    private val languageRepository: LanguageRepository
) : ScreenModel {

    private val _uiState = MutableStateFlow(NotebookUiState())
    val uiState: StateFlow<NotebookUiState> = _uiState.asStateFlow()

    init {
        // Observe language changes
        screenModelScope.launch {
            languageRepository.observeCurrentLanguage().collect { languageCode ->
                _uiState.update {
                    it.copy(strings = LocalizedStrings(languageCode))
                }
            }
        }

        // Load sessions
        loadSessions()
    }

    private fun loadSessions() {
        screenModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                getAllSessionsUseCase().collect { sessions ->
                    // Filter to only completed sessions
                    val completedSessions = sessions.filter { it.isCompleted }
                    _uiState.update {
                        it.copy(
                            sessions = completedSessions,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    fun showDeleteConfirmation(session: TrackingSession) {
        _uiState.update { it.copy(sessionToDelete = session) }
    }

    fun dismissDeleteConfirmation() {
        _uiState.update { it.copy(sessionToDelete = null) }
    }

    fun confirmDelete() {
        val session = _uiState.value.sessionToDelete ?: return
        screenModelScope.launch {
            try {
                deleteSessionUseCase(session.id)
                _uiState.update { it.copy(sessionToDelete = null) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        sessionToDelete = null,
                        error = e.message
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * UI State for NotebookScreen
 */
data class NotebookUiState(
    val sessions: List<TrackingSession> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val strings: LocalizedStrings = LocalizedStrings("en"),
    val sessionToDelete: TrackingSession? = null
)
