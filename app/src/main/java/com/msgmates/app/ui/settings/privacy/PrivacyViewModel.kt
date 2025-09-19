package com.msgmates.app.ui.settings.privacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msgmates.app.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class PrivacyViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrivacyUiState())
    val uiState: StateFlow<PrivacyUiState> = _uiState.asStateFlow()

    init {
        loadPrivacySettings()
    }

    private fun loadPrivacySettings() {
        viewModelScope.launch {
            settingsRepository.getLastSeenEnabled().collect { lastSeen ->
                _uiState.value = _uiState.value.copy(
                    lastSeenEnabled = lastSeen,
                    isLoading = false
                )
            }
        }

        viewModelScope.launch {
            settingsRepository.getOnlineStatusEnabled().collect { onlineStatus ->
                _uiState.value = _uiState.value.copy(onlineStatusEnabled = onlineStatus)
            }
        }

        viewModelScope.launch {
            settingsRepository.getReadReceiptsEnabled().collect { readReceipts ->
                _uiState.value = _uiState.value.copy(readReceiptsEnabled = readReceipts)
            }
        }
    }

    fun toggleLastSeen() {
        viewModelScope.launch {
            val currentState = _uiState.value.lastSeenEnabled
            settingsRepository.setLastSeenEnabled(!currentState)
            _uiState.value = _uiState.value.copy(
                lastSeenEnabled = !currentState,
                showSuccessMessage = true
            )
        }
    }

    fun toggleOnlineStatus() {
        viewModelScope.launch {
            val currentState = _uiState.value.onlineStatusEnabled
            settingsRepository.setOnlineStatusEnabled(!currentState)
            _uiState.value = _uiState.value.copy(
                onlineStatusEnabled = !currentState,
                showSuccessMessage = true
            )
        }
    }

    fun toggleReadReceipts() {
        viewModelScope.launch {
            val currentState = _uiState.value.readReceiptsEnabled
            settingsRepository.setReadReceiptsEnabled(!currentState)
            _uiState.value = _uiState.value.copy(
                readReceiptsEnabled = !currentState,
                showSuccessMessage = true
            )
        }
    }

    fun openProfilePhotoSettings() {
        // TODO: Navigate to profile photo privacy settings
    }

    fun openStatusMessageSettings() {
        // TODO: Navigate to status message privacy settings
    }

    fun openBlockedUsers() {
        // TODO: Navigate to blocked users list
    }

    fun openBlockedBy() {
        // TODO: Navigate to blocked by list
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(showSuccessMessage = false)
    }
}

data class PrivacyUiState(
    val isLoading: Boolean = true,
    val lastSeenEnabled: Boolean = true,
    val onlineStatusEnabled: Boolean = true,
    val readReceiptsEnabled: Boolean = true,
    val showSuccessMessage: Boolean = false
)

data class PrivacyItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val showSwitch: Boolean = false,
    val showArrow: Boolean = false
)
