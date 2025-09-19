package com.msgmates.app.domain.contacts.usecase

import com.msgmates.app.data.contacts.ContactsRepository
import com.msgmates.app.util.Result
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val contactsRepository: ContactsRepository
) {

    suspend operator fun invoke(contactId: Long): Result<Boolean> {
        return contactsRepository.toggleFavorite(contactId)
    }
}
