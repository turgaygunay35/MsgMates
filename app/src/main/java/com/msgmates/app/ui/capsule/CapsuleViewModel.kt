package com.msgmates.app.ui.capsule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CapsuleViewModel(private val repo: CapsuleRepository) : ViewModel() {

    val items: StateFlow<List<String>> =
        repo.flow().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addOrUpdate(s: String) {
        viewModelScope.launch { repo.upsert(s) }
    }

    fun remove(s: String) {
        viewModelScope.launch { repo.delete(s) }
    }
}
