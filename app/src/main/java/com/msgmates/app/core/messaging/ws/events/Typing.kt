package com.msgmates.app.core.messaging.ws.events

import kotlinx.serialization.Serializable

@Serializable
data class Typing(
    val conversationId: String,
    val userId: String,
    val isTyping: Boolean
)
