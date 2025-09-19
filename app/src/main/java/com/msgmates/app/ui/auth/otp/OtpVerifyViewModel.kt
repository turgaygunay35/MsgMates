package com.msgmates.app.ui.auth.otp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msgmates.app.core.auth.AuthRepository
import com.msgmates.app.core.auth.TokenRepository
import com.msgmates.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * OtpVerifyFragment ViewModel
 * OTP doğrulama ve geri sayım işlemleri
 */
@HiltViewModel
class OtpVerifyViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenRepository: TokenRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OtpVerifyUiState())
    val uiState: StateFlow<OtpVerifyUiState> = _uiState.asStateFlow()

    private var countdownJob: kotlinx.coroutines.Job? = null
    private var failedAttempts = 0
    private val maxFailedAttempts = 5
    private val cooldownDuration = 30L // 30 saniye

    /**
     * Fragment başlatıldığında çağrılır
     */
    fun init(phone: String) {
        _uiState.value = _uiState.value.copy(
            phone = phone,
            countdownSeconds = 60
        )
        startCountdown()
    }

    /**
     * OTP kodu değiştiğinde çağrılır
     */
    fun onOtpChanged(otp: String) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            otp = otp,
            isOtpValid = isValidOtp(otp),
            errorMessage = null
        )
        updateVerifyButtonState()
    }

    /**
     * OTP doğrulama işlemi
     */
    fun verifyOtp() {
        val currentState = _uiState.value
        val phone = currentState.phone
        val otp = currentState.otp

        if (!isValidOtp(otp)) {
            _uiState.value = currentState.copy(
                errorMessage = "error_otp_invalid"
            )
            return
        }

        // Çok fazla başarısız deneme kontrolü
        if (failedAttempts >= maxFailedAttempts) {
            _uiState.value = currentState.copy(
                errorMessage = "error_otp_too_many_attempts"
            )
            return
        }

        _uiState.value = currentState.copy(
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch {
            when (val result = authRepository.verifyCode(phone, otp)) {
                is Result.Success -> {
                    // Token'ları kaydet
                    val tokens = result.data
                    tokenRepository.setTokens(
                        tokens.access?.value ?: "",
                        tokens.refresh?.value ?: ""
                    )
                    
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        isVerificationSuccess = true
                    )
                }
                is Result.Error -> {
                    failedAttempts++
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        errorMessage = "error_otp_invalid"
                    )
                }
                is Result.Loading -> {
                    // Loading state already set above
                }
            }
        }
    }

    /**
     * Kodu tekrar gönderme işlemi
     */
    fun resendCode() {
        val currentState = _uiState.value
        val phone = currentState.phone

        _uiState.value = currentState.copy(
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch {
            when (val result = authRepository.requestCode(phone)) {
                is Result.Success -> {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        countdownSeconds = 60
                    )
                    startCountdown()
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
     * OTP validasyonu - 6 haneli numerik
     */
    private fun isValidOtp(otp: String): Boolean {
        val regex = "^\\d{6}$".toRegex()
        return regex.matches(otp)
    }

    /**
     * Doğrulama butonunun aktif/pasif durumunu güncelle
     */
    private fun updateVerifyButtonState() {
        val currentState = _uiState.value
        val isEnabled = currentState.isOtpValid && 
                       !currentState.isLoading &&
                       failedAttempts < maxFailedAttempts
        _uiState.value = currentState.copy(isVerifyButtonEnabled = isEnabled)
    }

    /**
     * Geri sayım başlat
     */
    private fun startCountdown() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            var seconds = 60
            while (seconds > 0) {
                _uiState.value = _uiState.value.copy(
                    countdownSeconds = seconds,
                    canResend = false
                )
                delay(1000)
                seconds--
            }
            _uiState.value = _uiState.value.copy(
                countdownSeconds = 0,
                canResend = true
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}

/**
 * OtpVerify UI State
 */
data class OtpVerifyUiState(
    val phone: String = "",
    val otp: String = "",
    val isOtpValid: Boolean = false,
    val isVerifyButtonEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val isVerificationSuccess: Boolean = false,
    val countdownSeconds: Int = 0,
    val canResend: Boolean = false,
    val errorMessage: String? = null
)