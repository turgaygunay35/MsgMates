package com.msgmates.app.core.messaging.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SyncRequest(
    val since: Long, // Unix timestamp
    val conversationId: String? = null // null for all conversations
)
