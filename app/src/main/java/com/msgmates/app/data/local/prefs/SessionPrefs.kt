package com.msgmates.app.data.local.prefs

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val SESSION_STORE = "session_prefs"
val Context.sessionDataStore by preferencesDataStore(name = SESSION_STORE)

class SessionPrefs(private val context: Context) {

    val phoneE164: Flow<String?> =
        context.sessionDataStore.data.map { prefs: Preferences ->
            prefs[DataStoreKeys.KEY_PHONE_E164]
        }

    val tosAccepted: Flow<Boolean> =
        context.sessionDataStore.data.map { prefs: Preferences ->
            prefs[DataStoreKeys.KEY_TOS_ACCEPTED] ?: false
        }

    val loggedIn: Flow<Boolean> =
        context.sessionDataStore.data.map { prefs: Preferences ->
            prefs[DataStoreKeys.KEY_LOGGED_IN] ?: false
        }

    suspend fun setPhoneE164(value: String) {
        context.sessionDataStore.edit { prefs ->
            prefs[DataStoreKeys.KEY_PHONE_E164] = value
        }
    }

    suspend fun setAcceptedTos(accepted: Boolean) {
        context.sessionDataStore.edit { prefs ->
            prefs[DataStoreKeys.KEY_TOS_ACCEPTED] = accepted
        }
    }

    suspend fun setLoggedIn(value: Boolean) {
        context.sessionDataStore.edit { prefs ->
            prefs[DataStoreKeys.KEY_LOGGED_IN] = value
        }
    }
}
