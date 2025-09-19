package com.msgmates.app.core.auth.remote

import retrofit2.http.Body
import retrofit2.http.POST

data class RefreshRequest(
    val refreshToken: String,
    val deviceId: String?
)

data class TokenResponse(
    val success: Boolean,
    val access_token: String
)

data class RevokeRequest(
    val refreshToken: String,
    val deviceId: String?
)

data class DeviceRegisterRequest(
    val deviceId: String,
    val pushToken: String?,
    val appVersion: String
)

interface AuthApi {
    // Only keep essential endpoints - OTP flow uses AuthApiService
    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): TokenResponse

    @POST("auth/revoke")
    suspend fun revoke(@Body body: RevokeRequest)

    @POST("device/register")
    suspend fun registerDevice(@Body body: DeviceRegisterRequest)
}
