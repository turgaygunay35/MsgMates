package com.msgmates.app.data.repository

import com.msgmates.app.data.local.db.dao.JournalDao
import com.msgmates.app.data.local.db.dao.JournalPhotoDao
import com.msgmates.app.data.local.db.dao.JournalTagDao
import com.msgmates.app.data.local.db.entity.JournalEntryEntity
import com.msgmates.app.data.local.db.entity.JournalPhotoEntity
import com.msgmates.app.data.local.db.entity.JournalTagEntity
import com.msgmates.app.domain.model.JournalEntry
import com.msgmates.app.domain.model.JournalMood
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

@Singleton
class JournalRepository @Inject constructor(
    private val journalDao: JournalDao,
    private val journalTagDao: JournalTagDao,
    private val journalPhotoDao: JournalPhotoDao
) : BaseRepository() {

    // Journal Entries
    fun getAllEntries(): Flow<List<JournalEntry>> = journalDao.getAllEntries().map { entities -> entities.map { it.toDomainModel() } }

    fun getArchivedEntries(): Flow<List<JournalEntry>> = journalDao.getArchivedEntries().map { entities -> entities.map { it.toDomainModel() } }

    fun getFavoriteEntries(): Flow<List<JournalEntry>> = journalDao.getFavoriteEntries().map { entities -> entities.map { it.toDomainModel() } }

    fun getEntryById(id: String): Flow<JournalEntry?> = flow {
        val entry = dbOperation { journalDao.getEntryById(id) }
        emit(entry?.toDomainModel())
    }

    fun searchEntries(query: String): Flow<List<JournalEntry>> = journalDao.searchEntries(query).map { entities -> entities.map { it.toDomainModel() } }

    fun getEntriesByMood(mood: JournalMood): Flow<List<JournalEntry>> = journalDao.getEntriesByMood(mood.name).map { entities -> entities.map { it.toDomainModel() } }

    fun getEntriesByDate(date: Long): Flow<List<JournalEntry>> = journalDao.getEntriesByDate(date).map { entities -> entities.map { it.toDomainModel() } }

    fun getEntriesByTag(tag: String): Flow<List<JournalEntry>> = journalDao.getEntriesByTag(tag).map { entities -> entities.map { it.toDomainModel() } }

    suspend fun createEntry(
        title: String,
        content: String,
        mood: JournalMood? = null,
        tags: List<String> = emptyList(),
        photoUris: List<String> = emptyList()
    ): String = dbOperation {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val entry = JournalEntryEntity(
            id = id,
            title = title,
            content = content,
            createdAt = now,
            updatedAt = now,
            mood = mood,
            tags = tags
        )

        journalDao.insertEntry(entry)

        // Add photos
        if (photoUris.isNotEmpty()) {
            val photos = photoUris.mapIndexed { index, uri ->
                JournalPhotoEntity(
                    entryId = id,
                    uri = uri,
                    order = index
                )
            }
            journalPhotoDao.insertPhotos(photos)
        }

        // Add tags
        if (tags.isNotEmpty()) {
            tags.forEach { tagName ->
                @Suppress("UNUSED_VARIABLE")
                val tagId = journalTagDao.insertTag(
                    JournalTagEntity(name = tagName)
                )
                // Note: We would need a method to insert cross-refs, but for now we'll use the tags field
            }
            // Note: We would need a method to insert cross-refs, but for now we'll use the tags field
        }

        id
    }

    suspend fun updateEntry(
        id: String,
        title: String,
        content: String,
        mood: JournalMood? = null,
        tags: List<String> = emptyList(),
        photoUris: List<String> = emptyList()
    ) {
        val now = System.currentTimeMillis()
        val existingEntry = journalDao.getEntryById(id)

        val entry = JournalEntryEntity(
            id = id,
            title = title,
            content = content,
            createdAt = existingEntry?.createdAt ?: now,
            updatedAt = now,
            isFavorite = existingEntry?.isFavorite ?: false,
            isArchived = existingEntry?.isArchived ?: false,
            mood = mood,
            tags = tags
        )

        journalDao.updateEntry(entry)

        // Update photos
        journalPhotoDao.deletePhotosForEntry(id)
        if (photoUris.isNotEmpty()) {
            val photos = photoUris.mapIndexed { index, uri ->
                JournalPhotoEntity(
                    entryId = id,
                    uri = uri,
                    order = index
                )
            }
            journalPhotoDao.insertPhotos(photos)
        }
    }

    suspend fun deleteEntry(id: String) {
        journalDao.deleteEntryById(id)
        journalPhotoDao.deletePhotosForEntry(id)
    }

    suspend fun toggleFavorite(id: String) {
        val entry = journalDao.getEntryById(id)
        entry?.let {
            journalDao.updateFavoriteStatus(id, !it.isFavorite)
        }
    }

    suspend fun archiveEntry(id: String) {
        journalDao.updateArchiveStatus(id, true)
    }

    suspend fun restoreEntry(id: String) {
        journalDao.updateArchiveStatus(id, false)
        // Update the updatedAt timestamp when restoring
        val now = System.currentTimeMillis()
        val entry = journalDao.getEntryById(id)
        entry?.let {
            val updatedEntry = it.copy(updatedAt = now)
            journalDao.updateEntry(updatedEntry)
        }
    }

    suspend fun archiveEntries(ids: List<String>) {
        journalDao.archiveEntries(ids)
    }

    suspend fun restoreEntries(ids: List<String>) {
        journalDao.restoreEntries(ids)
    }

    suspend fun deleteEntriesPermanently(ids: List<String>) {
        ids.forEach { id ->
            journalDao.deleteEntryById(id)
            journalPhotoDao.deletePhotosForEntry(id)
        }
    }

    // Tags
    fun getAllTags(): Flow<List<JournalTagEntity>> = journalTagDao.getAllTags()

    suspend fun createTag(name: String): Long = journalTagDao.insertTag(JournalTagEntity(name = name))

    suspend fun deleteTag(id: Long) = journalTagDao.deleteTagById(id)

    // Photos
    fun getPhotosForEntry(entryId: String): Flow<List<JournalPhotoEntity>> = journalPhotoDao.getPhotosForEntry(entryId)

    suspend fun addPhotoToEntry(entryId: String, uri: String) {
        val order = journalPhotoDao.getPhotoCountForEntry(entryId)
        val photo = JournalPhotoEntity(
            entryId = entryId,
            uri = uri,
            order = order
        )
        journalPhotoDao.insertPhoto(photo)
    }

    suspend fun removePhotoFromEntry(photoId: Long) {
        journalPhotoDao.deletePhotoById(photoId)
    }

    // Statistics
    suspend fun getEntryCount(): Int = journalDao.getEntryCount()

    suspend fun getTagCount(): Int = journalTagDao.getTagCount()

    private fun JournalEntryEntity.toDomainModel(): JournalEntry {
        return JournalEntry(
            id = id,
            title = title,
            content = content,
            createdAt = createdAt,
            updatedAt = updatedAt,
            mood = mood,
            tags = tags
        )
    }
}
