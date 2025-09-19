package com.msgmates.app.core.messaging.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ReceiptDTO(
    val type: String, // "delivered", "read"
    val messageIds: List<String>,
    val at: Long // Unix timestamp
)
