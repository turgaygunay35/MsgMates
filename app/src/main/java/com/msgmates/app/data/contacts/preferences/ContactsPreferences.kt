package com.msgmates.app.data.contacts.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class ContactsPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        internal val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "contacts_preferences")

        // Preference keys
        private val HIGHLIGHT_MSGMATES_USERS = booleanPreferencesKey("highlight_msgmates_users")
        private val SYNC_PERIOD = stringPreferencesKey("sync_period")
        private val SYNC_ONLY_WIFI = booleanPreferencesKey("sync_only_wifi")
    }

    // Highlight MsgMates users preference
    val highlightMsgMatesUsers: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HIGHLIGHT_MSGMATES_USERS] ?: true // Default: true
    }

    suspend fun setHighlightMsgMatesUsers(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HIGHLIGHT_MSGMATES_USERS] = enabled
        }
    }

    // Sync period preference
    val syncPeriod: Flow<SyncPeriod> = context.dataStore.data.map { preferences ->
        val periodString = preferences[SYNC_PERIOD] ?: SyncPeriod.HOURLY.name
        try {
            SyncPeriod.valueOf(periodString)
        } catch (e: IllegalArgumentException) {
            SyncPeriod.HOURLY // Default: Hourly
        }
    }

    suspend fun setSyncPeriod(period: SyncPeriod) {
        context.dataStore.edit { preferences ->
            preferences[SYNC_PERIOD] = period.name
        }
    }

    // Sync only on WiFi preference
    val syncOnlyWifi: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SYNC_ONLY_WIFI] ?: false // Default: false
    }

    suspend fun setSyncOnlyWifi(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SYNC_ONLY_WIFI] = enabled
        }
    }

    // Get all preferences as a single flow
    val allPreferences: Flow<ContactsPreferencesData> = context.dataStore.data.map { preferences ->
        ContactsPreferencesData(
            highlightMsgMatesUsers = preferences[HIGHLIGHT_MSGMATES_USERS] ?: true,
            syncPeriod = try {
                SyncPeriod.valueOf(preferences[SYNC_PERIOD] ?: SyncPeriod.HOURLY.name)
            } catch (e: IllegalArgumentException) {
                SyncPeriod.HOURLY
            },
            syncOnlyWifi = preferences[SYNC_ONLY_WIFI] ?: false
        )
    }
}

enum class SyncPeriod {
    HOURLY, // Saatlik
    DAILY, // Günlük
    OFF // Kapalı
}

data class ContactsPreferencesData(
    val highlightMsgMatesUsers: Boolean,
    val syncPeriod: SyncPeriod,
    val syncOnlyWifi: Boolean
)
