package com.msgmates.app.core.messaging.worker

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OutboxWorkManager @Inject constructor(
    private val context: Context
) {

    private val workManager = WorkManager.getInstance(context)

    fun enqueueOutboxWork(msgId: String? = null, attempt: Int = 0) {
        val workRequest = if (msgId != null) {
            // Unique work for specific message
            OutboxWorker.createUniqueWorkRequest(msgId, attempt)
        } else {
            // General outbox work
            OutboxWorker.createWorkRequest(null, attempt)
        }

        if (msgId != null) {
            // Use unique work to prevent duplicate processing
            workManager.enqueueUniqueWork(
                "outbox_$msgId",
                ExistingWorkPolicy.KEEP,
                workRequest as OneTimeWorkRequest
            )
        } else {
            // Use tagged work for general processing
            workManager.enqueue(workRequest)
        }
    }

    fun cancelOutboxWork(msgId: String? = null) {
        if (msgId != null) {
            workManager.cancelUniqueWork("outbox_$msgId")
        } else {
            workManager.cancelAllWorkByTag(OutboxWorker.WORK_TAG)
        }
    }

    fun cancelAllOutboxWork() {
        workManager.cancelAllWorkByTag(OutboxWorker.WORK_TAG)
    }
}
