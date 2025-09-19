package com.msgmates.app.core.db.dao

import androidx.room.*
import com.msgmates.app.core.db.entity.AttachmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttachmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attachment: AttachmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(attachments: List<AttachmentEntity>)

    @Update
    suspend fun update(attachment: AttachmentEntity)

    @Delete
    suspend fun delete(attachment: AttachmentEntity)

    @Query("SELECT * FROM attachments WHERE msgId = :msgId")
    suspend fun forMessage(msgId: String): List<AttachmentEntity>

    @Query("SELECT * FROM attachments WHERE msgId = :msgId")
    fun streamForMessage(msgId: String): Flow<List<AttachmentEntity>>

    @Query("SELECT * FROM attachments WHERE id = :id")
    suspend fun getById(id: String): AttachmentEntity?

    @Query("UPDATE attachments SET remoteUrl = :remoteUrl WHERE id = :id")
    suspend fun updateRemoteUrl(id: String, remoteUrl: String)

    @Query("UPDATE attachments SET localUri = :localUri WHERE id = :id")
    suspend fun updateLocalUri(id: String, localUri: String)

    @Query("UPDATE attachments SET thumbB64 = :thumbB64 WHERE id = :id")
    suspend fun updateThumbnail(id: String, thumbB64: String)

    @Query("DELETE FROM attachments WHERE msgId = :msgId")
    suspend fun deleteForMessage(msgId: String)

    @Query("SELECT * FROM attachments WHERE kind = :kind AND msgId = :msgId")
    suspend fun getByKindAndMessage(kind: String, msgId: String): List<AttachmentEntity>
}
