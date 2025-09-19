package com.msgmates.app.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters

class CapsuleNotifyWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        // TODO: burada bildirim gönder
        // val title = inputData.getString("title") ?: "MsgMates"
        // val body = inputData.getString("body") ?: "Capsule ready."
        return Result.success(Data.EMPTY)
    }
}
