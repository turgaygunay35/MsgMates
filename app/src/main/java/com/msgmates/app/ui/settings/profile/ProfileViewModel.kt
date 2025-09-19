package com.msgmates.app.ui.settings.profile

import android.net.Uri
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
class ProfileViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            settingsRepository.getProfileName().collect { name ->
                _uiState.value = _uiState.value.copy(username = name)
            }
        }

        viewModelScope.launch {
            settingsRepository.getProfileBio().collect { bio ->
                _uiState.value = _uiState.value.copy(statusMessage = bio)
            }
        }

        viewModelScope.launch {
            settingsRepository.getProfilePhone().collect { phone ->
                _uiState.value = _uiState.value.copy(phone = phone)
            }
        }
    }

    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
        validateUsername(username)
    }

    fun updateStatusMessage(statusMessage: String) {
        _uiState.value = _uiState.value.copy(statusMessage = statusMessage)
        validateStatusMessage(statusMessage)
    }

    fun updateProfilePhoto(uri: Uri) {
        _uiState.value = _uiState.value.copy(profilePhotoUri = uri)
    }

    fun deleteProfilePhoto() {
        _uiState.value = _uiState.value.copy(profilePhotoUri = null)
    }

    private fun validateUsername(username: String) {
        val error = when {
            username.isBlank() -> "Kullanıcı adı boş olamaz"
            username.length < 3 -> "Kullanıcı adı en az 3 karakter olmalı"
            username.length > 20 -> "Kullanıcı adı en fazla 20 karakter olabilir"
            !username.matches(Regex("^[a-zA-Z0-9_.-]+$")) -> "Kullanıcı adı sadece harf, rakam, nokta, tire ve alt çizgi içerebilir"
            else -> null
        }

        _uiState.value = _uiState.value.copy(usernameError = error)
    }

    private fun validateStatusMessage(statusMessage: String) {
        val error = when {
            statusMessage.length > 150 -> "Durum mesajı en fazla 150 karakter olabilir"
            else -> null
        }

        _uiState.value = _uiState.value.copy(statusMessageError = error)
    }

    fun saveProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val state = _uiState.value

                // Validate before saving
                validateUsername(state.username)
                validateStatusMessage(state.statusMessage)

                if (state.usernameError == null && state.statusMessageError == null) {
                    settingsRepository.setProfileName(state.username)
                    settingsRepository.setProfileBio(state.statusMessage)

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showSuccessMessage = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Profil kaydedilemedi"
                )
            }
        }
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(showSuccessMessage = false)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class ProfileUiState(
    val isLoading: Boolean = false,
    val username: String = "",
    val statusMessage: String = "",
    val phone: String = "",
    val profilePhotoUri: Uri? = null,
    val usernameError: String? = null,
    val statusMessageError: String? = null,
    val showSuccessMessage: Boolean = false,
    val error: String? = null
)
