package com.msgmates.app.data.remote.auth

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    // OTP gönder (veya yeniden gönder)
    @POST("auth/request-code")
    suspend fun requestCode(@Body body: RequestCodeRequest): RequestCodeResponse

    // OTP doğrula
    @POST("auth/verify-code")
    suspend fun verifyCode(@Body body: VerifyCodeRequest): VerifyCodeResponse

    @POST("auth/refresh-token")
    suspend fun refreshToken(@Body body: RefreshTokenRequest): RefreshTokenResponse
}
