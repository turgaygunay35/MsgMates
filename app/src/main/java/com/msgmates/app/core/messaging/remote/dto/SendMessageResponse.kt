package com.msgmates.app.core.messaging.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SendMessageResponse(
    val serverMsgId: String,
    val sentAt: Long,
    val deliveryState: String // "sent", "delivered", "read"
)
