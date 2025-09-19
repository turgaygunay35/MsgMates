package com.msgmates.app.ui.settings.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msgmates.app.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class MessagesViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessagesUiState())
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    init {
        loadMessagesSettings()
    }

    private fun loadMessagesSettings() {
        viewModelScope.launch {
            settingsRepository.getReadReceiptsEnabled().collect { readReceipts ->
                _uiState.value = _uiState.value.copy(
                    readReceiptsEnabled = readReceipts,
                    isLoading = false
                )
            }
        }

        viewModelScope.launch {
            settingsRepository.getTypingIndicatorEnabled().collect { typingIndicator ->
                _uiState.value = _uiState.value.copy(typingIndicatorEnabled = typingIndicator)
            }
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

    fun toggleTypingIndicator() {
        viewModelScope.launch {
            val currentState = _uiState.value.typingIndicatorEnabled
            settingsRepository.setTypingIndicatorEnabled(!currentState)
            _uiState.value = _uiState.value.copy(
                typingIndicatorEnabled = !currentState,
                showSuccessMessage = true
            )
        }
    }

    fun openMessageArchive() {
        // TODO: Navigate to message archive
    }

    fun openMessageBackup() {
        // TODO: Navigate to message backup
    }

    fun openMessageCleanup() {
        // TODO: Navigate to message cleanup
    }

    fun openMessageExport() {
        // TODO: Navigate to message export
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(showSuccessMessage = false)
    }
}

data class MessagesUiState(
    val isLoading: Boolean = true,
    val readReceiptsEnabled: Boolean = true,
    val typingIndicatorEnabled: Boolean = true,
    val showSuccessMessage: Boolean = false
)

data class MessagesItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val showSwitch: Boolean = false,
    val showArrow: Boolean = false
)
