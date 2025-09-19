package com.msgmates.app.core.network

import java.io.IOException
import retrofit2.HttpException

fun Throwable.toNetworkError(): NetworkError = when (this) {
    is HttpException -> when (code()) {
        400 -> NetworkError.BadRequest
        401 -> NetworkError.Unauthorized
        404 -> NetworkError.NotFound
        429 -> NetworkError.TooManyRequests
        in 500..599 -> NetworkError.ServerError
        else -> NetworkError.Unknown(message ?: "Bilinmeyen hata (HTTP ${code()})")
    }
    is IOException -> NetworkError.Unknown("Ağ bağlantı hatası: ${message ?: "IO"}")
    else -> NetworkError.Unknown(message ?: "Bilinmeyen hata")
}
