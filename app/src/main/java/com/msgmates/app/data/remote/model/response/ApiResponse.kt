package com.msgmates.app.data.remote.model.response

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?,
    val error: String?,
    val code: Int
)

data class FileUploadResponse(
    val fileId: String,
    val fileName: String,
    val fileUrl: String,
    val fileType: String,
    val fileSize: Long,
    val thumbnailUrl: String? = null
)
