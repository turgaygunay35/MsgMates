package com.msgmates.app.core.auth

/**
 * Token yenileme işlemlerini yapan ayrı servis
 * AuthRepository'den bağımsız, sadece refresh endpoint'ini çağırır
 */
interface TokenRefresher {
    suspend fun refreshToken(refreshToken: String): Result<Tokens>
}
