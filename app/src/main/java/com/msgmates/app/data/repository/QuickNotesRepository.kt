package com.msgmates.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.msgmates.app.domain.model.QuickNote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val QUICK_NOTES_STORE = "quick_notes"
val Context.quickNotesDataStore by preferencesDataStore(name = QUICK_NOTES_STORE)

class QuickNotesRepository(private val context: Context) {

    companion object {
        val NOTES_KEY = stringPreferencesKey("notes")
    }

    val notes: Flow<List<QuickNote>> = context.quickNotesDataStore.data.map { prefs ->
        val raw = prefs[NOTES_KEY] ?: ""
        if (raw.isBlank()) {
            emptyList()
        } else {
            raw.split("||").mapNotNull { noteStr ->
                val parts = noteStr.split("::")
                if (parts.size >= 3) {
                    QuickNote(
                        id = parts[0],
                        title = parts[1],
                        body = parts[2],
                        createdAt = parts.getOrNull(3)?.toLongOrNull() ?: System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                } else {
                    null
                }
            }
        }
    }

    suspend fun add(note: QuickNote) {
        val current = notes.let { flow ->
            var result: List<QuickNote> = emptyList()
            flow.collect { result = it }
            result
        }
        val updated = current + note
        saveNotes(updated)
    }

    suspend fun addRaw(text: String) {
        val parts = text.split("::")
        val note = QuickNote(
            id = System.currentTimeMillis().toString(),
            title = parts.getOrNull(0) ?: "",
            body = parts.getOrNull(1) ?: "",
            createdAt = parts.getOrNull(2)?.toLongOrNull() ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        add(note)
    }

    suspend fun delete(noteId: String) {
        val current = notes.let { flow ->
            var result: List<QuickNote> = emptyList()
            flow.collect { result = it }
            result
        }
        val updated = current.filter { it.id != noteId }
        saveNotes(updated)
    }

    private suspend fun saveNotes(notes: List<QuickNote>) {
        val serialized = notes.joinToString("||") { note ->
            "${note.id}::${note.title}::${note.body}::${note.createdAt}"
        }
        context.quickNotesDataStore.edit { prefs ->
            prefs[NOTES_KEY] = serialized
        }
    }
}
