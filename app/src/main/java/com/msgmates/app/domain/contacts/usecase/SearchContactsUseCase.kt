package com.msgmates.app.domain.contacts.usecase

import com.msgmates.app.data.contacts.ContactsRepository
import com.msgmates.app.domain.contacts.model.Contact
import com.msgmates.app.util.DiacriticRemover
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class SearchContactsUseCase @Inject constructor(
    private val contactsRepository: ContactsRepository
) {

    operator fun invoke(query: String): Flow<List<Contact>> {
        val normalizedQuery = DiacriticRemover.normalizeForSearch(query)
        return contactsRepository.searchContacts(normalizedQuery)
    }
}
