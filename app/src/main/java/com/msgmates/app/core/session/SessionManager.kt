package com.msgmates.app.core.session

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class Tokens(val access: String = "", val refresh: String = "")

@Singleton
class SessionManager @Inject constructor(
    private val tokenRepository: com.msgmates.app.core.auth.TokenRepository
) {
    val isLoggedIn: Flow<Boolean> = tokenRepository.tokensFlow.map { tokens ->
        // Debug ve prod için tek kural: access non-blank
        tokens.access?.isNotBlank() == true
    }

    suspend fun save(tokens: Tokens) {
        tokenRepository.setTokens(tokens.access, tokens.refresh)
    }

    suspend fun clear() {
        tokenRepository.clear()
    }
}
