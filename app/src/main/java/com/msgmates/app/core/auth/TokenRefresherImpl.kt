package com.msgmates.app.core.auth

import com.msgmates.app.data.remote.auth.AuthApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenRefresherImpl @Inject constructor(
    private val authApi: AuthApiService
) : TokenRefresher {

    override suspend fun refreshToken(refreshToken: String): Result<Tokens> {
        return try {
            val response = authApi.refreshToken(
                com.msgmates.app.data.remote.auth.RefreshTokenRequest(refreshToken)
            )
            val newTokens = Tokens(
                access = response.accessToken,
                refresh = response.refreshToken ?: refreshToken // Keep existing if not provided
            )
            Result.success(newTokens)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
