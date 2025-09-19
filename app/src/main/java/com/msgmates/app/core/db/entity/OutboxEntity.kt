package com.msgmates.app.core.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "outbox")
data class OutboxEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0,
    val msgId: String,
    val attempt: Int = 0,
    val lastError: String? = null
)
