package com.msgmates.app.ui.groups.wizard.steps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msgmates.app.data.groups.GroupsRepository
import com.msgmates.app.domain.groups.User
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class StepSelectMembersViewModel @Inject constructor(
    private val groupsRepository: GroupsRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedUserIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedUserIds: StateFlow<Set<String>> = _selectedUserIds.asStateFlow()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedCount = MutableStateFlow(0)
    val selectedCount: StateFlow<Int> = _selectedCount.asStateFlow()

    private val _canProceed = MutableStateFlow(false)
    val canProceed: StateFlow<Boolean> = _canProceed.asStateFlow()

    init {
        loadUsers()
        observeSelectionChanges()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            groupsRepository.getAvailableUsers().collect { userList ->
                _users.value = userList
                _isLoading.value = false
            }
        }
    }

    private fun observeSelectionChanges() {
        viewModelScope.launch {
            selectedUserIds.collect { selectedIds ->
                _selectedCount.value = selectedIds.size
                _canProceed.value = selectedIds.isNotEmpty()
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        searchUsers(query)
    }

    private fun searchUsers(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            groupsRepository.searchUsers(query).collect { userList ->
                _users.value = userList
                _isLoading.value = false
            }
        }
    }

    fun toggleUserSelection(user: User) {
        val currentSelection = _selectedUserIds.value.toMutableSet()
        if (currentSelection.contains(user.id)) {
            currentSelection.remove(user.id)
        } else {
            currentSelection.add(user.id)
        }
        _selectedUserIds.value = currentSelection
    }

    fun isUserSelected(user: User): Boolean {
        return _selectedUserIds.value.contains(user.id)
    }

    fun getSelectedUsers(): List<User> {
        val selectedIds = _selectedUserIds.value
        return _users.value.filter { selectedIds.contains(it.id) }
    }

    fun clearSelection() {
        _selectedUserIds.value = emptySet()
    }
}
