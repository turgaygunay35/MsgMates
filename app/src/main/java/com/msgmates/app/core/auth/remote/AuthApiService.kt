package com.msgmates.app.core.auth.remote

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Auth API servisi - telefon doğrulama endpoint'leri
 */
interface AuthApiService {
    
    @POST("auth/request-code")
    suspend fun requestCode(@Body request: RequestCodeRequest): RequestCodeResponse
    
    @POST("auth/verify-code")
    suspend fun verifyCode(@Body request: VerifyCodeRequest): VerifyCodeResponse
}

/**
 * Kod isteme request'i
 */
data class RequestCodeRequest(
    val phone: String
)

/**
 * Kod isteme response'u
 */
data class RequestCodeResponse(
    val success: Boolean,
    val message: String? = null
)

/**
 * Kod doğrulama request'i
 */
data class VerifyCodeRequest(
    val phone: String,
    val code: String
)

/**
 * Kod doğrulama response'u
 */
data class VerifyCodeResponse(
    val success: Boolean,
    val message: String? = null,
    val tokens: AuthTokensResponse? = null
)

/**
 * Token response'u
 */
data class AuthTokensResponse(
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiry: Long,
    val refreshTokenExpiry: Long
)
