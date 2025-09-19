package com.msgmates.app.core.network

import java.net.SocketTimeoutException
import java.net.UnknownHostException
import retrofit2.HttpException

object ErrorMapper {
    fun toUserMessage(t: Throwable): String {
        return when (t) {
            is UnknownHostException -> "İnternet bağlantısı yok."
            is SocketTimeoutException -> "Zaman aşımı. Lütfen tekrar deneyin."
            is HttpException -> when (t.code()) {
                400 -> "Geçersiz istek."
                401 -> "Oturumunuz geçersiz. Lütfen tekrar giriş yapın."
                403 -> "Yetkiniz yok."
                429 -> parseRetryAfter(t) ?: "Çok fazla deneme. Lütfen biraz sonra tekrar deneyin."
                in 500..599 -> "Sunucuda sorun var. Biraz sonra deneyin."
                else -> "Bir hata oluştu. (${t.code()})"
            }
            else -> "Bir hata oluştu."
        }
    }

    private fun parseRetryAfter(http: HttpException): String? {
        return http.response()?.headers()?.get("Retry-After")?.let { "Lütfen $it sn sonra tekrar deneyin." }
    }
}

fun mapAuthError(t: Throwable) = ErrorMapper.toUserMessage(t)
