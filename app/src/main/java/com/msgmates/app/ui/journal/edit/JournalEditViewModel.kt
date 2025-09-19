package com.msgmates.app.ui.journal.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msgmates.app.data.repository.JournalRepository
import com.msgmates.app.domain.model.JournalMood
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class JournalEditViewModel @Inject constructor(
    private val journalRepository: JournalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalEditUiState())
    val uiState: StateFlow<JournalEditUiState> = _uiState.asStateFlow()

    private val _events = MutableStateFlow<JournalEditEvent?>(null)
    val events: StateFlow<JournalEditEvent?> = _events.asStateFlow()

    fun loadEntry(entryId: String?) {
        if (entryId == null) {
            _uiState.value = JournalEditUiState()
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                journalRepository.getEntryById(entryId).collect { entry ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        entryId = entryId,
                        title = entry?.title ?: "",
                        content = entry?.content ?: "",
                        mood = entry?.mood,
                        tags = entry?.tags ?: emptyList(),
                        photoUris = emptyList(), // TODO: Load photos
                        isEditMode = entry != null
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

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
        validateForm()
    }

    fun updateContent(content: String) {
        _uiState.value = _uiState.value.copy(content = content)
        validateForm()
    }

    fun updateMood(mood: JournalMood?) {
        _uiState.value = _uiState.value.copy(mood = mood)
    }

    fun addTag(tag: String) {
        val trimmedTag = tag.trim()
        if (trimmedTag.isNotEmpty() && !_uiState.value.tags.contains(trimmedTag)) {
            val newTags = _uiState.value.tags + trimmedTag
            _uiState.value = _uiState.value.copy(tags = newTags)
        }
    }

    fun removeTag(tag: String) {
        val newTags = _uiState.value.tags.filter { it != tag }
        _uiState.value = _uiState.value.copy(tags = newTags)
    }

    fun addPhoto(uri: String) {
        val newPhotos = _uiState.value.photoUris + uri
        _uiState.value = _uiState.value.copy(photoUris = newPhotos)
    }

    fun removePhoto(uri: String) {
        val newPhotos = _uiState.value.photoUris.filter { it != uri }
        _uiState.value = _uiState.value.copy(photoUris = newPhotos)
    }

    fun saveEntry() {
        if (!_uiState.value.isFormValid) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)

            try {
                val currentState = _uiState.value

                if (currentState.isEditMode && currentState.entryId != null) {
                    journalRepository.updateEntry(
                        id = currentState.entryId,
                        title = currentState.title,
                        content = currentState.content,
                        mood = currentState.mood,
                        tags = currentState.tags,
                        photoUris = currentState.photoUris
                    )
                    _events.value = JournalEditEvent.EntryUpdated
                } else {
                    val entryId = journalRepository.createEntry(
                        title = currentState.title,
                        content = currentState.content,
                        mood = currentState.mood,
                        tags = currentState.tags,
                        photoUris = currentState.photoUris
                    )
                    _events.value = JournalEditEvent.EntryCreated(entryId)
                }

                _uiState.value = _uiState.value.copy(isSaving = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "Failed to save entry"
                )
            }
        }
    }

    private fun validateForm() {
        val currentState = _uiState.value
        val isTitleValid = currentState.title.trim().isNotEmpty()
        val isContentValid = currentState.content.trim().isNotEmpty()
        val isFormValid = isTitleValid && isContentValid

        _uiState.value = currentState.copy(
            isTitleValid = isTitleValid,
            isContentValid = isContentValid,
            isFormValid = isFormValid
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearEvent() {
        _events.value = null
    }
}

data class JournalEditUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val entryId: String? = null,
    val title: String = "",
    val content: String = "",
    val mood: JournalMood? = null,
    val tags: List<String> = emptyList(),
    val photoUris: List<String> = emptyList(),
    val isEditMode: Boolean = false,
    val isTitleValid: Boolean = false,
    val isContentValid: Boolean = false,
    val isFormValid: Boolean = false,
    val error: String? = null
)

sealed interface JournalEditEvent {
    data class EntryCreated(val entryId: String) : JournalEditEvent
    object EntryUpdated : JournalEditEvent
    data class Error(val message: String) : JournalEditEvent
}
