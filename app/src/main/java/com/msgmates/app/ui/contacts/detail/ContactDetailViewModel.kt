package com.msgmates.app.ui.contacts.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msgmates.app.data.contacts.ContactsRepository
import com.msgmates.app.domain.contacts.model.Contact
import com.msgmates.app.domain.contacts.usecase.ToggleFavoriteUseCase
import com.msgmates.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class ContactDetailViewModel @Inject constructor(
    private val contactsRepository: ContactsRepository,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val contactId: Long = savedStateHandle.get<Long>("contactId")
        ?: throw IllegalArgumentException("ContactId is required")

    private val _uiState = MutableStateFlow(ContactDetailUiState())
    val uiState: StateFlow<ContactDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ContactDetailEvent?>()
    val events: SharedFlow<ContactDetailEvent?> = _events.asSharedFlow()

    init {
        loadContact()
    }

    private fun loadContact() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            contactsRepository.getContactById(contactId).collect { contact ->
                if (contact != null) {
                    _uiState.value = _uiState.value.copy(
                        contact = contact,
                        isLoading = false,
                        error = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Contact not found"
                    )
                }
            }
        }
    }

    fun onToggleFavorite() {
        viewModelScope.launch {
            when (val result = toggleFavoriteUseCase(contactId)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        contact = _uiState.value.contact?.copy(favorite = result.data)
                    )
                    _events.emit(ContactDetailEvent.FavoriteToggled(result.data))
                }
                is Result.Error -> {
                    _events.emit(ContactDetailEvent.Error(result.exception.message ?: "Failed to toggle favorite"))
                }
                is Result.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }

    fun onMessageClick() {
        viewModelScope.launch {
            _events.emit(ContactDetailEvent.NavigateToChat(contactId))
        }
    }

    fun onCallClick(phoneNumber: String) {
        viewModelScope.launch {
            _events.emit(ContactDetailEvent.StartCall(phoneNumber))
        }
    }

    fun onVideoCallClick() {
        viewModelScope.launch {
            _events.emit(ContactDetailEvent.StartVideoCall(contactId))
        }
    }

    fun onInviteClick() {
        viewModelScope.launch {
            _events.emit(ContactDetailEvent.InviteToMsgMates(contactId))
        }
    }

    fun onOpenInSystem() {
        viewModelScope.launch {
            _events.emit(ContactDetailEvent.OpenInSystem(contactId))
        }
    }

    fun onDisasterModeClick() {
        viewModelScope.launch {
            _events.emit(ContactDetailEvent.NavigateToDisasterMode(contactId))
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class ContactDetailUiState(
    val contact: Contact? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class ContactDetailEvent {
    data class NavigateToChat(val contactId: Long) : ContactDetailEvent()
    data class StartCall(val phoneNumber: String) : ContactDetailEvent()
    data class StartVideoCall(val contactId: Long) : ContactDetailEvent()
    data class InviteToMsgMates(val contactId: Long) : ContactDetailEvent()
    data class OpenInSystem(val contactId: Long) : ContactDetailEvent()
    data class NavigateToDisasterMode(val contactId: Long) : ContactDetailEvent()
    data class FavoriteToggled(val isFavorite: Boolean) : ContactDetailEvent()
    data class Error(val message: String) : ContactDetailEvent()
}
