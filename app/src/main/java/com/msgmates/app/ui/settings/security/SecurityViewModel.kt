package com.msgmates.app.ui.settings.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msgmates.app.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SecurityUiState())
    val uiState: StateFlow<SecurityUiState> = _uiState.asStateFlow()

    init {
        loadSecuritySettings()
    }

    private fun loadSecuritySettings() {
        viewModelScope.launch {
            settingsRepository.getBiometricEnabled().collect { biometric ->
                _uiState.value = _uiState.value.copy(
                    biometricEnabled = biometric,
                    isLoading = false
                )
            }
        }

        viewModelScope.launch {
            settingsRepository.getAutoLockEnabled().collect { autoLock ->
                _uiState.value = _uiState.value.copy(autoLockEnabled = autoLock)
            }
        }

        viewModelScope.launch {
            settingsRepository.getScreenCaptureEnabled().collect { screenCapture ->
                _uiState.value = _uiState.value.copy(screenCaptureEnabled = screenCapture)
            }
        }

        viewModelScope.launch {
            settingsRepository.getAutoLockTimeout().collect { timeout ->
                _uiState.value = _uiState.value.copy(autoLockTimeout = timeout)
            }
        }
    }

    fun toggleBiometric() {
        viewModelScope.launch {
            val currentState = _uiState.value.biometricEnabled
            settingsRepository.setBiometricEnabled(!currentState)
            _uiState.value = _uiState.value.copy(
                biometricEnabled = !currentState,
                showSuccessMessage = true
            )
        }
    }

    fun toggleAutoLock() {
        viewModelScope.launch {
            val currentState = _uiState.value.autoLockEnabled
            settingsRepository.setAutoLockEnabled(!currentState)
            _uiState.value = _uiState.value.copy(
                autoLockEnabled = !currentState,
                showSuccessMessage = true
            )
        }
    }

    fun toggleScreenCapture() {
        viewModelScope.launch {
            val currentState = _uiState.value.screenCaptureEnabled
            settingsRepository.setScreenCaptureEnabled(!currentState)
            _uiState.value = _uiState.value.copy(
                screenCaptureEnabled = !currentState,
                showSuccessMessage = true
            )
        }
    }

    fun updateAutoLockTimeout(timeout: Int) {
        viewModelScope.launch {
            settingsRepository.setAutoLockTimeout(timeout)
            _uiState.value = _uiState.value.copy(
                autoLockTimeout = timeout,
                showSuccessMessage = true
            )
        }
    }

    fun openTwoFactorAuth() {
        // TODO: Navigate to two-factor authentication setup
    }

    fun openSessionManagement() {
        // TODO: Navigate to session management
    }

    fun openSecurityLog() {
        // TODO: Navigate to security log
    }

    fun openChangePassword() {
        // TODO: Navigate to change password
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(showSuccessMessage = false)
    }
}

data class SecurityUiState(
    val isLoading: Boolean = true,
    val biometricEnabled: Boolean = false,
    val autoLockEnabled: Boolean = false,
    val screenCaptureEnabled: Boolean = true,
    val autoLockTimeout: Int = 5,
    val showSuccessMessage: Boolean = false
)

data class SecurityItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val showSwitch: Boolean = false,
    val showArrow: Boolean = false
)
