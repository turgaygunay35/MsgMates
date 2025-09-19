package com.msgmates.app.data.contacts.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "phones",
    foreignKeys = [
        ForeignKey(
            entity = ContactEntity::class,
            parentColumns = ["id"],
            childColumns = ["contactId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["contactId"]),
        Index(value = ["normalizedE164"])
    ]
)
data class PhoneEntity(
    @PrimaryKey
    val id: Long, // phoneId
    val contactId: Long, // FK to ContactEntity
    val rawNumber: String,
    val normalizedE164: String? = null,
    val type: String? = null, // HOME, MOBILE, WORK, etc.
    val label: String? = null
)
