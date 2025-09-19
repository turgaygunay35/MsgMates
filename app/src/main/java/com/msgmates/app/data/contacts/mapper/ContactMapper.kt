package com.msgmates.app.data.contacts.mapper

import com.msgmates.app.data.contacts.local.entity.ContactEntity
import com.msgmates.app.data.contacts.local.entity.ContactWithPhones
import com.msgmates.app.data.contacts.local.entity.PhoneEntity
import com.msgmates.app.domain.contacts.model.Contact
import com.msgmates.app.domain.contacts.model.Phone
import com.msgmates.app.util.DiacriticRemover

object ContactMapper {

    fun ContactEntity.toDomainModel(): Contact {
        return Contact(
            id = id,
            displayName = displayName,
            photoUri = photoUri,
            isMsgMatesUser = isMsgMatesUser,
            favorite = favorite,
            lastSeenEpoch = lastSeenEpoch,
            presenceOnline = presenceOnline,
            normalizedPrimary = normalizedPrimary,
            phones = emptyList() // Will be filled separately
        )
    }

    fun ContactWithPhones.toDomainModel(): Contact {
        return Contact(
            id = contact.id,
            displayName = contact.displayName,
            photoUri = contact.photoUri,
            isMsgMatesUser = contact.isMsgMatesUser,
            favorite = contact.favorite,
            lastSeenEpoch = contact.lastSeenEpoch,
            presenceOnline = contact.presenceOnline,
            normalizedPrimary = contact.normalizedPrimary,
            phones = phones.map { it.toDomainModel() }
        )
    }

    fun PhoneEntity.toDomainModel(): Phone {
        return Phone(
            id = id,
            contactId = contactId,
            rawNumber = rawNumber,
            normalizedE164 = normalizedE164,
            type = type,
            label = label
        )
    }

    fun Contact.toEntity(): ContactEntity {
        return ContactEntity(
            id = id,
            displayName = displayName,
            photoUri = photoUri,
            isMsgMatesUser = isMsgMatesUser,
            favorite = favorite,
            lastSeenEpoch = lastSeenEpoch,
            presenceOnline = presenceOnline,
            normalizedPrimary = normalizedPrimary
        )
    }

    fun Phone.toEntity(): PhoneEntity {
        return PhoneEntity(
            id = id,
            contactId = contactId,
            rawNumber = rawNumber,
            normalizedE164 = normalizedE164,
            type = type,
            label = label
        )
    }

    /**
     * Checks if contact matches search query (name or number)
     */
    fun Contact.matchesQuery(query: String): Boolean {
        if (query.isBlank()) return true

        // Check display name
        if (DiacriticRemover.containsMatch(query, displayName)) {
            return true
        }

        // Check normalized primary number
        if (normalizedPrimary != null && DiacriticRemover.containsMatch(query, normalizedPrimary)) {
            return true
        }

        // Check all phone numbers
        return phones.any { phone ->
            phone.rawNumber.contains(query, ignoreCase = true) ||
                (phone.normalizedE164 != null && phone.normalizedE164.contains(query))
        }
    }

    // Köprü fonksiyonları - Repository'den statik çağrı için
    fun mapToDomain(entity: ContactEntity): Contact =
        with(this) { entity.toDomainModel() }

    fun mapToDomain(cwp: ContactWithPhones): Contact =
        with(this) { cwp.toDomainModel() }
}
