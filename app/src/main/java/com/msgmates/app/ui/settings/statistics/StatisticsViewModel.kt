package com.msgmates.app.ui.settings.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msgmates.app.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
        }

        // Flow'ları collect et
        viewModelScope.launch {
            settingsRepository.getSentMessagesCount().collect { sentMessages ->
                _uiState.value = _uiState.value.copy(sentMessages = sentMessages)
            }
        }

        viewModelScope.launch {
            settingsRepository.getReceivedMessagesCount().collect { receivedMessages ->
                _uiState.value = _uiState.value.copy(receivedMessages = receivedMessages)
            }
        }

        viewModelScope.launch {
            settingsRepository.getTextStorageUsed().collect { textStorage ->
                _uiState.value = _uiState.value.copy(
                    textStorageUsed = formatStorageSize(textStorage),
                    isLoading = false
                )
            }
        }

        viewModelScope.launch {
            settingsRepository.getMediaStorageUsed().collect { mediaStorage ->
                _uiState.value = _uiState.value.copy(
                    mediaStorageUsed = formatStorageSize(mediaStorage)
                )
            }
        }

        viewModelScope.launch {
            settingsRepository.getContactCount().collect { contactCount ->
                _uiState.value = _uiState.value.copy(contactCount = contactCount)
            }
        }

        viewModelScope.launch {
            settingsRepository.getGroupCount().collect { groupCount ->
                _uiState.value = _uiState.value.copy(groupCount = groupCount)
            }
        }

        viewModelScope.launch {
            settingsRepository.getLastBackupDate().collect { lastBackup ->
                _uiState.value = _uiState.value.copy(lastBackupDate = lastBackup)
            }
        }
    }

    fun startCleanupWizard() {
        viewModelScope.launch {
            // TODO: Implement cleanup wizard
            _uiState.value = _uiState.value.copy(
                showSuccessMessage = true
            )
        }
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(showSuccessMessage = false)
    }

    private fun formatStorageSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024 * 1024)} GB"
            bytes >= 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            bytes >= 1024 -> "${bytes / 1024} KB"
            else -> "$bytes B"
        }
    }
}

data class StatisticsUiState(
    val isLoading: Boolean = true,
    val sentMessages: Int = 0,
    val receivedMessages: Int = 0,
    val textStorageUsed: String = "0 B",
    val mediaStorageUsed: String = "0 B",
    val totalStorageUsed: String = "0 B",
    val contactCount: Int = 0,
    val groupCount: Int = 0,
    val lastBackupDate: String = "Hiç yedeklenmedi",
    val showSuccessMessage: Boolean = false
)
