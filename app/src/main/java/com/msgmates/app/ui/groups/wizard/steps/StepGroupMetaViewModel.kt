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
class StepGroupMetaViewModel @Inject constructor(
    private val groupsRepository: GroupsRepository
) : ViewModel() {

    private val _groupName = MutableStateFlow("")
    val groupName: StateFlow<String> = _groupName.asStateFlow()

    private val _groupDescription = MutableStateFlow("")
    val groupDescription: StateFlow<String> = _groupDescription.asStateFlow()

    private val _avatarUri = MutableStateFlow<String?>(null)
    val avatarUri: StateFlow<String?> = _avatarUri.asStateFlow()

    private val _selectedUsers = MutableStateFlow<List<User>>(emptyList())
    val selectedUsers: StateFlow<List<User>> = _selectedUsers.asStateFlow()

    private val _isValid = MutableStateFlow(false)
    val isValid: StateFlow<Boolean> = _isValid.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        observeValidation()
    }

    fun setSelectedUsers(users: List<User>) {
        _selectedUsers.value = users
    }

    fun updateGroupName(name: String) {
        _groupName.value = name
    }

    fun updateGroupDescription(description: String) {
        _groupDescription.value = description
    }

    fun updateAvatarUri(uri: String?) {
        _avatarUri.value = uri
    }

    private fun observeValidation() {
        viewModelScope.launch {
            groupName.collect { name ->
                _isValid.value = name.trim().length >= 2
            }
        }
    }

    fun getGroupData(): GroupData {
        return GroupData(
            name = _groupName.value.trim(),
            description = _groupDescription.value.trim().takeIf { it.isNotEmpty() },
            avatarUri = _avatarUri.value,
            selectedUsers = _selectedUsers.value
        )
    }
}

data class GroupData(
    val name: String,
    val description: String?,
    val avatarUri: String?,
    val selectedUsers: List<User>
)
