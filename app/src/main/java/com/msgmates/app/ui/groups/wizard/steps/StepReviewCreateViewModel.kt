package com.msgmates.app.ui.groups.wizard.steps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msgmates.app.data.groups.GroupsRepository
import com.msgmates.app.domain.groups.CreateGroupRequest
import com.msgmates.app.domain.groups.User
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class StepReviewCreateViewModel @Inject constructor(
    private val groupsRepository: GroupsRepository
) : ViewModel() {

    private val _groupName = MutableStateFlow("")
    val groupName: StateFlow<String> = _groupName.asStateFlow()

    private val _groupDescription = MutableStateFlow<String?>(null)
    val groupDescription: StateFlow<String?> = _groupDescription.asStateFlow()

    private val _avatarUri = MutableStateFlow<String?>(null)
    val avatarUri: StateFlow<String?> = _avatarUri.asStateFlow()

    private val _selectedUsers = MutableStateFlow<List<User>>(emptyList())
    val selectedUsers: StateFlow<List<User>> = _selectedUsers.asStateFlow()

    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating.asStateFlow()

    private val _creationResult = MutableStateFlow<CreationResult?>(null)
    val creationResult: StateFlow<CreationResult?> = _creationResult.asStateFlow()

    fun setGroupData(
        name: String,
        description: String?,
        avatarUri: String?,
        users: List<User>
    ) {
        _groupName.value = name
        _groupDescription.value = description
        _avatarUri.value = avatarUri
        _selectedUsers.value = users
    }

    fun createGroup() {
        viewModelScope.launch {
            _isCreating.value = true
            _creationResult.value = null

            try {
                val request = CreateGroupRequest(
                    name = _groupName.value,
                    description = _groupDescription.value,
                    avatarUri = _avatarUri.value,
                    memberIds = _selectedUsers.value.map { it.id }
                )

                val groupId = groupsRepository.createGroup(request)
                _creationResult.value = CreationResult.Success(groupId)
            } catch (e: Exception) {
                _creationResult.value = CreationResult.Error(e.message ?: "Grup oluşturulamadı")
            } finally {
                _isCreating.value = false
            }
        }
    }

    fun getMemberCount(): Int = _selectedUsers.value.size
}

sealed class CreationResult {
    data class Success(val groupId: String) : CreationResult()
    data class Error(val message: String) : CreationResult()
}
