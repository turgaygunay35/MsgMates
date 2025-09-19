package com.msgmates.app.ui.journal.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msgmates.app.data.repository.JournalRepository
import com.msgmates.app.domain.model.JournalEntry
import com.msgmates.app.domain.model.JournalMood
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@HiltViewModel
class JournalListViewModel @Inject constructor(
    private val journalRepository: JournalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalListUiState())
    val uiState: StateFlow<JournalListUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _selectedMood = MutableStateFlow<JournalMood?>(null)
    private val _sortOrder = MutableStateFlow(SortOrder.DATE_DESC)
    private val _showFavoritesOnly = MutableStateFlow(false)
    private val _showArchivedOnly = MutableStateFlow(false)

    private val _selectedEntries = MutableStateFlow<Set<String>>(emptySet())
    val selectedEntries: StateFlow<Set<String>> = _selectedEntries.asStateFlow()

    private val _isMultiSelectMode = MutableStateFlow(false)
    val isMultiSelectMode: StateFlow<Boolean> = _isMultiSelectMode.asStateFlow()

    init {
        observeEntries()
    }

    private fun observeEntries() {
        viewModelScope.launch {
            combine(
                _searchQuery,
                _selectedMood,
                _sortOrder,
                _showFavoritesOnly,
                _showArchivedOnly
            ) { query, mood, _, favoritesOnly, archivedOnly ->
                Triple(query, mood, if (favoritesOnly) "favorites" else if (archivedOnly) "archived" else "all")
            }.collect { (query, mood, filter) ->
                loadEntries(query, mood, filter)
            }
        }
    }

    private suspend fun loadEntries(query: String, mood: JournalMood?, filter: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)

        try {
            val entriesFlow = when {
                query.isNotEmpty() -> journalRepository.searchEntries(query)
                mood != null -> journalRepository.getEntriesByMood(mood)
                filter == "favorites" -> journalRepository.getFavoriteEntries()
                filter == "archived" -> journalRepository.getArchivedEntries()
                else -> journalRepository.getAllEntries()
            }

            entriesFlow.collect { entries ->
                val sortedEntries = when (_sortOrder.value) {
                    SortOrder.DATE_DESC -> entries.sortedByDescending { it.createdAt }
                    SortOrder.DATE_ASC -> entries.sortedBy { it.createdAt }
                    SortOrder.TITLE_ASC -> entries.sortedBy { it.title }
                    SortOrder.TITLE_DESC -> entries.sortedByDescending { it.title }
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    entries = sortedEntries,
                    isEmpty = sortedEntries.isEmpty()
                )
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = e.message ?: "Unknown error occurred"
            )
        }
    }

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun filterByMood(mood: JournalMood?) {
        _selectedMood.value = mood
    }

    fun setSortOrder(sortOrder: SortOrder) {
        _sortOrder.value = sortOrder
    }

    fun toggleFavoritesOnly() {
        _showFavoritesOnly.value = !_showFavoritesOnly.value
        if (_showFavoritesOnly.value) {
            _showArchivedOnly.value = false
        }
    }

    fun toggleArchivedOnly() {
        _showArchivedOnly.value = !_showArchivedOnly.value
        if (_showArchivedOnly.value) {
            _showFavoritesOnly.value = false
        }
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedMood.value = null
        _showFavoritesOnly.value = false
        _showArchivedOnly.value = false
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

    fun archiveSelectedEntries() {
        val selectedIds = _selectedEntries.value.toList()
        if (selectedIds.isNotEmpty()) {
            viewModelScope.launch {
                try {
                    journalRepository.archiveEntries(selectedIds)
                    _selectedEntries.value = emptySet()
                    _isMultiSelectMode.value = false
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "Failed to archive entries"
                    )
                }
            }
        }
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

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class JournalListUiState(
    val isLoading: Boolean = false,
    val entries: List<JournalEntry> = emptyList(),
    val isEmpty: Boolean = false,
    val error: String? = null
)

enum class SortOrder {
    DATE_DESC, DATE_ASC, TITLE_ASC, TITLE_DESC
}
