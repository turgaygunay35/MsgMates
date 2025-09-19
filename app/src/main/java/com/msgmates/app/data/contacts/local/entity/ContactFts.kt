package com.msgmates.app.data.contacts.local.entity

import androidx.room.Entity
import androidx.room.Fts4

@Entity(tableName = "contacts_fts")
@Fts4(contentEntity = ContactEntity::class)
data class ContactFts(
    val displayName: String,
    val normalizedPrimary: String?
)
