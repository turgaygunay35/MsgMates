package com.msgmates.app.core.db.dao

import androidx.room.*
import com.msgmates.app.core.db.entity.MessageEntity
import com.msgmates.app.core.db.entity.MessageEntityWithAttachments
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<MessageEntity>)

    @Update
    suspend fun update(message: MessageEntity)

    @Update
    @Transaction
    suspend fun updateAll(messages: List<MessageEntity>)

    @Query("SELECT * FROM messages WHERE convoId = :convoId ORDER BY localCreatedAt ASC")
    @Transaction
    fun streamByConversation(convoId: String): Flow<List<MessageEntityWithAttachments>>

    @Query("SELECT * FROM messages WHERE convoId = :convoId ORDER BY localCreatedAt ASC LIMIT :limit")
    @Transaction
    suspend fun getByConversation(convoId: String, limit: Int = 50): List<MessageEntityWithAttachments>

    @Query("UPDATE messages SET status = :status, sentAt = :sentAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, sentAt: Long? = null)

    @Query("UPDATE messages SET status = 'delivered' WHERE id IN (:ids)")
    suspend fun markDelivered(ids: List<String>)

    @Query("UPDATE messages SET status = 'read' WHERE id IN (:ids)")
    suspend fun markRead(ids: List<String>)

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getById(id: String): MessageEntity?

    @Query("SELECT * FROM messages WHERE convoId = :convoId AND status = 'pending' ORDER BY localCreatedAt ASC")
    suspend fun getPendingMessages(convoId: String): List<MessageEntity>

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE messages SET deleted = 1 WHERE id = :id")
    suspend fun markAsDeleted(id: String)

    @Query("UPDATE messages SET edited = 1, body = :newBody WHERE id = :id")
    suspend fun markAsEdited(id: String, newBody: String)
}
