package com.msgmates.app.core.network

sealed class NetworkError(val userMessage: String) : Throwable(userMessage) {
    object BadRequest : NetworkError("Geçersiz istek")
    object Unauthorized : NetworkError("Yetkisiz. Lütfen tekrar deneyiniz")
    object NotFound : NetworkError("Sunucu uç noktası bulunamadı (404)")
    object TooManyRequests : NetworkError("Çok fazla deneme. Lütfen biraz sonra tekrar deneyin")
    object ServerError : NetworkError("Sunucu hatası")
    data class Unknown(val causeMsg: String) : NetworkError(causeMsg)
}
