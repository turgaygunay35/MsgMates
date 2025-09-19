package com.msgmates.app.core.messaging.ws.events

import com.msgmates.app.core.messaging.remote.dto.ReceiptDTO
import kotlinx.serialization.Serializable

@Serializable
data class Receipt(
    val receipt: ReceiptDTO
)
