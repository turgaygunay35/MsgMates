package com.msgmates.app.core.network

import com.msgmates.app.core.auth.TokenRepository

class WsHeaders(private val tokenRepo: TokenRepository) {
    fun authHeader(): Pair<String, String>? {
        val token = runCatching { tokenRepo.getTokensSync().access }.getOrNull() ?: return null
        return "Authorization" to "Bearer $token"
    }
}
