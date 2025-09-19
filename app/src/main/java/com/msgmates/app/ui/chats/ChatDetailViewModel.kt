package com.msgmates.app.ui.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msgmates.app.core.messaging.MessageRepository
import com.msgmates.app.core.messaging.model.UiMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val receiptBatcher: com.msgmates.app.core.messaging.ReceiptBatcher
) : ViewModel() {

    private var conversationId: String? = null

    private val _messages = MutableStateFlow<List<UiMessage>>(emptyList())
    val messages: StateFlow<List<UiMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun initialize(conversationId: String) {
        this.conversationId = conversationId

        viewModelScope.launch {
            messageRepository.stream(conversationId)
                .stateIn(viewModelScope)
                .collect { messageList ->
                    _messages.value = messageList
                }
        }
    }

    fun sendText(text: String) {
        val convoId = conversationId ?: return

        if (text.trim().isEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = messageRepository.sendText(convoId, text.trim())

            if (result.isFailure) {
                _error.value = "Mesaj gönderilemedi: ${result.exceptionOrNull()?.message}"
            }

            _isLoading.value = false
        }
    }

    fun sendWithAttachments(text: String?, attachments: List<com.msgmates.app.core.messaging.model.LocalAttachment>) {
        val convoId = conversationId ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = messageRepository.sendWithAttachments(convoId, text, attachments)

            if (result.isFailure) {
                _error.value = "Mesaj gönderilemedi: ${result.exceptionOrNull()?.message}"
            }

            _isLoading.value = false
        }
    }

    fun ackDelivered(messageIds: List<String>) {
        // Use batcher to avoid UI blocking
        messageIds.forEach { messageId ->
            receiptBatcher.addDelivered(messageId)
        }
    }

    fun ackRead(messageIds: List<String>) {
        // Use batcher to avoid UI blocking
        messageIds.forEach { messageId ->
            receiptBatcher.addRead(messageId)
        }
    }

    fun sync() {
        val convoId = conversationId ?: return

        viewModelScope.launch {
            val lastMessage = _messages.value.lastOrNull()
            val since = lastMessage?.localCreatedAt ?: 0L

            messageRepository.sync(since, convoId)
        }
    }

    fun clearError() {
        _error.value = null
    }

    // Placeholder methods for existing ChatDetailFragment compatibility
    val title = MutableStateFlow("Chat")
    val subtitle = MutableStateFlow("Online")
    val callActive = MutableStateFlow<String?>(null)
    val callDuration = MutableStateFlow("")

    fun startFakeCall() {
        // Placeholder for call functionality
    }

    fun formatCallDuration(duration: String): String {
        return duration
    }
}
