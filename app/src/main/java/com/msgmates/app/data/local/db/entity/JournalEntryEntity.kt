package com.msgmates.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.msgmates.app.data.local.db.converter.JournalMoodConverter
import com.msgmates.app.data.local.db.converter.ListConverter

@Entity(
    tableName = "journal_entries",
    indices = [
        Index(value = ["title"]),
        Index(value = ["createdAt"]),
        Index(value = ["isFavorite"]),
        Index(value = ["isArchived"])
    ]
)
@TypeConverters(ListConverter::class, JournalMoodConverter::class)
data class JournalEntryEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val mood: com.msgmates.app.domain.model.JournalMood? = null,
    val tags: List<String> = emptyList()
)
