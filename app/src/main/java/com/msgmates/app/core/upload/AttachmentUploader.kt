package com.msgmates.app.core.upload

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

@Singleton
class AttachmentUploader @Inject constructor(
    private val context: Context,
    private val uploadApi: UploadApi,
    private val envConfig: com.msgmates.app.core.env.EnvConfig
) {

    suspend fun uploadLocal(
        uri: Uri,
        kind: String,
        convoId: String,
        onProgress: (Int) -> Unit
    ): UploadResponse = withContext(Dispatchers.IO) {
        try {
            // Copy file to temp location
            val tempFile = copyUriToTempFile(uri)

            // Create multipart request
            val fileBody = tempFile.asRequestBody(
                getMimeType(uri)?.toMediaTypeOrNull()
            )

            val filePart = MultipartBody.Part.createFormData(
                "file",
                tempFile.name,
                fileBody
            )

            val kindBody = kind.toRequestBody("text/plain".toMediaTypeOrNull())
            val convoIdBody = convoId.toRequestBody("text/plain".toMediaTypeOrNull())

            // Upload with progress tracking
            val response = uploadWithProgress(
                filePart,
                kindBody,
                convoIdBody,
                onProgress
            )

            // Clean up temp file
            tempFile.delete()

            response
        } catch (e: Exception) {
            Log.e("AttachmentUploader", "Upload failed", e)
            throw e
        }
    }

    private suspend fun uploadWithProgress(
        filePart: MultipartBody.Part,
        kindBody: okhttp3.RequestBody,
        convoIdBody: okhttp3.RequestBody,
        onProgress: (Int) -> Unit
    ): UploadResponse {
        return withContext(Dispatchers.IO) {
            // For now, we'll use the regular upload API
            // In a real implementation, you'd use OkHttp with progress interceptor
            // and custom timeouts for large files
            uploadApi.upload(filePart, kindBody, convoIdBody)
        }
    }

    private fun copyUriToTempFile(uri: Uri): File {
        val tempFile = File.createTempFile("upload_", "_temp", context.cacheDir)

        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        return tempFile
    }

    private fun getMimeType(uri: Uri): String? {
        return context.contentResolver.getType(uri)
    }
}

// Progress tracking interceptor for future use
class ProgressInterceptor(
    private val onProgress: (Int) -> Unit
) : okhttp3.Interceptor {

    override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
        val originalResponse = chain.proceed(chain.request())

        return originalResponse.newBuilder()
            .body(
                originalResponse.body?.let { body ->
                    ProgressResponseBody(body, onProgress)
                }
            )
            .build()
    }
}

class ProgressResponseBody(
    private val responseBody: okhttp3.ResponseBody,
    private val onProgress: (Int) -> Unit
) : okhttp3.ResponseBody() {

    private var bufferedSource: okio.BufferedSource? = null

    override fun contentType() = responseBody.contentType()

    override fun contentLength() = responseBody.contentLength()

    override fun source(): okio.BufferedSource {
        if (bufferedSource == null) {
            bufferedSource = responseBody.source().buffer()
        }
        return bufferedSource!!
    }
}

// ProgressSource removed for simplicity - progress tracking can be added later
