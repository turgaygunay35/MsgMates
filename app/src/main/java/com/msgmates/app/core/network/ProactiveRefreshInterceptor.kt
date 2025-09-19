package com.msgmates.app.core.network

import com.msgmates.app.core.auth.RefreshCoordinator
import com.msgmates.app.core.auth.TokenRepository
import okhttp3.Interceptor
import okhttp3.Response

class ProactiveRefreshInterceptor(
    private val tokenRepo: TokenRepository,
    private val coordinator: RefreshCoordinator
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val tokens = tokenRepo.getTokensSync()
        if (tokens.access != null) {
            // Basic proactive refresh - can be enhanced later
            try {
                coordinator.blockingRefresh()
            } catch (e: Exception) {
                // Ignore - 401 flow will handle it
            }
        }
        return chain.proceed(chain.request())
    }
}
