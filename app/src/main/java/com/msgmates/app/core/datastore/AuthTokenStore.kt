package com.msgmates.app.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val AUTH_TOKEN_STORE = "auth_token_store"
val Context.authTokenDataStore by preferencesDataStore(name = AUTH_TOKEN_STORE)

class AuthTokenStore(private val context: Context) {

    companion object {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val REFRESH_EXPIRES_AT = stringPreferencesKey("refresh_expires_at")
    }

    suspend fun saveTokens(access: String, refresh: String, refreshExp: String?) {
        context.authTokenDataStore.edit { prefs ->
            prefs[ACCESS_TOKEN] = access
            prefs[REFRESH_TOKEN] = refresh
            if (refreshExp != null) {
                prefs[REFRESH_EXPIRES_AT] = refreshExp
            }
        }
    }

    fun accessTokenFlow(): Flow<String?> =
        context.authTokenDataStore.data.map { prefs: Preferences ->
            prefs[ACCESS_TOKEN]
        }

    fun refreshTokenFlow(): Flow<String?> =
        context.authTokenDataStore.data.map { prefs: Preferences ->
            prefs[REFRESH_TOKEN]
        }

    fun refreshExpiresAtFlow(): Flow<String?> =
        context.authTokenDataStore.data.map { prefs: Preferences ->
            prefs[REFRESH_EXPIRES_AT]
        }

    fun isLoggedInFlow(): Flow<Boolean> =
        accessTokenFlow().map { it != null }

    suspend fun clearTokens() {
        context.authTokenDataStore.edit { prefs ->
            prefs.remove(ACCESS_TOKEN)
            prefs.remove(REFRESH_TOKEN)
            prefs.remove(REFRESH_EXPIRES_AT)
        }
    }
}
