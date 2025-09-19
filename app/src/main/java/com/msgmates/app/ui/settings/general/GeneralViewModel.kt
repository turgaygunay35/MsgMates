package com.msgmates.app.ui.settings.general

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msgmates.app.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class GeneralViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GeneralUiState())
    val uiState: StateFlow<GeneralUiState> = _uiState.asStateFlow()

    init {
        loadGeneralSettings()
    }

    private fun loadGeneralSettings() {
        viewModelScope.launch {
            settingsRepository.getAutoDownloadEnabled().collect { autoDownload ->
                _uiState.value = _uiState.value.copy(
                    autoDownloadEnabled = autoDownload,
                    isLoading = false
                )
            }
        }

        viewModelScope.launch {
            settingsRepository.getDataUsageMode().collect { dataUsageMode ->
                _uiState.value = _uiState.value.copy(dataUsageMode = dataUsageMode)
            }
        }

        viewModelScope.launch {
            settingsRepository.getCacheSize().collect { cacheSize ->
                _uiState.value = _uiState.value.copy(
                    cacheSize = formatStorageSize(cacheSize)
                )
            }
        }
    }

    fun updateAutoDownload(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoDownloadEnabled(enabled)
            _uiState.value = _uiState.value.copy(
                autoDownloadEnabled = enabled,
                showSuccessMessage = true
            )
        }
    }

    fun updateDataUsageMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.setDataUsageMode(mode)
            _uiState.value = _uiState.value.copy(
                dataUsageMode = mode,
                showSuccessMessage = true
            )
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            // TODO: Implement cache clearing
            _uiState.value = _uiState.value.copy(
                showSuccessMessage = true
            )
        }
    }

    fun openMediaDownloadSettings() {
        // TODO: Navigate to media download settings
    }

    fun openBackupSettings() {
        // TODO: Navigate to backup settings
    }

    fun openShortcutsSettings() {
        // TODO: Navigate to shortcuts settings
    }

    fun openStorageSettings() {
        // TODO: Navigate to storage settings
    }

    fun openAdvancedSettings() {
        // TODO: Navigate to advanced settings
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

data class GeneralUiState(
    val isLoading: Boolean = true,
    val autoDownloadEnabled: Boolean = true,
    val dataUsageMode: String = "wifi",
    val cacheSize: String = "0 B",
    val showSuccessMessage: Boolean = false
)

data class GeneralItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val showArrow: Boolean = true
)
