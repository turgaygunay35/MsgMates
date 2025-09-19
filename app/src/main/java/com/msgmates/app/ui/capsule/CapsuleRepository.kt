package com.msgmates.app.ui.capsule

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.msgmates.app.data.local.prefs.DataStoreKeys
import com.msgmates.app.data.local.prefs.sessionDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class CapsuleRepository(private val context: Context) {

    fun flow(): Flow<List<String>> =
        context.sessionDataStore.data.map { prefs ->
            val raw = prefs[DataStoreKeys.KEY_CAPSULES].orEmpty()
            if (raw.isBlank()) emptyList() else raw.split("||").filter { it.isNotBlank() }
        }

    suspend fun upsert(item: String) {
        val current = flow().firstOrNull() ?: emptyList()
        val next = (current + item).distinct()
        context.sessionDataStore.edit { it[DataStoreKeys.KEY_CAPSULES] = next.joinToString("||") }
    }

    suspend fun delete(item: String) {
        val current = flow().firstOrNull() ?: emptyList()
        val next = current.filterNot { it == item }
        context.sessionDataStore.edit { it[DataStoreKeys.KEY_CAPSULES] = next.joinToString("||") }
    }
}
