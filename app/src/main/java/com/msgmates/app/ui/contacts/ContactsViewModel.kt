package com.msgmates.app.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msgmates.app.analytics.ContactsTelemetryService
import com.msgmates.app.analytics.FilterType
import com.msgmates.app.data.contacts.ContactsRepository
import com.msgmates.app.data.contacts.preferences.ContactsPreferences
import com.msgmates.app.domain.contacts.model.Contact
import com.msgmates.app.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactsRepository: ContactsRepository,
    private val contactsPreferences: ContactsPreferences,
    private val telemetryService: ContactsTelemetryService
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _favoritesFilter = MutableStateFlow(false)
    private val _msgMatesFilter = MutableStateFlow(false)
    private val _isRefreshing = MutableStateFlow(false)

    private val _uiState = MutableStateFlow<ContactsUiState>(ContactsUiState.Loading)
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

    init {
        observeContacts()
    }

    private fun observeContacts() {
        viewModelScope.launch {
            combine(
                searchQuery,
                _favoritesFilter,
                _msgMatesFilter
            ) { query, favoritesOnly, msgMatesOnly ->
                Triple(query, favoritesOnly, msgMatesOnly)
            }.collect { (query, favoritesOnly, msgMatesOnly) ->
                loadContacts(query, favoritesOnly, msgMatesOnly)
            }
        }
    }

    fun searchContacts(query: String) {
        _searchQuery.value = query
    }

    fun setFavoritesFilter(enabled: Boolean) {
        _favoritesFilter.value = enabled
        if (enabled) {
            viewModelScope.launch {
                telemetryService.recordFilterUsage(FilterType.FAVORITES)
            }
        }
    }

    fun setMsgMatesFilter(enabled: Boolean) {
        _msgMatesFilter.value = enabled
        if (enabled) {
            viewModelScope.launch {
                telemetryService.recordFilterUsage(FilterType.MSGMATES)
            }
        }
    }

    fun loadContacts() {
        loadContacts(_searchQuery.value, _favoritesFilter.value, _msgMatesFilter.value)
    }

    private fun loadContacts(query: String, favoritesOnly: Boolean, msgMatesOnly: Boolean) {
        viewModelScope.launch {
            try {
                _uiState.value = ContactsUiState.Loading

                val contactsFlow = when {
                    query.isNotBlank() -> contactsRepository.searchContacts(query)
                    favoritesOnly && msgMatesOnly -> contactsRepository.getFilteredContacts(true, true)
                    favoritesOnly -> contactsRepository.getFavoriteContacts()
                    msgMatesOnly -> contactsRepository.getMsgMatesContacts()
                    else -> contactsRepository.getAllContacts()
                }

                contactsFlow.collect { contacts ->
                    _uiState.value = if (contacts.isEmpty()) {
                        ContactsUiState.Empty(
                            message = when {
                                query.isNotBlank() -> "Arama sonucu bulunamadı"
                                favoritesOnly -> "Favori kişi bulunamadı"
                                msgMatesOnly -> "MsgMates kullanıcısı bulunamadı"
                                else -> "Rehber boş"
                            }
                        )
                    } else {
                        ContactsUiState.Success(contacts)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ContactsUiState.Error("Kişiler yüklenirken hata oluştu: ${e.message}")
            }
        }
    }

    fun refreshContacts() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                // Record pull-to-refresh usage
                telemetryService.recordPullToRefresh()

                when (val result = contactsRepository.refreshContacts(force = true)) {
                    is Result.Success -> {
                        loadContacts()
                    }
                    is Result.Error -> {
                        _uiState.value = ContactsUiState.Error("Yenileme hatası: ${result.exception.message}")
                    }
                    is Result.Loading -> {
                        // Loading state is already handled by _isRefreshing
                    }
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    suspend fun getLastSyncTime(): Long? {
        return contactsRepository.getLastSyncTime()
    }

    fun toggleFavorite(contactId: Long) {
        viewModelScope.launch {
            when (val result = contactsRepository.toggleFavorite(contactId)) {
                is Result.Success -> {
                    // UI will be updated through the flow
                }
                is Result.Error -> {
                    _uiState.value = ContactsUiState.Error("Favori durumu değiştirilemedi: ${result.exception.message}")
                }
                is Result.Loading -> {
                    // Loading state is handled by the repository
                }
            }
        }
    }

    fun setPermissionRequired() {
        _uiState.value = ContactsUiState.PermissionRequired
    }
}

sealed class ContactsUiState {
    object Loading : ContactsUiState()
    object PermissionRequired : ContactsUiState()
    data class Empty(val message: String) : ContactsUiState()
    data class Error(val message: String) : ContactsUiState()
    data class Success(val contacts: List<Contact>) : ContactsUiState()

    val isRefreshing: Boolean
        get() = when (this) {
            is Loading -> true
            else -> false
        }
}
