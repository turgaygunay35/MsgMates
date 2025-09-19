package com.msgmates.app.data.local.prefs

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val USER_STORE = "user_prefs"
val Context.userDataStore by preferencesDataStore(name = USER_STORE)

class UserPrefsDataStore(private val context: Context) {

    companion object {
        val USER_TOKEN = stringPreferencesKey("user_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val USER_ID = stringPreferencesKey("user_id")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DISASTER_MODE = stringPreferencesKey("disaster_mode")
    }

    fun getUserToken(): Flow<String?> =
        context.userDataStore.data.map { prefs: Preferences ->
            prefs[USER_TOKEN]
        }

    suspend fun saveUserToken(token: String) {
        context.userDataStore.edit { prefs ->
            prefs[USER_TOKEN] = token
        }
    }

    fun getRefreshToken(): Flow<String?> =
        context.userDataStore.data.map { prefs: Preferences ->
            prefs[REFRESH_TOKEN]
        }

    suspend fun saveRefreshToken(token: String) {
        context.userDataStore.edit { prefs ->
            prefs[REFRESH_TOKEN] = token
        }
    }

    fun getUserId(): Flow<String?> =
        context.userDataStore.data.map { prefs: Preferences ->
            prefs[USER_ID]
        }

    suspend fun saveUserId(userId: String) {
        context.userDataStore.edit { prefs ->
            prefs[USER_ID] = userId
        }
    }

    fun getThemeMode(): Flow<String?> =
        context.userDataStore.data.map { prefs: Preferences ->
            prefs[THEME_MODE]
        }

    suspend fun saveThemeMode(mode: String) {
        context.userDataStore.edit { prefs ->
            prefs[THEME_MODE] = mode
        }
    }

    fun getDisasterMode(): Flow<Boolean> =
        context.userDataStore.data.map { prefs: Preferences ->
            prefs[DISASTER_MODE] == "true"
        }

    suspend fun saveDisasterMode(enabled: Boolean) {
        context.userDataStore.edit { prefs ->
            prefs[DISASTER_MODE] = enabled.toString()
        }
    }

    suspend fun clearTokens() {
        context.userDataStore.edit { prefs ->
            prefs.remove(USER_TOKEN)
            prefs.remove(REFRESH_TOKEN)
        }
    }

    suspend fun clearUserId() {
        context.userDataStore.edit { prefs ->
            prefs.remove(USER_ID)
        }
    }

    suspend fun clearAll() {
        context.userDataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
