package com.msgmates.app.core.network

import com.msgmates.app.core.auth.TokenReadOnlyProvider
import com.msgmates.app.core.auth.TokenRefresher
import com.msgmates.app.core.auth.TokenRepository
import com.msgmates.app.core.logging.StructuredLogger
import com.msgmates.app.core.metrics.MetricsCollector
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

@Singleton
class RefreshAuthenticator @Inject constructor(
    private val tokenProvider: TokenReadOnlyProvider,
    private val tokenRefresher: TokenRefresher,
    private val tokenRepository: TokenRepository,
    private val metrics: MetricsCollector
) : Authenticator {

    private val refreshMutex = Mutex()

    @Volatile private var isRefreshing = false

    override fun authenticate(route: Route?, response: Response): Request? {
        // MaxRetry = 1: Aynı isteği iki kez denediysek bırak
        if (responseCount(response) >= 2) {
            StructuredLogger.logEvent("WARN", "AUTH_REFRESH_MAX_RETRY", "Max retry reached, giving up")
            return null
        }

        val refreshToken = tokenProvider.getRefreshToken() ?: return null

        // Single-flight: Eğer başka bir thread refresh yapıyorsa bekle
        val startTime = System.currentTimeMillis()
        val newTokens = runBlocking {
            refreshMutex.withLock {
                if (isRefreshing) {
                    StructuredLogger.logEvent("DEBUG", "AUTH_REFRESH_WAIT", "Another refresh in progress, waiting...")
                    return@withLock null
                }

                isRefreshing = true
                try {
                    StructuredLogger.authRefreshStart()
                    metrics.incrementRefreshAttempts()

                    val result = tokenRefresher.refreshToken(refreshToken)
                    val latency = System.currentTimeMillis() - startTime

                    result.fold(
                        onSuccess = { tokens ->
                            StructuredLogger.authRefreshOk(latency)
                            metrics.incrementRefreshSuccess(latency)
                            // Token'ları güncelle
                            tokenRepository.setTokens(tokens.access, tokens.refresh)
                            tokens
                        },
                        onFailure = { error ->
                            StructuredLogger.authRefreshFail(error.message ?: "Unknown error", latency)
                            metrics.incrementRefreshFailure()
                            // Başarısız refresh: tokenları temizle ve logout tetikle
                            tokenRepository.clear()
                            null
                        }
                    )
                } finally {
                    isRefreshing = false
                }
            }
        }

        return newTokens?.let { tokens ->
            // Yeni access token ile isteği tekrar oluştur
            response.request.newBuilder()
                .header("Authorization", "Bearer ${tokens.access}")
                .build()
        }
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var r = response.priorResponse
        while (r != null) { result++; r = r.priorResponse }
        return result
    }
}
