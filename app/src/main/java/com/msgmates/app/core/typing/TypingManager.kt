package com.msgmates.app.core.typing

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Singleton
class TypingManager @Inject constructor() {

    private var typingJob: Job? = null
    private var isTyping = false

    fun startTyping(
        scope: CoroutineScope,
        conversationId: String,
        onTypingChanged: (Boolean) -> Unit
    ) {
        if (isTyping) return

        isTyping = true
        onTypingChanged(true)

        // Cancel previous typing job
        typingJob?.cancel()

        // Start new typing job with 500ms debounce
        typingJob = scope.launch {
            delay(500)
            stopTyping(onTypingChanged)
        }

        Log.d("TypingManager", "Started typing in conversation: $conversationId")
    }

    fun stopTyping(onTypingChanged: (Boolean) -> Unit) {
        if (!isTyping) return

        isTyping = false
        onTypingChanged(false)

        typingJob?.cancel()
        typingJob = null

        Log.d("TypingManager", "Stopped typing")
    }

    fun isCurrentlyTyping(): Boolean = isTyping
}
