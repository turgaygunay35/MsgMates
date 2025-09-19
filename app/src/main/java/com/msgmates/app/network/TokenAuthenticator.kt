package com.msgmates.app.network

import com.msgmates.app.data.local.prefs.UserPrefsDataStore
import com.msgmates.app.data.remote.model.request.RefreshTokenRequest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val userPrefs: UserPrefsDataStore,
    private val apiService: ApiService
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.code == 401) {
            // Token expired, try to refresh
            val refreshToken = runBlocking { userPrefs.getRefreshToken().firstOrNull() }

            if (refreshToken != null) {
                try {
                    val refreshRequest = RefreshTokenRequest(refreshToken)
                    val refreshResponse = runBlocking { apiService.refreshToken(refreshRequest) }

                    if (refreshResponse.isSuccessful) {
                        val authResponse = refreshResponse.body()
                        if (authResponse != null) {
                            runBlocking {
                                userPrefs.saveUserToken(authResponse.accessToken)
                                userPrefs.saveRefreshToken(authResponse.refreshToken)
                            }

                            // Retry the original request with new token
                            return response.request.newBuilder()
                                .header("Authorization", "Bearer ${authResponse.accessToken}")
                                .build()
                        }
                    }
                } catch (e: Exception) {
                    // Refresh failed, clear tokens
                    runBlocking {
                        userPrefs.clearTokens()
                    }
                }
            }
        }

        return null
    }
}
