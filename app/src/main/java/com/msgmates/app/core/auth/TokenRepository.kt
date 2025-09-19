package com.msgmates.app.core.auth

import kotlinx.coroutines.flow.StateFlow

interface TokenRepository : TokenReadOnlyProvider {
    /** Anlık token durumu (UI veya ağ katmanı dinleyebilir) */
    val tokensFlow: StateFlow<Tokens>

    /** Senkron erişim (interceptor gibi yerlerde kullanışlı) */
    fun getTokensSync(): Tokens

    /** Suspend erişim (gerekirse) */
    suspend fun getTokens(): Tokens

    /** Yalnızca access/refresh'i ayarla */
    fun setTokens(access: String?, refresh: String?)

    /** Güncelle ve güncel Tokens döndür (suspend imza bekleyen yerler için) */
    suspend fun updateTokens(access: String?, refresh: String?): Tokens

    /** Tüm kimlik bilgisini temizle */
    fun clear()
}
