package com.msgmates.app.data.repository

import com.msgmates.app.data.local.db.dao.CachedFileDao
import com.msgmates.app.data.remote.model.response.FileUploadResponse
import com.msgmates.app.network.ApiService
import java.io.File
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody

class FilesRepository(
    private val apiService: ApiService,
    private val cachedFileDao: CachedFileDao
) {

    suspend fun uploadFile(file: File, type: String): Result<FileUploadResponse> {
        return try {
            val requestFile = MultipartBody.Part.createFormData(
                "file",
                file.name,
                file.asRequestBody("application/octet-stream".toMediaType())
            )

            val response = apiService.uploadFile(requestFile, type)
            if (response.isSuccessful && response.body() != null) {
                val fileUploadResponse = response.body()!!
                Result.success(fileUploadResponse)
            } else {
                Result.failure(Exception("File upload failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadFile(fileId: String): Result<ResponseBody> {
        return try {
            val response = apiService.downloadFile(fileId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("File download failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteFile(fileId: String): Result<Unit> {
        return try {
            val response = apiService.deleteFile(fileId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("File deletion failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cacheFile(
        fileId: String,
        fileName: String,
        localPath: String,
        remoteUrl: String,
        fileType: String,
        fileSize: Long,
        expiresAt: Long? = null
    ) {
        val cachedFile = com.msgmates.app.data.local.db.entity.CachedFileEntity(
            id = fileId,
            fileName = fileName,
            localPath = localPath,
            remoteUrl = remoteUrl,
            fileType = fileType,
            fileSize = fileSize,
            downloadedAt = System.currentTimeMillis(),
            expiresAt = expiresAt
        )
        cachedFileDao.insertCachedFile(cachedFile)
    }

    suspend fun getCachedFile(fileId: String): com.msgmates.app.data.local.db.entity.CachedFileEntity? {
        return cachedFileDao.getCachedFileById(fileId)
    }

    suspend fun clearExpiredFiles() {
        cachedFileDao.deleteExpiredFiles(System.currentTimeMillis())
    }
}
