package com.msgmates.app.core.download

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

@Singleton
class AttachmentDownloader @Inject constructor(
    private val context: Context,
    private val okHttpClient: OkHttpClient
) {

    suspend fun downloadAttachment(
        remoteUrl: String,
        fileName: String,
        mimeType: String,
        onProgress: (Int) -> Unit
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(remoteUrl)
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.e("AttachmentDownloader", "Download failed: ${response.code}")
                return@withContext null
            }

            val responseBody = response.body ?: return@withContext null
            val contentLength = responseBody.contentLength()
            var downloadedBytes = 0L

            // Create file in appropriate directory based on mime type
            val uri = when {
                mimeType.startsWith("image/") -> saveToMediaStore(
                    fileName,
                    mimeType,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                mimeType.startsWith("video/") -> saveToMediaStore(
                    fileName,
                    mimeType,
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                )
                mimeType.startsWith("audio/") -> saveToMediaStore(
                    fileName,
                    mimeType,
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                )
                else -> saveToDownloads(fileName, mimeType)
            }

            uri?.let { fileUri ->
                context.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                    responseBody.byteStream().use { inputStream ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int

                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead

                            if (contentLength > 0) {
                                val progress = ((downloadedBytes * 100) / contentLength).toInt()
                                onProgress(progress)
                            }
                        }
                    }
                }
            }

            uri
        } catch (e: Exception) {
            Log.e("AttachmentDownloader", "Download failed", e)
            null
        }
    }

    private fun saveToMediaStore(
        fileName: String,
        mimeType: String,
        collection: Uri
    ): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, getRelativePath(mimeType))
        }

        return context.contentResolver.insert(collection, contentValues)
    }

    private fun saveToDownloads(fileName: String, mimeType: String): Uri? {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        return context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
    }

    private fun getRelativePath(mimeType: String): String {
        return when {
            mimeType.startsWith("image/") -> "Pictures/MsgMates"
            mimeType.startsWith("video/") -> "Movies/MsgMates"
            mimeType.startsWith("audio/") -> "Music/MsgMates"
            else -> "Download/MsgMates"
        }
    }
}
