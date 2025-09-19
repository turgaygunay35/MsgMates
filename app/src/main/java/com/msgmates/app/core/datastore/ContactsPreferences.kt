package com.msgmates.app.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Context'e extension
private val Context.contactsDataStore by preferencesDataStore(name = "contacts_prefs")

@Singleton
class ContactsPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val KEY_HIGHLIGHT = booleanPreferencesKey("highlight_msgmates")

    // Oku
    val highlightFlow: Flow<Boolean> = context.contactsDataStore.data
        .map { prefs -> prefs[KEY_HIGHLIGHT] ?: false }

    // Kaydet
    suspend fun setHighlight(enabled: Boolean) {
        context.contactsDataStore.edit { prefs ->
            prefs[KEY_HIGHLIGHT] = enabled
        }
    }
}
