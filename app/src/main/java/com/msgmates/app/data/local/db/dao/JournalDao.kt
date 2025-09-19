package com.msgmates.app.data.local.db.dao

import androidx.room.*
import com.msgmates.app.data.local.db.entity.JournalEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {

    @Query("SELECT * FROM journal_entries WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getAllEntries(): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE isArchived = 1 ORDER BY createdAt DESC")
    fun getArchivedEntries(): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE id = :id")
    suspend fun getEntryById(id: String): JournalEntryEntity?

    @Query(
        "SELECT * FROM journal_entries WHERE (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') AND isArchived = 0 ORDER BY createdAt DESC"
    )
    fun searchEntries(query: String): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE mood = :mood AND isArchived = 0 ORDER BY createdAt DESC")
    fun getEntriesByMood(mood: String): Flow<List<JournalEntryEntity>>

    @Query(
        "SELECT * FROM journal_entries WHERE date(createdAt/1000, 'unixepoch') = date(:date/1000, 'unixepoch') AND isArchived = 0 ORDER BY createdAt DESC"
    )
    fun getEntriesByDate(date: Long): Flow<List<JournalEntryEntity>>

    @Query(
        "SELECT * FROM journal_entries WHERE tags LIKE '%' || :tag || '%' AND isArchived = 0 ORDER BY createdAt DESC"
    )
    fun getEntriesByTag(tag: String): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE isFavorite = 1 AND isArchived = 0 ORDER BY createdAt DESC")
    fun getFavoriteEntries(): Flow<List<JournalEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntryEntity)

    @Update
    suspend fun updateEntry(entry: JournalEntryEntity)

    @Delete
    suspend fun deleteEntry(entry: JournalEntryEntity)

    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun deleteEntryById(id: String)

    @Query("SELECT COUNT(*) FROM journal_entries")
    suspend fun getEntryCount(): Int

    @Query("SELECT * FROM journal_entries WHERE isArchived = 0 ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentEntries(limit: Int): Flow<List<JournalEntryEntity>>

    @Query("UPDATE journal_entries SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean)

    @Query("UPDATE journal_entries SET isArchived = :isArchived WHERE id = :id")
    suspend fun updateArchiveStatus(id: String, isArchived: Boolean)

    @Query("UPDATE journal_entries SET isArchived = 1 WHERE id IN (:ids)")
    suspend fun archiveEntries(ids: List<String>)

    @Query("UPDATE journal_entries SET isArchived = 0 WHERE id IN (:ids)")
    suspend fun restoreEntries(ids: List<String>)
}
