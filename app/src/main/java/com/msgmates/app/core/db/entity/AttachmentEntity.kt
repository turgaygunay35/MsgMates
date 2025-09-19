package com.msgmates.app.core.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attachments",
    foreignKeys = [
        ForeignKey(
            entity = MessageEntity::class,
            parentColumns = ["id"],
            childColumns = ["msgId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["msgId"])]
)
data class AttachmentEntity(
    @PrimaryKey
    val id: String,
    val msgId: String, // Foreign key to MessageEntity
    val kind: String, // "image", "video", "audio", "file", "location"
    val mime: String,
    val size: Long?,
    val width: Int?,
    val height: Int?,
    val durationMs: Long?, // for video/audio
    val remoteUrl: String?,
    val localUri: String?,
    val thumbB64: String? // Base64 encoded thumbnail
)
