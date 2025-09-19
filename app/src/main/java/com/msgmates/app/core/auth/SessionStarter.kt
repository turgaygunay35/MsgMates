package com.msgmates.app.core.auth

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionStarter @Inject constructor(
    private val tokenRepo: TokenRepository,
    private val refresh: RefreshCoordinator
) {

    /**
     * Uygulama açılışında sessiz token yenileme
     * @return true if session is fresh or refresh successful, false if refresh failed
     */
    suspend fun ensureFreshSession(): Boolean {
        val tokens = tokenRepo.getTokens()
        val hasAccess = tokens.access != null

        return if (hasAccess) {
            // Token exists, try to refresh if needed
            try {
                refresh.blockingRefresh()
            } catch (e: Exception) {
                false
            }
        } else {
            false
        }
    }
}
