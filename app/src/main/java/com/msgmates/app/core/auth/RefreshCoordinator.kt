package com.msgmates.app.core.auth

import com.msgmates.app.data.repository.auth.AuthRepository
import kotlin.system.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RefreshCoordinator(
    private val tokenRepo: TokenRepository,
    private val authApi: AuthRepository
) {
    private val mutex = Mutex()

    @Volatile private var lastRefreshEpochMs: Long = 0L

    /**
     * Blocks other callers while a single refresh happens.
     * Returns true if refresh succeeded, false otherwise.
     */
    fun blockingRefresh(): Boolean = runBlocking {
        mutex.withLock {
            val now = System.currentTimeMillis()
            // Debounce: eğer az önce yenilendiyse tekrar deneme
            if (now - lastRefreshEpochMs < 500) return@withLock true

            val tokens = tokenRepo.getTokens()
            val refresh = tokens.refresh ?: return@withLock false

            return@withLock try {
                val result = authApi.refresh(refresh)
                result.fold(
                    onSuccess = { resp ->
                        val newRefresh = resp.refreshToken ?: tokens.refresh // Keep existing refresh token if not provided
                        tokenRepo.setTokens(resp.accessToken, newRefresh)
                        lastRefreshEpochMs = System.currentTimeMillis()
                        true
                    },
                    onFailure = { t ->
                        // refresh başarısız → tokenları temizleyebiliriz (Bölüm 3'te logout akışı ile bağlayacağız)
                        false
                    }
                )
            } catch (t: Throwable) {
                // refresh başarısız → tokenları temizleyebiliriz (Bölüm 3'te logout akışı ile bağlayacağız)
                false
            }
        }
    }
}
