package com.msgmates.app.ui.contacts.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msgmates.app.data.contacts.ContactsRepository
import com.msgmates.app.domain.contacts.model.Contact
import com.msgmates.app.domain.contacts.usecase.RefreshContactsUseCase
import com.msgmates.app.domain.contacts.usecase.SearchContactsUseCase
import com.msgmates.app.domain.contacts.usecase.ToggleFavoriteUseCase
import com.msgmates.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class ContactsListViewModel @Inject constructor(
    private val contactsRepository: ContactsRepository,
    private val searchContactsUseCase: SearchContactsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val refreshContactsUseCase: RefreshContactsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactsListUiState())
    val uiState: StateFlow<ContactsListUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    private val _events = MutableSharedFlow<ContactsListEvent?>()
    val events: SharedFlow<ContactsListEvent?> = _events.asSharedFlow()

    init {
        observeContacts()
        // Otomatik refresh kaldırıldı - sadece kullanıcı isteyince yap
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeContacts() {
        viewModelScope.launch {
            combine(
                searchQuery,
                filterState
            ) { query, filter ->
                when {
                    query.isNotBlank() -> searchContactsUseCase(query)
                    filter.favoritesOnly -> contactsRepository.getFavoriteContacts()
                    filter.msgMatesOnly -> contactsRepository.getMsgMatesContacts()
                    else -> contactsRepository.getAllContacts()
                }
            }.flattenMerge().collect { contacts ->
                _uiState.value = _uiState.value.copy(
                    contacts = contacts,
                    isLoading = false,
                    error = null
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChanged(favoritesOnly: Boolean, msgMatesOnly: Boolean) {
        _filterState.value = FilterState(
            favoritesOnly = favoritesOnly,
            msgMatesOnly = msgMatesOnly
        )
    }

    fun onRefresh() {
        refreshContacts()
    }

    private fun refreshContacts() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            when (val result = refreshContactsUseCase()) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = result.exception.message ?: "Unknown error"
                    )
                }
                is Result.Loading -> {
                    // Already set to loading
                }
            }
        }
    }

    fun onToggleFavorite(contactId: Long) {
        viewModelScope.launch {
            when (val result = toggleFavoriteUseCase(contactId)) {
                is Result.Success -> {
                    _events.emit(ContactsListEvent.FavoriteToggled(contactId, result.data))
                }
                is Result.Error -> {
                    _events.emit(ContactsListEvent.Error(result.exception.message ?: "Failed to toggle favorite"))
                }
                is Result.Loading -> {
                    // Handle loading state if needed
                }
            }
        }
    }

    fun onContactClick(contactId: Long) {
        viewModelScope.launch {
            _events.emit(ContactsListEvent.NavigateToDetail(contactId))
        }
    }

    fun onMessageClick(contactId: Long) {
        viewModelScope.launch {
            _events.emit(ContactsListEvent.NavigateToChat(contactId))
        }
    }

    fun onCallClick(contactId: Long) {
        viewModelScope.launch {
            _events.emit(ContactsListEvent.StartCall(contactId))
        }
    }

    fun onVideoCallClick(contactId: Long) {
        viewModelScope.launch {
            _events.emit(ContactsListEvent.StartVideoCall(contactId))
        }
    }

    fun onInviteClick(contactId: Long) {
        viewModelScope.launch {
            _events.emit(ContactsListEvent.InviteToMsgMates(contactId))
        }
    }

    fun onDisasterModeClick(contactId: Long) {
        viewModelScope.launch {
            _events.emit(ContactsListEvent.NavigateToDisasterMode(contactId))
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun startContactsObserverIfPermitted() {
        contactsRepository.startObservingIfPermitted()
    }
}

data class ContactsListUiState(
    val contacts: List<Contact> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class FilterState(
    val favoritesOnly: Boolean = false,
    val msgMatesOnly: Boolean = false
)

sealed class ContactsListEvent {
    data class NavigateToDetail(val contactId: Long) : ContactsListEvent()
    data class NavigateToChat(val contactId: Long) : ContactsListEvent()
    data class StartCall(val contactId: Long) : ContactsListEvent()
    data class StartVideoCall(val contactId: Long) : ContactsListEvent()
    data class InviteToMsgMates(val contactId: Long) : ContactsListEvent()
    data class NavigateToDisasterMode(val contactId: Long) : ContactsListEvent()
    data class FavoriteToggled(val contactId: Long, val isFavorite: Boolean) : ContactsListEvent()
    data class Error(val message: String) : ContactsListEvent()
}
