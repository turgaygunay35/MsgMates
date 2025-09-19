package com.msgmates.app.ui.notes

import androidx.lifecycle.ViewModel

class NotesViewModel : ViewModel() {
    private val repo = NotesRepository()
    val notes = repo.notes

    fun addNote(text: String) = repo.add(text)
    fun delete(note: Note) = repo.delete(note)
}
