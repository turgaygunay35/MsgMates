package com.msgmates.app.ui.journal.archive

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
class JournalArchiveViewModel @Inject constructor(
    private val journalRepository: JournalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalArchiveUiState())
    val uiState: StateFlow<JournalArchiveUiState> = _uiState.asStateFlow()

    private val _selectedEntries = MutableStateFlow<Set<String>>(emptySet())
    val selectedEntries: StateFlow<Set<String>> = _selectedEntries.asStateFlow()

    private val _isMultiSelectMode = MutableStateFlow(false)
    val isMultiSelectMode: StateFlow<Boolean> = _isMultiSelectMode.asStateFlow()

    init {
        loadArchivedEntries()
    }

    private fun loadArchivedEntries() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                journalRepository.getArchivedEntries().collect { entries ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        entries = entries,
                        isEmpty = entries.isEmpty()
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun toggleMultiSelectMode() {
        _isMultiSelectMode.value = !_isMultiSelectMode.value
        if (!_isMultiSelectMode.value) {
            _selectedEntries.value = emptySet()
        }
    }

    fun toggleEntrySelection(entryId: String) {
        val currentSelection = _selectedEntries.value.toMutableSet()
        if (currentSelection.contains(entryId)) {
            currentSelection.remove(entryId)
        } else {
            currentSelection.add(entryId)
        }
        _selectedEntries.value = currentSelection
    }

    fun selectAllEntries() {
        _selectedEntries.value = _uiState.value.entries.map { it.id }.toSet()
    }

    fun clearSelection() {
        _selectedEntries.value = emptySet()
    }

    fun restoreSelectedEntries() {
        val selectedIds = _selectedEntries.value.toList()
        if (selectedIds.isNotEmpty()) {
            viewModelScope.launch {
                try {
                    journalRepository.restoreEntries(selectedIds)
                    _selectedEntries.value = emptySet()
                    _isMultiSelectMode.value = false
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Failed to restore entries"
                    )
                }
            }
        }
    }

    fun deleteSelectedEntries() {
        val selectedIds = _selectedEntries.value.toList()
        if (selectedIds.isNotEmpty()) {
            viewModelScope.launch {
                try {
                    journalRepository.deleteEntriesPermanently(selectedIds)
                    _selectedEntries.value = emptySet()
                    _isMultiSelectMode.value = false
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Failed to delete entries"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class JournalArchiveUiState(
    val isLoading: Boolean = false,
    val entries: List<JournalEntry> = emptyList(),
    val isEmpty: Boolean = false,
    val error: String? = null
)
