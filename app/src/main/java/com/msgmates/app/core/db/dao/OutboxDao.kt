package com.msgmates.app.core.db.dao

import androidx.room.*
import com.msgmates.app.core.db.entity.OutboxEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OutboxDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(outboxItem: OutboxEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueueAll(outboxItems: List<OutboxEntity>)

    @Query("SELECT * FROM outbox ORDER BY localId ASC LIMIT :limit")
    suspend fun nextBatch(limit: Int = 10): List<OutboxEntity>

    @Query("SELECT * FROM outbox ORDER BY localId ASC LIMIT :limit")
    fun streamNextBatch(limit: Int = 10): Flow<List<OutboxEntity>>

    @Query("DELETE FROM outbox WHERE msgId = :msgId")
    suspend fun delete(msgId: String)

    @Query("DELETE FROM outbox WHERE localId = :localId")
    suspend fun deleteByLocalId(localId: Long)

    @Query("UPDATE outbox SET attempt = :attempt, lastError = :lastError WHERE msgId = :msgId")
    suspend fun updateAttempt(msgId: String, attempt: Int, lastError: String?)

    @Query("SELECT * FROM outbox WHERE msgId = :msgId")
    suspend fun getByMsgId(msgId: String): OutboxEntity?

    @Query("SELECT COUNT(*) FROM outbox")
    suspend fun getPendingCount(): Int

    @Query("SELECT COUNT(*) FROM outbox")
    fun streamPendingCount(): Flow<Int>

    @Query("DELETE FROM outbox")
    suspend fun clearAll()

    @Query("SELECT * FROM outbox WHERE attempt >= :maxAttempts")
    suspend fun getFailedItems(maxAttempts: Int = 3): List<OutboxEntity>
}
