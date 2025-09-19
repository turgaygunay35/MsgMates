package com.msgmates.app.ui.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.msgmates.app.core.connectivity.ConnectivityRepository
import com.msgmates.app.core.datastore.UiPrefs
import com.msgmates.app.core.disaster.DisasterModeRepository
import com.msgmates.app.core.notifications.MessageNotificationManager
import com.msgmates.app.data.chats.ChatsRepository
import com.msgmates.app.domain.chats.ChatFilter
import com.msgmates.app.domain.chats.Conversation
import com.msgmates.app.ui.common.ConnectionStatus
import com.msgmates.app.ui.common.StatusCapsuleMapper
import com.msgmates.app.ui.common.StatusCapsuleUi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Chats screen managing conversation list, filters, search,
 * and connection status with disaster mode support.
 */
@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val connectivityRepository: ConnectivityRepository,
    private val uiPrefs: UiPrefs,
    private val messageNotificationManager: MessageNotificationManager,
    private val disasterModeRepository: DisasterModeRepository,
    private val chatsRepository: ChatsRepository
) : ViewModel() {

    private val _isSearchMode = MutableStateFlow(false)
    val isSearchMode: StateFlow<Boolean> = _isSearchMode.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filtre yönetimi
    val selectedFilter: StateFlow<String> = uiPrefs.selectedFilter.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = UiPrefs.DEFAULT_FILTER
    )

    val filterOrder: StateFlow<List<String>> = uiPrefs.filterOrder.map { orderString ->
        orderString.split(",").filter { it.isNotBlank() }
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = UiPrefs.DEFAULT_FILTER_ORDER.split(",")
    )

    // Gizli filtreler (bellekte tutulur, DataStore'a yazılmaz)
    private val _hiddenFilters = MutableStateFlow(setOf<String>())
    val hiddenFilters: StateFlow<Set<String>> = _hiddenFilters.asStateFlow()

    // Görünür filtreler (order - hidden)
    val visibleFilters: StateFlow<List<String>> = combine(filterOrder, hiddenFilters) { order, hidden ->
        order.filter { !hidden.contains(it) }
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = UiPrefs.DEFAULT_FILTER_ORDER.split(",")
    )

    val connectionUi: StateFlow<StatusCapsuleUi> = combine(
        connectivityRepository.status,
        disasterModeRepository.isEnabled
    ) { status, isDisasterMode ->
        // Afet modu açıksa öncelikle DISASTER durumunu göster
        if (isDisasterMode) {
            StatusCapsuleMapper.mapStatusToUi(ConnectionStatus.DISASTER)
        } else {
            StatusCapsuleMapper.mapStatusToUi(status)
        }
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = StatusCapsuleMapper.mapStatusToUi(ConnectionStatus.LIVE)
    )

    // Okunmamış mesaj sayısı - gerçek hesaplama
    val unreadCount: StateFlow<Int> = selectedFilter.flatMapLatest { filter ->
        val chatFilter = when (filter) {
            "all" -> ChatFilter.ALL
            "unread" -> ChatFilter.UNREAD
            "groups" -> ChatFilter.GROUPS
            "favorites" -> ChatFilter.FAVORITES
            "archived" -> ChatFilter.ARCHIVED
            "disaster" -> ChatFilter.DISASTER
            else -> ChatFilter.ALL
        }
        chatsRepository.getUnreadCount(chatFilter)
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // Afet modu durumu
    val isDisasterMode: StateFlow<Boolean> = disasterModeRepository.isEnabled

    // Conversations state
    private val _conversationsState = MutableStateFlow<ConversationsState>(ConversationsState.Loading)
    val conversationsState: StateFlow<ConversationsState> = _conversationsState.asStateFlow()

    // Paged conversations based on selected filter
    val pagedConversations: Flow<PagingData<Conversation>> = selectedFilter.flatMapLatest { filter ->
        val chatFilter = when (filter) {
            "all" -> ChatFilter.ALL
            "unread" -> ChatFilter.UNREAD
            "groups" -> ChatFilter.GROUPS
            "favorites" -> ChatFilter.FAVORITES
            "archived" -> ChatFilter.ARCHIVED
            "disaster" -> ChatFilter.DISASTER
            else -> ChatFilter.ALL
        }
        chatsRepository.pagedConversations(chatFilter)
    }.cachedIn(viewModelScope)

    fun enterSearch() {
        _isSearchMode.value = true
    }

    fun exitSearch() {
        _isSearchMode.value = false
        _searchQuery.value = ""
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun onStatusCapsuleClick() {
        viewModelScope.launch {
            when (connectionUi.value.status) {
                ConnectionStatus.OFFLINE -> {
                    connectivityRepository.retryConnect()
                }
                ConnectionStatus.DISASTER -> {
                    // TODO: Show disaster panel
                }
                else -> {
                    // Other statuses - could show info toast
                }
            }
        }
    }

    // Filtre yönetimi metodları
    fun selectFilter(filter: String) {
        viewModelScope.launch {
            uiPrefs.setSelectedFilter(filter)
        }
    }

    fun reorderFilters(newOrder: List<String>) {
        viewModelScope.launch {
            uiPrefs.setFilterOrder(newOrder)
        }
    }

    fun toggleFilterVisibility(filter: String) {
        val currentHidden = _hiddenFilters.value
        if (currentHidden.contains(filter)) {
            _hiddenFilters.value = currentHidden - filter
        } else {
            _hiddenFilters.value = currentHidden + filter
        }
    }

    fun moveFilterUp(filter: String) {
        val currentOrder = filterOrder.value.toMutableList()
        val currentIndex = currentOrder.indexOf(filter)
        if (currentIndex > 0) {
            currentOrder.removeAt(currentIndex)
            currentOrder.add(currentIndex - 1, filter)
            reorderFilters(currentOrder)
        }
    }

    fun moveFilterDown(filter: String) {
        val currentOrder = filterOrder.value.toMutableList()
        val currentIndex = currentOrder.indexOf(filter)
        if (currentIndex < currentOrder.size - 1) {
            currentOrder.removeAt(currentIndex)
            currentOrder.add(currentIndex + 1, filter)
            reorderFilters(currentOrder)
        }
    }

    // Conversation actions
    fun archiveConversation(conversation: Conversation) {
        viewModelScope.launch {
            try {
                chatsRepository.archive(conversation.id)
                // TODO: Show snackbar with undo option
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun muteConversation(conversation: Conversation) {
        viewModelScope.launch {
            try {
                chatsRepository.mute(conversation.id, !conversation.isMuted)
                // TODO: Show snackbar with undo option
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteConversation(conversation: Conversation) {
        viewModelScope.launch {
            try {
                chatsRepository.delete(conversation.id)
                // TODO: Show snackbar with undo option
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun searchConversations(query: String) {
        // TODO: Implement search functionality
        // For now, just log the query
        android.util.Log.d("ChatsViewModel", "Search query: $query")
    }

    fun retryLoadConversations() {
        _conversationsState.value = ConversationsState.Loading
        // The pagedConversations flow will automatically retry
    }

    // Bulk operations
    fun deleteConversations(conversations: List<Conversation>) {
        viewModelScope.launch {
            try {
                conversations.forEach { conversation ->
                    chatsRepository.delete(conversation.id)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun archiveConversations(conversations: List<Conversation>) {
        viewModelScope.launch {
            try {
                conversations.forEach { conversation ->
                    chatsRepository.archive(conversation.id)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun muteConversations(conversations: List<Conversation>) {
        viewModelScope.launch {
            try {
                conversations.forEach { conversation ->
                    chatsRepository.mute(conversation.id, !conversation.isMuted)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

/**
 * State for conversations list
 */
sealed class ConversationsState {
    object Loading : ConversationsState()
    object Empty : ConversationsState()
    data class Error(val throwable: Throwable) : ConversationsState()
}
