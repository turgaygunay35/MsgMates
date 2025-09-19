package com.msgmates.app.data.journal.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey
    val id: String,
    val userId: String,
    val userName: String,
    val profileImageUrl: String,
    val hasNewStory: Boolean,
    val type: JournalType,
    val contentUrl: String? = null, // foto/video/ses için
    val textContent: String? = null, // metin günlükleri için
    val createdAt: Long,
    val durationHours: Int = 24 // 6/12/24/48 varsayılan 24
)
