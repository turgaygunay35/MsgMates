package com.msgmates.app.core.messaging.model

import com.msgmates.app.core.db.entity.AttachmentEntity
import com.msgmates.app.core.db.entity.MessageEntity

data class UiMessage(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val body: String?,
    val msgType: String,
    val sentAt: Long?,
    val localCreatedAt: Long,
    val status: String,
    val replyToId: String?,
    val edited: Boolean,
    val deleted: Boolean,
    val attachments: List<AttachmentEntity> = emptyList()
) {
    companion object {
        fun fromEntity(entity: MessageEntity, attachments: List<AttachmentEntity> = emptyList()): UiMessage {
            return UiMessage(
                id = entity.id,
                conversationId = entity.convoId,
                senderId = entity.senderId,
                body = entity.body,
                msgType = entity.msgType,
                sentAt = entity.sentAt,
                localCreatedAt = entity.localCreatedAt,
                status = entity.status,
                replyToId = entity.replyToId,
                edited = entity.edited,
                deleted = entity.deleted,
                attachments = attachments
            )
        }
    }
}
