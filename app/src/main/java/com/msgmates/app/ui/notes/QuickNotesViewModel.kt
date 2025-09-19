package com.msgmates.app.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msgmates.app.data.repository.QuickNotesRepository
import com.msgmates.app.domain.model.QuickNote
import kotlinx.coroutines.launch

class QuickNotesViewModel(
    private val repo: QuickNotesRepository
) : ViewModel() {

    fun saveNote(note: QuickNote) {
        viewModelScope.launch {
            repo.add(note)
        }
    }

    fun newNoteId(): Long = System.currentTimeMillis()
}
