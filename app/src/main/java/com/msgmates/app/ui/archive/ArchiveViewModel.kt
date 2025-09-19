package com.msgmates.app.ui.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msgmates.app.data.repository.ArchiveRepository
import com.msgmates.app.domain.model.ArchiveItem
import java.util.UUID
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class ArchiveViewModel : ViewModel() {
    private val repo = ArchiveRepository()
    val items = repo.items.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addDummy() {
        repo.add(ArchiveItem(UUID.randomUUID().toString(), "Başlık", "Açıklama", System.currentTimeMillis()))
    }
    fun clear() = repo.clear()
}
