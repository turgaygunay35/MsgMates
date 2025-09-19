package com.msgmates.app.core.presence

import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Singleton
class PresenceRepository @Inject constructor() {

    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun typingFlow(conversationId: String): Flow<Boolean> = flow {
        while (true) {
            // Simulate typing status changes
            val isTyping = Random().nextBoolean()
            emit(isTyping)
            delay(500) // 500ms debounce
        }
    }

    fun lastSeenFlow(conversationId: String): Flow<String> = flow {
        while (true) {
            // Simulate last seen updates
            val lastSeen = when (Random().nextInt(4)) {
                0 -> "çevrimiçi"
                1 -> "son görülme ${dateFormat.format(Date())}"
                2 -> "grupta 12 üye"
                3 -> "çevrimiçi • yazıyor…"
                else -> "çevrimiçi"
            }
            emit(lastSeen)
            delay(3000) // Update every 3 seconds
        }
    }

    fun getPresenceStatus(conversationId: String): Flow<String> = flow {
        while (true) {
            val status = when (Random().nextInt(3)) {
                0 -> "çevrimiçi • yazıyor…"
                1 -> "son görülme ${dateFormat.format(Date())}"
                2 -> "grupta 12 üye"
                else -> "çevrimiçi"
            }
            emit(status)
            delay(2000) // Update every 2 seconds
        }
    }
}
