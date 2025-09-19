package com.msgmates.app.core.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class MessageEntityWithAttachments(
    @Embedded
    val message: MessageEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "msgId"
    )
    val attachments: List<AttachmentEntity> = emptyList()
)
