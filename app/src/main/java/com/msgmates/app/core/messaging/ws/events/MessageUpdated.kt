package com.msgmates.app.core.messaging.ws.events

import com.msgmates.app.core.messaging.remote.dto.MessageDTO
import kotlinx.serialization.Serializable

@Serializable
data class MessageUpdated(
    val message: MessageDTO
)
