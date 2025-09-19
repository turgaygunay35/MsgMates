package com.msgmates.app.data.contacts.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ContactWithPhones(
    @Embedded
    val contact: ContactEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "contactId"
    )
    val phones: List<PhoneEntity>
)
