package com.msgmates.app.core.audio

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class AudioRecorder @Inject constructor(
    private val context: Context
) {

    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var outputFile: File? = null

    suspend fun startRecording(): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            if (isRecording) {
                return@withContext Result.failure(Exception("Already recording"))
            }

            // Create output file
            outputFile = File.createTempFile("voice_note_", ".m4a", context.cacheDir)

            // Initialize MediaRecorder
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile!!.absolutePath)

                try {
                    prepare()
                    start()
                    isRecording = true
                    Log.d("AudioRecorder", "Recording started: ${outputFile!!.absolutePath}")
                } catch (e: IOException) {
                    Log.e("AudioRecorder", "Failed to prepare MediaRecorder", e)
                    throw e
                }
            }

            Result.success(Uri.fromFile(outputFile))
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Failed to start recording", e)
            Result.failure(e)
        }
    }

    suspend fun stopRecording(): Result<Uri?> = withContext(Dispatchers.IO) {
        try {
            if (!isRecording) {
                return@withContext Result.failure(Exception("Not recording"))
            }

            mediaRecorder?.apply {
                try {
                    stop()
                    release()
                } catch (e: Exception) {
                    Log.e("AudioRecorder", "Error stopping MediaRecorder", e)
                }
            }

            isRecording = false
            mediaRecorder = null

            val file = outputFile
            outputFile = null

            if (file != null && file.exists() && file.length() > 0) {
                Log.d("AudioRecorder", "Recording stopped: ${file.absolutePath}, size: ${file.length()}")
                Result.success(Uri.fromFile(file))
            } else {
                Log.w("AudioRecorder", "Recording file is empty or doesn't exist")
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Failed to stop recording", e)
            Result.failure(e)
        }
    }

    fun isCurrentlyRecording(): Boolean = isRecording

    fun getRecordingDuration(): Long {
        return try {
            mediaRecorder?.maxAmplitude?.toLong() ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    fun cleanup() {
        if (isRecording) {
            try {
                mediaRecorder?.stop()
                mediaRecorder?.release()
            } catch (e: Exception) {
                Log.e("AudioRecorder", "Error during cleanup", e)
            }
        }

        isRecording = false
        mediaRecorder = null

        // Clean up temp file
        outputFile?.let { file ->
            if (file.exists()) {
                file.delete()
            }
        }
        outputFile = null
    }
}
