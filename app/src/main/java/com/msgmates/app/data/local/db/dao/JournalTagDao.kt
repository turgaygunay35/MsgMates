package com.msgmates.app.data.local.db.dao

import androidx.room.*
import com.msgmates.app.data.local.db.entity.JournalTagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalTagDao {

    @Query("SELECT * FROM journal_tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<JournalTagEntity>>

    @Query("SELECT * FROM journal_tags WHERE id = :id")
    suspend fun getTagById(id: Long): JournalTagEntity?

    @Query("SELECT * FROM journal_tags WHERE name = :name")
    suspend fun getTagByName(name: String): JournalTagEntity?

    @Query(
        "SELECT t.* FROM journal_tags t INNER JOIN journal_entry_tags et ON t.id = et.tagId WHERE et.entryId = :entryId ORDER BY t.name ASC"
    )
    fun getTagsForEntry(entryId: String): Flow<List<JournalTagEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: JournalTagEntity): Long

    @Update
    suspend fun updateTag(tag: JournalTagEntity)

    @Delete
    suspend fun deleteTag(tag: JournalTagEntity)

    @Query("DELETE FROM journal_tags WHERE id = :id")
    suspend fun deleteTagById(id: Long)

    @Query("SELECT COUNT(*) FROM journal_tags")
    suspend fun getTagCount(): Int

    @Query("SELECT * FROM journal_tags WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchTags(query: String): Flow<List<JournalTagEntity>>
}
