package com.msgmates.app.data.local.db.dao

import androidx.room.*
import com.msgmates.app.data.local.db.entity.CachedFileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedFileDao {

    @Query("SELECT * FROM cached_files")
    fun getAllCachedFiles(): Flow<List<CachedFileEntity>>

    @Query("SELECT * FROM cached_files WHERE id = :fileId")
    suspend fun getCachedFileById(fileId: String): CachedFileEntity?

    @Query("SELECT * FROM cached_files WHERE remoteUrl = :remoteUrl")
    suspend fun getCachedFileByUrl(remoteUrl: String): CachedFileEntity?

    @Query("SELECT * FROM cached_files WHERE fileType = :fileType")
    suspend fun getCachedFilesByType(fileType: String): List<CachedFileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedFile(file: CachedFileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedFiles(files: List<CachedFileEntity>)

    @Update
    suspend fun updateCachedFile(file: CachedFileEntity)

    @Delete
    suspend fun deleteCachedFile(file: CachedFileEntity)

    @Query("DELETE FROM cached_files WHERE id = :fileId")
    suspend fun deleteCachedFileById(fileId: String)

    @Query("DELETE FROM cached_files WHERE expiresAt < :currentTime")
    suspend fun deleteExpiredFiles(currentTime: Long)

    @Query("DELETE FROM cached_files")
    suspend fun deleteAllCachedFiles()
}
