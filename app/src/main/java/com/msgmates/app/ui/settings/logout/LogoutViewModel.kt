package com.msgmates.app.ui.settings.logout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msgmates.app.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class LogoutViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogoutUiState())
    val uiState: StateFlow<LogoutUiState> = _uiState.asStateFlow()

    init {
        loadLogoutSettings()
    }

    private fun loadLogoutSettings() {
        viewModelScope.launch {
            settingsRepository.getCloseServerSessions().collect { closeServerSessions ->
                _uiState.value = _uiState.value.copy(
                    closeServerSessions = closeServerSessions,
                    isLoading = false
                )
            }
        }
    }

    fun updateServerSessions(close: Boolean) {
        viewModelScope.launch {
            settingsRepository.setCloseServerSessions(close)
            _uiState.value = _uiState.value.copy(
                closeServerSessions = close
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Clear local data
                settingsRepository.clearUserData()

                // Close server sessions if enabled
                if (_uiState.value.closeServerSessions) {
                    settingsRepository.closeServerSessions()
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    showSuccessMessage = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    showSuccessMessage = true
                )
            }
        }
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(showSuccessMessage = false)
    }
}

data class LogoutUiState(
    val isLoading: Boolean = true,
    val closeServerSessions: Boolean = true,
    val showSuccessMessage: Boolean = false
)
