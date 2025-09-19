package com.msgmates.app.ui.contacts.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msgmates.app.core.datastore.ContactsPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@HiltViewModel
class ContactsSettingsViewModel @Inject constructor(
    private val contactsPreferences: ContactsPreferences
) : ViewModel() {

    val highlightFlow: Flow<Boolean> = contactsPreferences.highlightFlow

    fun setHighlightMsgMatesUsers(enabled: Boolean) {
        viewModelScope.launch {
            contactsPreferences.setHighlight(enabled)
        }
    }
}
