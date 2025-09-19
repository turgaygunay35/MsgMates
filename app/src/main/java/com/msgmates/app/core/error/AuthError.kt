package com.msgmates.app.core.error

sealed class AuthError : Exception() {
    object ApiRouteNotFound : AuthError()
    object InvalidRequest : AuthError()
    object Unauthorized : AuthError()
    object NetworkUnavailable : AuthError()
    object UnknownApiError : AuthError()

    override val message: String
        get() = when (this) {
            is ApiRouteNotFound -> "Giriş servisi bulunamadı (geçici sorun). Lütfen tekrar deneyin."
            is InvalidRequest -> "Geçersiz istek. Lütfen bilgilerinizi kontrol edin."
            is Unauthorized -> "Yetkisiz erişim. Lütfen tekrar giriş yapın."
            is NetworkUnavailable -> "İnternet bağlantısı yok veya sunucuya ulaşılamıyor."
            is UnknownApiError -> "Beklenmeyen hata. Tekrar deneyin."
        }
}
