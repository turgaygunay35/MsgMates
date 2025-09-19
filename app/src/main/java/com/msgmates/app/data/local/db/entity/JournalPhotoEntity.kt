package com.msgmates.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_photos")
data class JournalPhotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val entryId: String,
    val uri: String,
    val order: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
