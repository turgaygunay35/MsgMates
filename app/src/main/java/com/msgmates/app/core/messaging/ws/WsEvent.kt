package com.msgmates.app.core.messaging.ws

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class WsEvent(
    val type: String,
    val payload: JsonElement
)

// Call signaling events
@Serializable
data class CallOffer(
    val calleeId: String,
    val callId: String,
    val video: Boolean,
    val sdpOffer: String? = null
)

@Serializable
data class CallAnswer(
    val callId: String,
    val sdpAnswer: String? = null
)

@Serializable
data class CallCandidate(
    val callId: String,
    val candidate: String
)

@Serializable
data class CallEnd(
    val callId: String,
    val reason: String
)
