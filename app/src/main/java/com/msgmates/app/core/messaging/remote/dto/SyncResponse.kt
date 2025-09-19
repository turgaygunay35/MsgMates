package com.msgmates.app.core.messaging.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SyncResponse(
    val messages: List<MessageDTO>,
    val receipts: List<ReceiptDTO>
)
