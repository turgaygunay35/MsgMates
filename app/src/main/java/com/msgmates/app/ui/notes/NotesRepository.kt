package com.msgmates.app.ui.notes

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class NotesRepository {
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes
    private var nextId = 1L

    fun add(text: String) {
        if (text.isBlank()) return
        _notes.value = _notes.value + Note(nextId++, text.trim())
    }

    fun delete(note: Note) {
        _notes.value = _notes.value.filterNot { it.id == note.id }
    }
}
