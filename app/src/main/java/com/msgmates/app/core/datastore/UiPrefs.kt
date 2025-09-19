package com.msgmates.app.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ui_prefs")

@Singleton
class UiPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private val PREF_CHAT_FILTER = stringPreferencesKey("chat_filter")
        private val PREF_CHAT_FILTER_ORDER = stringPreferencesKey("chat_filter_order")

        const val DEFAULT_FILTER = "all"
        const val DEFAULT_FILTER_ORDER = "all,unread,groups,favorites,archived,disaster"
    }

    val selectedFilter: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PREF_CHAT_FILTER] ?: DEFAULT_FILTER
    }

    val filterOrder: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PREF_CHAT_FILTER_ORDER] ?: DEFAULT_FILTER_ORDER
    }

    suspend fun setSelectedFilter(filter: String) {
        context.dataStore.edit { preferences ->
            preferences[PREF_CHAT_FILTER] = filter
        }
    }

    suspend fun setFilterOrder(order: String) {
        context.dataStore.edit { preferences ->
            preferences[PREF_CHAT_FILTER_ORDER] = order
        }
    }

    suspend fun setFilterOrder(orderList: List<String>) {
        setFilterOrder(orderList.joinToString(","))
    }
}
