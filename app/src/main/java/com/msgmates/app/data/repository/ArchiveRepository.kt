package com.msgmates.app.data.repository

import com.msgmates.app.domain.model.ArchiveItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class ArchiveRepository {
    private val _items = MutableStateFlow<List<ArchiveItem>>(emptyList())
    val items: Flow<List<ArchiveItem>> get() = _items

    fun add(item: ArchiveItem) {
        _items.value = _items.value + item
    }
    fun clear() {
        _items.value = emptyList()
    }
}
