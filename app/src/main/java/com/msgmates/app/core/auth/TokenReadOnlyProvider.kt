package com.msgmates.app.core.auth

/**
 * Token'ları sadece okumak için dar arayüz
 * RefreshAuthenticator ve diğer bileşenler bu arayüzü kullanır
 * AuthRepository'ye bağımlılık yok
 */
interface TokenReadOnlyProvider {
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun hasValidTokens(): Boolean
}
