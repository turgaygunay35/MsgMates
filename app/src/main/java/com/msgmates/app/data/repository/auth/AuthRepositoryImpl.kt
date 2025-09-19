package com.msgmates.app.data.repository.auth

import com.msgmates.app.core.network.toNetworkError
import com.msgmates.app.data.remote.auth.AuthApiService
import com.msgmates.app.data.remote.auth.RefreshTokenRequest
import com.msgmates.app.data.remote.auth.RefreshTokenResponse
import com.msgmates.app.data.remote.auth.RequestCodeRequest
import com.msgmates.app.data.remote.auth.RequestCodeResponse
import com.msgmates.app.data.remote.auth.VerifyCodeRequest
import com.msgmates.app.data.remote.auth.VerifyCodeResponse
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApiService
) : AuthRepository {

    override suspend fun requestCode(phoneNumber: String): Result<RequestCodeResponse> {
        return try {
            val resp = api.requestCode(RequestCodeRequest(phoneNumber))
            Result.success(resp)
        } catch (t: Throwable) {
            Result.failure(t.toNetworkError())
        }
    }

    override suspend fun verifyCode(phoneNumber: String, code: String): Result<VerifyCodeResponse> {
        return try {
            val resp = api.verifyCode(VerifyCodeRequest(phoneNumber, code))
            Result.success(resp)
        } catch (t: Throwable) {
            Result.failure(t.toNetworkError())
        }
    }

    override suspend fun refresh(refreshToken: String): Result<RefreshTokenResponse> =
        try {
            Result.success(api.refreshToken(RefreshTokenRequest(refreshToken)))
        } catch (t: Throwable) {
            Result.failure(t.toNetworkError())
        }
}
