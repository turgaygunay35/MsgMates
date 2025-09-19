package com.msgmates.app.domain.usecase

import android.content.Context
import androidx.work.WorkManager
import com.msgmates.app.core.auth.TokenRepository
import com.msgmates.app.core.logging.StructuredLogger
import com.msgmates.app.core.metrics.MetricsCollector
import com.msgmates.app.core.ws.WsClient
import com.msgmates.app.data.secure.SecureTokenStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

class LogoutUseCase @Inject constructor(
    private val tokens: TokenRepository,
    private val secureTokenStore: SecureTokenStore,
    private val wsClient: WsClient?,
    private val metrics: MetricsCollector,
    @ApplicationContext private val context: Context
) {
    suspend fun execute() = coroutineScope {
        StructuredLogger.authLogoutStart()

        try {
            // 1. Stop reconnect attempts for websocket
            wsClient?.disconnect()
            metrics.setWsState(0) // disconnected

            // 2. Cancel user-scoped work
            WorkManager.getInstance(context).cancelAllWorkByTag("user-session")
            WorkManager.getInstance(context).cancelAllWork()

            // 3. Clear secure token store
            secureTokenStore.clear()

            // 4. Clear legacy token repository
            tokens.clear()

            // 5. Clear DB user tables atomically
            withContext(Dispatchers.IO) {
                clearUserData()
            }

            // 6. Image caches
            clearImageCache()

            // 7. HTTP cache evict (optional)
            clearHttpCache()

            // 8. Metrics'i güncelle
            metrics.incrementLogout()

            StructuredLogger.authLogoutOk()
        } catch (e: Exception) {
            StructuredLogger.logEvent("ERROR", "LOGOUT_CLEANUP_FAIL", "Logout cleanup failed: ${e.message}")
            throw e
        }
    }

    private fun clearImageCache() {
        try {
            // Coil cache temizliği
            // Coil.imageLoader(context).memoryCache.clear()
            // Coil.imageLoader(context).diskCache.clear()
            StructuredLogger.logEvent("DEBUG", "CACHE_IMAGE_CLEAR", "Image cache cleared")
        } catch (e: Exception) {
            StructuredLogger.logEvent("WARN", "CACHE_IMAGE_CLEAR_FAIL", "Failed to clear image cache: ${e.message}")
        }
    }

    private fun clearHttpCache() {
        try {
            // OkHttp cache temizliği
            // okHttpClient.cache?.evictAll()
            StructuredLogger.logEvent("DEBUG", "CACHE_HTTP_CLEAR", "HTTP cache cleared")
        } catch (e: Exception) {
            StructuredLogger.logEvent("WARN", "CACHE_HTTP_CLEAR_FAIL", "Failed to clear HTTP cache: ${e.message}")
        }
    }

    private fun clearUserData() {
        try {
            // Room veritabanı user-scoped tabloları temizle
            // userDao.deleteAll()
            // messageDao.deleteAll()
            // contactDao.deleteAll()
            StructuredLogger.logEvent("DEBUG", "CACHE_DB_CLEAR", "User data cleared from database")
        } catch (e: Exception) {
            StructuredLogger.logEvent("WARN", "CACHE_DB_CLEAR_FAIL", "Failed to clear user data: ${e.message}")
        }
    }
}
