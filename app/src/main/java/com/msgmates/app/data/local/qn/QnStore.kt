package com.msgmates.app.data.local.qn

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val QN_STORE = "qn_store"
val Context.qnDataStore by preferencesDataStore(name = QN_STORE)

object QnKeys {
    val KEY_QN = stringPreferencesKey("qn_items") // CSV/JSON tek alan
}

class QnStore(private val context: Context) {

    fun items(): Flow<String?> =
        context.qnDataStore.data.map { prefs: Preferences -> prefs[QnKeys.KEY_QN] }

    suspend fun save(raw: String) {
        context.qnDataStore.edit { prefs ->
            prefs[QnKeys.KEY_QN] = raw
        }
    }
}
