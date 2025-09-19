package com.msgmates.app.core.disaster.mesh

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class MeshRepository @Inject constructor() {

    private val _messages = MutableStateFlow<List<MeshMessage>>(emptyList())
    val messages: StateFlow<List<MeshMessage>> = _messages.asStateFlow()

    fun saveMessage(message: MeshMessage) {
        val currentMessages = _messages.value.toMutableList()
        currentMessages.add(message)
        _messages.value = currentMessages.sortedByDescending { it.timestamp }

        android.util.Log.d("MeshRepository", "Saved message: ${message.content}")
    }

    fun getMessages(): List<MeshMessage> = _messages.value

    fun getMessagesByType(type: MessageType): List<MeshMessage> = _messages.value.filter { it.type == type }

    fun getRecentMessages(limit: Int = 50): List<MeshMessage> = _messages.value.take(limit)

    fun clearMessages() {
        _messages.value = emptyList()
        android.util.Log.d("MeshRepository", "Cleared all messages")
    }
}
