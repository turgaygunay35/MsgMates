package com.msgmates.app.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msgmates.app.data.journal.JournalRepository
import com.msgmates.app.data.journal.model.JournalEntry
import com.msgmates.app.data.journal.model.WatchedUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class JournalViewModel @Inject constructor(
    val journalRepository: JournalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    val mutedUserIds: StateFlow<Set<String>> = journalRepository.getMutedUsers()
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(), emptySet())

    val watchedUsers: StateFlow<List<WatchedUser>> = journalRepository.watchedUsers
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(), emptyList())

    init {
        loadUserStories()
    }

    private fun loadUserStories() {
        viewModelScope.launch {
            journalRepository.userStories
                .combine(mutedUserIds) { stories, muted ->
                    // Ana ekranda sessize alınanları GİZLE
                    stories.filter { it.userId !in muted }
                }
                .combine(watchedUsers) { stories, watched ->
                    // Ana ekranda sadece İZLENMEMİŞ günlükler görünsün
                    val watchedUserIds = watched.map { it.userId }.toSet()
                    stories.filter { it.userId !in watchedUserIds }
                }
                .collect { filteredStories ->
                    _uiState.value = _uiState.value.copy(
                        stories = filteredStories,
                        isLoading = false
                    )
                }
        }
    }

    fun addStory(entry: JournalEntry) {
        viewModelScope.launch {
            journalRepository.addStory(entry)
        }
    }

    fun deleteStory(id: String) {
        viewModelScope.launch {
            journalRepository.deleteStory(id)
        }
    }

    fun refreshStories() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadUserStories()
    }

    fun toggleMute(userId: String) {
        if (journalRepository.isMuted(userId)) {
            journalRepository.unmuteUser(userId)
        } else {
            journalRepository.muteUser(userId)
        }
    }

    fun onStoryViewed(userId: String, userName: String, profileUrl: String) {
        journalRepository.onStoryViewed(userId, userName, profileUrl)
    }

    fun addStory(
        type: com.msgmates.app.data.journal.model.JournalType,
        contentUrl: String,
        textContent: String,
        durationHours: Int
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                journalRepository.addStory(
                    type = type,
                    contentUrl = contentUrl,
                    textContent = textContent,
                    durationHours = durationHours
                )
                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Hikaye eklenirken hata oluştu"
                )
            }
        }
    }
}

data class JournalUiState(
    val stories: List<JournalEntry> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
