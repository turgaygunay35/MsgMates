package com.msgmates.app.core.upload

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

data class UploadResponse(
    val remoteId: String,
    val remoteUrl: String?,
    val size: Long?
)

interface UploadApi {
    @Multipart
    @POST("/v1/upload")
    suspend fun upload(
        @Part file: MultipartBody.Part,
        @Part("kind") kind: RequestBody,
        @Part("conversationId") conversationId: RequestBody
    ): UploadResponse
}
