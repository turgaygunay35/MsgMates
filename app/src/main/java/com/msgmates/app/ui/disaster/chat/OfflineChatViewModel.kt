package com.msgmates.app.ui.disaster.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msgmates.app.core.disaster.mesh.MeshMessage
import com.msgmates.app.core.disaster.mesh.MeshRepository
import com.msgmates.app.core.disaster.mesh.MeshService
import com.msgmates.app.core.disaster.mesh.MessageType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class OfflineChatViewModel @Inject constructor(
    private val meshRepository: MeshRepository,
    private val meshService: MeshService
) : ViewModel() {

    private val _messages = MutableStateFlow<List<MeshMessage>>(emptyList())
    val messages: StateFlow<List<MeshMessage>> = _messages.asStateFlow()

    init {
        loadMessages()
        observeMeshMessages()
    }

    private fun loadMessages() {
        _messages.value = meshRepository.getRecentMessages(100)
    }

    private fun observeMeshMessages() {
        viewModelScope.launch {
            meshRepository.messages.collect { allMessages ->
                _messages.value = allMessages.take(100)
            }
        }
    }

    fun sendMessage(content: String) {
        viewModelScope.launch {
            meshService.broadcastMessage(content, MessageType.QUICK_MESSAGE)
        }
    }

    fun clearMessages() {
        viewModelScope.launch {
            meshRepository.clearMessages()
            _messages.value = emptyList()
        }
    }
}
