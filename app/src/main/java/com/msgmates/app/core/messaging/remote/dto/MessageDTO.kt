package com.msgmates.app.core.messaging.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class MessageDTO(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val type: String, // "text", "image", "video", "audio", "file", "location"
    val body: String?,
    val attachments: List<AttachmentDTO>? = null,
    val sentAt: Long,
    val edited: Boolean = false,
    val deleted: Boolean = false
)

@Serializable
data class AttachmentDTO(
    val id: String,
    val kind: String, // "image", "video", "audio", "file", "location"
    val mime: String,
    val size: Long?,
    val width: Int?,
    val height: Int?,
    val durationMs: Long?, // for video/audio
    val remoteUrl: String?
)
