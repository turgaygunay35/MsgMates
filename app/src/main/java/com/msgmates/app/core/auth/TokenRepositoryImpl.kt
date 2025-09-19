package com.msgmates.app.core.auth

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Singleton
class TokenRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
) : TokenRepository {

    private val prefs = context.getSharedPreferences("auth_tokens", Context.MODE_PRIVATE)

    private val _tokensFlow = MutableStateFlow(
        Tokens(
            access = prefs.getString(KEY_ACCESS, null),
            refresh = prefs.getString(KEY_REFRESH, null)
        )
    )
    override val tokensFlow: StateFlow<Tokens> = _tokensFlow

    override fun getTokensSync(): Tokens = _tokensFlow.value

    override suspend fun getTokens(): Tokens = _tokensFlow.value

    override fun setTokens(access: String?, refresh: String?) {
        prefs.edit {
            if (access != null) putString(KEY_ACCESS, access) else remove(KEY_ACCESS)
            if (refresh != null) putString(KEY_REFRESH, refresh) else remove(KEY_REFRESH)
        }
        _tokensFlow.value = Tokens(access, refresh)
    }

    override suspend fun updateTokens(access: String?, refresh: String?): Tokens {
        setTokens(access, refresh)
        return _tokensFlow.value
    }

    override fun clear() {
        prefs.edit { clear() }
        _tokensFlow.value = Tokens(null, null)
    }

    // TokenReadOnlyProvider implementation
    override fun getAccessToken(): String? = getTokensSync().access
    override fun getRefreshToken(): String? = getTokensSync().refresh
    override fun hasValidTokens(): Boolean {
        val tokens = getTokensSync()
        return !tokens.access.isNullOrBlank() && !tokens.refresh.isNullOrBlank()
    }

    private companion object {
        const val KEY_ACCESS = "access_token"
        const val KEY_REFRESH = "refresh_token"
    }
}
