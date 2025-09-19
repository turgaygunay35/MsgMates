package com.msgmates.app.ui.settings.profile

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
class ShareSettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShareSettingsUiState())
    val uiState: StateFlow<ShareSettingsUiState> = _uiState.asStateFlow()

    init {
        loadShareSettings()
    }

    private fun loadShareSettings() {
        viewModelScope.launch {
            // Load current share settings from repository
            // For now, using default values
            _uiState.value = _uiState.value.copy(
                shareName = true,
                shareStatus = true,
                sharePhoto = false
            )
        }
    }

    fun updateShareName(share: Boolean) {
        _uiState.value = _uiState.value.copy(shareName = share)
    }

    fun updateShareStatus(share: Boolean) {
        _uiState.value = _uiState.value.copy(shareStatus = share)
    }

    fun updateSharePhoto(share: Boolean) {
        _uiState.value = _uiState.value.copy(sharePhoto = share)
    }

    fun saveShareSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val state = _uiState.value
                // Save share settings to repository
                // settingsRepository.setShareName(state.shareName)
                // settingsRepository.setShareStatus(state.shareStatus)
                // settingsRepository.setSharePhoto(state.sharePhoto)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    showSuccessMessage = true,
                    successMessage = "Paylaşım ayarları kaydedildi"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Ayarlar kaydedilemedi"
                )
            }
        }
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(showSuccessMessage = false, successMessage = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class ShareSettingsUiState(
    val isLoading: Boolean = false,
    val shareName: Boolean = true,
    val shareStatus: Boolean = true,
    val sharePhoto: Boolean = false,
    val showSuccessMessage: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null
)
