package com.msgmates.app.core.messaging.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import com.msgmates.app.core.messaging.MessageRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay

@HiltWorker
class OutboxWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val messageRepository: MessageRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "outbox_worker"
        const val WORK_TAG = "outbox_uploader"
        const val KEY_MSG_ID = "msg_id"
        const val KEY_ATTEMPT = "attempt"
        const val MAX_ATTEMPTS = 5

        fun createWorkRequest(msgId: String? = null, attempt: Int = 0): WorkRequest {
            val data = Data.Builder()
                .putString(KEY_MSG_ID, msgId)
                .putInt(KEY_ATTEMPT, attempt)
                .build()

            return OneTimeWorkRequestBuilder<OutboxWorker>()
                .setInputData(data)
                .addTag(WORK_TAG)
                .setConstraints(
                    androidx.work.Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(
                    androidx.work.BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
        }

        fun createUniqueWorkRequest(msgId: String, attempt: Int = 0): WorkRequest {
            val data = Data.Builder()
                .putString(KEY_MSG_ID, msgId)
                .putInt(KEY_ATTEMPT, attempt)
                .build()

            return OneTimeWorkRequestBuilder<OutboxWorker>()
                .setInputData(data)
                .addTag(WORK_TAG)
                .setConstraints(
                    androidx.work.Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(
                    androidx.work.BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val msgId = inputData.getString(KEY_MSG_ID)
            val attempt = inputData.getInt(KEY_ATTEMPT, 0)

            if (msgId != null) {
                // Process specific message
                processSingleMessage(msgId, attempt)
            } else {
                // Process batch of messages
                processBatch()
            }

            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("OutboxWorker", "Worker failed", e)

            // If this is a retry attempt and we've exceeded max attempts, give up
            val attempt = inputData.getInt(KEY_ATTEMPT, 0)
            if (attempt >= MAX_ATTEMPTS) {
                android.util.Log.w("OutboxWorker", "Max attempts reached ($MAX_ATTEMPTS), giving up")
                Result.failure()
            } else {
                android.util.Log.d(
                    "OutboxWorker",
                    "Retrying in ${WorkRequest.MIN_BACKOFF_MILLIS * (attempt + 1)}ms (attempt ${attempt + 1}/$MAX_ATTEMPTS)"
                )
                Result.retry()
            }
        }
    }

    private suspend fun processSingleMessage(msgId: String, attempt: Int) {
        val result = messageRepository.processOutboxItem(msgId)

        if (result.isFailure) {
            android.util.Log.w("OutboxWorker", "Failed to process message $msgId, attempt $attempt")
            throw Exception("Failed to process message: ${result.exceptionOrNull()?.message}")
        }
    }

    private suspend fun processBatch() {
        // This would be called by a periodic worker or when the app starts
        // For now, we'll process messages one by one
        // In a real implementation, you might want to process multiple messages in a batch

        // Get pending messages from outbox
        // Process them one by one
        // This is a simplified version - in reality you'd get the outbox items from the DAO

        android.util.Log.d("OutboxWorker", "Processing outbox batch")

        // Add a small delay to prevent overwhelming the server
        delay(1000)
    }
}
