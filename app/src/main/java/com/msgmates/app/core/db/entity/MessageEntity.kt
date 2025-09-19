package com.msgmates.app.core.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val convoId: String,
    val senderId: String,
    val body: String?,
    val msgType: String, // "text", "image", "video", "audio", "file", "location"
    val sentAt: Long?, // null if not sent yet
    val localCreatedAt: Long,
    val status: String, // "pending", "sending", "sent", "delivered", "read", "failed"
    val replyToId: String?,
    val edited: Boolean = false,
    val deleted: Boolean = false
)
