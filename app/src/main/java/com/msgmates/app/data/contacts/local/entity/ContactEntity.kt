package com.msgmates.app.data.contacts.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contacts",
    indices = [
        Index(value = ["displayName"]),
        Index(value = ["isMsgMatesUser"]),
        Index(value = ["favorite"]),
        Index(value = ["lastSeenEpoch"]),
        Index(value = ["normalizedPrimary"])
    ]
)
data class ContactEntity(
    @PrimaryKey
    val id: Long, // Android contactId
    val displayName: String,
    val photoUri: String? = null,
    val isMsgMatesUser: Boolean = false,
    val favorite: Boolean = false,
    val lastSeenEpoch: Long? = null,
    val presenceOnline: Boolean? = null,
    val normalizedPrimary: String? = null, // E.164 format
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncAt: Long? = null // Last successful sync timestamp
)
