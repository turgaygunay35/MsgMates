package com.msgmates.app.ui.auth.phone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msgmates.app.core.auth.AuthRepository
import com.msgmates.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * PhoneEntryFragment ViewModel
 * Telefon numarası girişi ve doğrulama kodu isteme işlemleri
 */
@HiltViewModel
class PhoneEntryViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PhoneEntryUiState())
    val uiState: StateFlow<PhoneEntryUiState> = _uiState.asStateFlow()

    /**
     * Telefon numarası değiştiğinde çağrılır
     */
    fun onPhoneChanged(phone: String) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            phone = phone,
            isPhoneValid = isValidTurkishPhone(phone),
            errorMessage = null
        )
        updateSendButtonState()
    }

    /**
     * Kullanıcı koşulları onayı değiştiğinde çağrılır
     */
    fun onTermsAccepted(accepted: Boolean) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            isTermsAccepted = accepted,
            errorMessage = null
        )
        updateSendButtonState()
    }

    /**
     * Doğrulama kodu gönderme işlemi
     */
    fun sendCode() {
        val currentState = _uiState.value
        val phone = currentState.phone

        if (!isValidTurkishPhone(phone)) {
            _uiState.value = currentState.copy(
                errorMessage = "error_invalid_tr_phone"
            )
            return
        }

        if (!currentState.isTermsAccepted) {
            _uiState.value = currentState.copy(
                errorMessage = "error_terms_required"
            )
            return
        }

        _uiState.value = currentState.copy(
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch {
            when (val result = authRepository.requestCode(phone)) {
                is Result.Success -> {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        isCodeSent = true
                    )
                }
                is Result.Error -> {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = "error_network"
                    )
                }
                is Result.Loading -> {
                    // Loading state already set above
                }
            }
        }
    }

    /**
     * Hata mesajını temizle
     */
    fun clearError() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(errorMessage = null)
    }

    /**
     * Türk GSM numarası validasyonu
     * Format: 05XXXXXXXXX (11 hane)
     */
    private fun isValidTurkishPhone(phone: String): Boolean {
        val regex = "^05\\d{9}$".toRegex()
        return regex.matches(phone)
    }

    /**
     * Gönder butonunun aktif/pasif durumunu güncelle
     */
    private fun updateSendButtonState() {
        val currentState = _uiState.value
        val isEnabled = currentState.isPhoneValid && 
                       currentState.isTermsAccepted && 
                       !currentState.isLoading
        _uiState.value = currentState.copy(isSendButtonEnabled = isEnabled)
    }
}

/**
 * PhoneEntry UI State
 */
data class PhoneEntryUiState(
    val phone: String = "",
    val isPhoneValid: Boolean = false,
    val isTermsAccepted: Boolean = false,
    val isSendButtonEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val isCodeSent: Boolean = false,
    val errorMessage: String? = null
)