package com.msgmates.app.data.repository.auth

import com.msgmates.app.data.remote.auth.RefreshTokenResponse
import com.msgmates.app.data.remote.auth.RequestCodeResponse
import com.msgmates.app.data.remote.auth.VerifyCodeResponse

interface AuthRepository {
    suspend fun requestCode(phoneNumber: String): Result<RequestCodeResponse>
    suspend fun verifyCode(phoneNumber: String, code: String): Result<VerifyCodeResponse>
    suspend fun refresh(refreshToken: String): Result<RefreshTokenResponse>
}
