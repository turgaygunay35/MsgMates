package com.msgmates.app.ui.settings

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
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            // Load all settings in parallel
            val profileName = settingsRepository.getProfileName()
            val profileBio = settingsRepository.getProfileBio()
            val profilePhone = settingsRepository.getProfilePhone()
            val profileEmail = settingsRepository.getProfileEmail()

            // Update UI state with loaded settings
            _uiState.value = _uiState.value.copy(
                isLoading = false
            )
        }
    }

    fun updateProfileName(name: String) {
        viewModelScope.launch {
            settingsRepository.setProfileName(name)
            _uiState.value = _uiState.value.copy(
                profileName = name,
                showSuccessMessage = true
            )
        }
    }

    fun updateProfileBio(bio: String) {
        viewModelScope.launch {
            settingsRepository.setProfileBio(bio)
            _uiState.value = _uiState.value.copy(
                profileBio = bio,
                showSuccessMessage = true
            )
        }
    }

    fun updateProfilePhone(phone: String) {
        viewModelScope.launch {
            settingsRepository.setProfilePhone(phone)
            _uiState.value = _uiState.value.copy(
                profilePhone = phone,
                showSuccessMessage = true
            )
        }
    }

    fun updateProfileEmail(email: String) {
        viewModelScope.launch {
            settingsRepository.setProfileEmail(email)
            _uiState.value = _uiState.value.copy(
                profileEmail = email,
                showSuccessMessage = true
            )
        }
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(showSuccessMessage = false)
    }

    fun clearCache() {
        viewModelScope.launch {
            // TODO: Implement cache clearing logic
            _uiState.value = _uiState.value.copy(
                showSuccessMessage = true,
                successMessage = "Önbellek temizlendi"
            )
        }
    }

    fun exportData() {
        viewModelScope.launch {
            // TODO: Implement data export logic
            _uiState.value = _uiState.value.copy(
                showSuccessMessage = true,
                successMessage = "Veriler dışa aktarıldı"
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            // TODO: Implement logout logic
            _uiState.value = _uiState.value.copy(
                showSuccessMessage = true,
                successMessage = "Çıkış yapıldı"
            )
        }
    }
}

data class SettingsUiState(
    val isLoading: Boolean = true,
    val profileName: String = "",
    val profileBio: String = "",
    val profilePhone: String = "",
    val profileEmail: String = "",
    val showSuccessMessage: Boolean = false,
    val successMessage: String = "Ayarlar kaydedildi"
)
