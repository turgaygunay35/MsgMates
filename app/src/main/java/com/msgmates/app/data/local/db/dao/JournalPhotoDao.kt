package com.msgmates.app.data.local.db.dao

import androidx.room.*
import com.msgmates.app.data.local.db.entity.JournalPhotoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalPhotoDao {

    @Query("SELECT * FROM journal_photos WHERE entryId = :entryId ORDER BY `order` ASC")
    fun getPhotosForEntry(entryId: String): Flow<List<JournalPhotoEntity>>

    @Query("SELECT * FROM journal_photos WHERE id = :id")
    suspend fun getPhotoById(id: Long): JournalPhotoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: JournalPhotoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<JournalPhotoEntity>)

    @Update
    suspend fun updatePhoto(photo: JournalPhotoEntity)

    @Delete
    suspend fun deletePhoto(photo: JournalPhotoEntity)

    @Query("DELETE FROM journal_photos WHERE id = :id")
    suspend fun deletePhotoById(id: Long)

    @Query("DELETE FROM journal_photos WHERE entryId = :entryId")
    suspend fun deletePhotosForEntry(entryId: String)

    @Query("SELECT COUNT(*) FROM journal_photos WHERE entryId = :entryId")
    suspend fun getPhotoCountForEntry(entryId: String): Int

    @Query("UPDATE journal_photos SET `order` = :order WHERE id = :id")
    suspend fun updatePhotoOrder(id: Long, order: Int)
}
