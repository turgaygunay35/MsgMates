package com.msgmates.app.ui.qn

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.qnDataStore by preferencesDataStore(name = "quick_notes_store")

class QuickNotesRepository(private val context: Context) {

    companion object {
        private val QUICK_NOTES = stringSetPreferencesKey("quick_notes")
    }

    val notesFlow: Flow<List<String>> =
        context.qnDataStore.data.map { prefs -> (prefs[QUICK_NOTES]?.toList() ?: emptyList()) }

    suspend fun add(note: String) {
        context.qnDataStore.edit { prefs ->
            val set = prefs[QUICK_NOTES]?.toMutableSet() ?: mutableSetOf()
            set.add(note)
            prefs[QUICK_NOTES] = set
        }
    }

    suspend fun delete(index: Int) {
        context.qnDataStore.edit { prefs ->
            val current = prefs[QUICK_NOTES]?.toMutableList() ?: mutableListOf()
            if (index in current.indices) {
                current.removeAt(index)
                prefs[QUICK_NOTES] = current.toMutableSet()
            }
        }
    }

    suspend fun replaceAll(list: List<String>) {
        context.qnDataStore.edit { prefs -> prefs[QUICK_NOTES] = list.toSet() }
    }
}
