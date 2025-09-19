package com.msgmates.app.domain.contacts.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Contact(
    val id: Long,
    val displayName: String,
    val photoUri: String? = null,
    val isMsgMatesUser: Boolean = false,
    val favorite: Boolean = false,
    val lastSeenEpoch: Long? = null,
    val presenceOnline: Boolean? = null,
    val normalizedPrimary: String? = null,
    val phones: List<Phone> = emptyList()
) : Parcelable

@Parcelize
data class Phone(
    val id: Long,
    val contactId: Long,
    val rawNumber: String,
    val normalizedE164: String? = null,
    val type: String? = null,
    val label: String? = null
) : Parcelable

data class ContactUiModel(
    val contact: Contact,
    val lastSeenText: String? = null,
    val isOnline: Boolean = false,
    val hasMultiplePhones: Boolean = false
)

data class ContactPresence(
    val contactId: Long,
    val isOnline: Boolean,
    val lastSeenEpoch: Long?
)
