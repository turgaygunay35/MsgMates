package com.msgmates.app.ui.auth

// Ortak UI sözleşmeleri
sealed interface UiState
sealed interface UiEffect
sealed interface UiEvent

// --- PHONE ENTRY ---
data class PhoneState(
    val phone: String = "",
    val isValid: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
) : UiState

sealed class PhoneEvent : UiEvent {
    data class PhoneChanged(val value: String) : PhoneEvent()
    data object Submit : PhoneEvent()
}

sealed class PhoneEffect : UiEffect {
    data class NavigateToOtp(val phoneNumber: String) : PhoneEffect()
    data class ShowError(val message: String) : PhoneEffect()
}

// --- OTP VERIFY ---
data class OtpState(
    val phone: String = "",
    val code: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val resendEnabled: Boolean = false,
    val secondsLeft: Int = 45
) : UiState

sealed class OtpEvent : UiEvent {
    data class CodeChanged(val value: String) : OtpEvent()
    data object Submit : OtpEvent()
    data object Resend : OtpEvent()
}

sealed class OtpEffect : UiEffect {
    data object NavigateToMain : OtpEffect()
    data class ShowError(val message: String) : OtpEffect()
}
