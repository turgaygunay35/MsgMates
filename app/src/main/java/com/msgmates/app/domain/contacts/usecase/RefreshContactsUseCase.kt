package com.msgmates.app.domain.contacts.usecase

import com.msgmates.app.data.contacts.ContactsRepository
import com.msgmates.app.util.Result
import javax.inject.Inject

class RefreshContactsUseCase @Inject constructor(
    private val contactsRepository: ContactsRepository
) {

    suspend operator fun invoke(force: Boolean = false): Result<Unit> {
        return contactsRepository.refreshContacts(force)
    }
}
