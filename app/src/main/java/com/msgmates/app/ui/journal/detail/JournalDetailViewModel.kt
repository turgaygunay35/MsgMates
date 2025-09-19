package com.msgmates.app.ui.journal.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msgmates.app.data.repository.JournalRepository
import com.msgmates.app.domain.model.JournalEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class JournalDetailViewModel @Inject constructor(
    private val journalRepository: JournalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalDetailUiState())
    val uiState: StateFlow<JournalDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<JournalDetailEvent?>(null)
    val events: StateFlow<JournalDetailEvent?> = _events.asStateFlow()

    fun loadEntry(entryId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                journalRepository.getEntryById(entryId).collect { entry ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        entry = entry,
                        error = if (entry == null) "Entry not found" else null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load entry"
                )
            }
        }
    }

    fun toggleFavorite() {
        val entry = _uiState.value.entry ?: return

        viewModelScope.launch {
            try {
                journalRepository.toggleFavorite(entry.id)
                _events.value = JournalDetailEvent.FavoriteToggled
            } catch (e: Exception) {
                _events.value = JournalDetailEvent.Error(e.message ?: "Failed to toggle favorite")
            }
        }
    }

    fun archiveEntry() {
        val entry = _uiState.value.entry ?: return

        viewModelScope.launch {
            try {
                journalRepository.archiveEntry(entry.id)
                _events.value = JournalDetailEvent.EntryArchived
            } catch (e: Exception) {
                _events.value = JournalDetailEvent.Error(e.message ?: "Failed to archive entry")
            }
        }
    }

    fun restoreEntry() {
        val entry = _uiState.value.entry ?: return

        viewModelScope.launch {
            try {
                journalRepository.restoreEntry(entry.id)
                _events.value = JournalDetailEvent.EntryRestored
            } catch (e: Exception) {
                _events.value = JournalDetailEvent.Error(e.message ?: "Failed to restore entry")
            }
        }
    }

    fun deleteEntry() {
        val entry = _uiState.value.entry ?: return

        viewModelScope.launch {
            try {
                journalRepository.deleteEntry(entry.id)
                _events.value = JournalDetailEvent.EntryDeleted
            } catch (e: Exception) {
                _events.value = JournalDetailEvent.Error(e.message ?: "Failed to delete entry")
            }
        }
    }

    fun shareEntry() {
        val entry = _uiState.value.entry ?: return
        _events.value = JournalDetailEvent.ShareEntry(entry)
    }

    fun clearEvent() {
        _events.value = null
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class JournalDetailUiState(
    val isLoading: Boolean = false,
    val entry: JournalEntry? = null,
    val error: String? = null
)

sealed interface JournalDetailEvent {
    object FavoriteToggled : JournalDetailEvent
    object EntryArchived : JournalDetailEvent
    object EntryRestored : JournalDetailEvent
    object EntryDeleted : JournalDetailEvent
    data class ShareEntry(val entry: JournalEntry) : JournalDetailEvent
    data class Error(val message: String) : JournalDetailEvent
}
