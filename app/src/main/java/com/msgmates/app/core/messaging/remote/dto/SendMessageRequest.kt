package com.msgmates.app.core.messaging.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SendMessageRequest(
    val conversationId: String,
    val clientMsgId: String,
    val type: String, // "text", "image", "video", "audio", "file", "location"
    val body: String?,
    val attachments: List<AttachmentStub>? = null
)

@Serializable
data class AttachmentStub(
    val kind: String, // "image", "video", "audio", "file", "location"
    val mime: String,
    val size: Long?,
    val remoteId: String?,
    val fileName: String?
)
