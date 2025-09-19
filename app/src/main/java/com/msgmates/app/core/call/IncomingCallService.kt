package com.msgmates.app.core.call

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.msgmates.app.core.notifications.CallNotificationBuilder
import com.msgmates.app.ui.call.IncomingCallActivity

/**
 * Service for handling incoming call notifications and actions
 */
class IncomingCallService : BroadcastReceiver() {

    companion object {
        private const val TAG = "IncomingCallService"
        const val ACTION_SIMULATE_INCOMING = "com.msgmates.app.ACTION_SIMULATE_INCOMING"
        const val ACTION_ANSWER_CALL = "com.msgmates.app.ACTION_ANSWER_CALL"
        const val ACTION_REJECT_CALL = "com.msgmates.app.ACTION_REJECT_CALL"

        // Call data - will be provided by real call system
        private const val CALL_ID = "call_123"
        private const val CALLEE_ID = "user_456"
        private const val CALLEE_NAME = "Kullanıcı"
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_SIMULATE_INCOMING -> {
                simulateIncomingCall(context, intent)
            }
            ACTION_ANSWER_CALL -> {
                handleAnswerCall(context, intent)
            }
            ACTION_REJECT_CALL -> {
                handleRejectCall(context, intent)
            }
        }
    }

    private fun simulateIncomingCall(context: Context, intent: Intent) {
        val isVideo = intent.getBooleanExtra("is_video", false)
        val calleeName = intent.getStringExtra("callee_name") ?: CALLEE_NAME

        Log.d(TAG, "Simulating incoming call: $calleeName, video: $isVideo")

        // Create notification builder
        val callNotificationBuilder = CallNotificationBuilder(context)

        // Show notification
        val notification = callNotificationBuilder.buildIncomingCallNotification(
            callId = CALL_ID,
            calleeId = CALLEE_ID,
            calleeName = calleeName,
            isVideo = isVideo
        )

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(CallNotificationBuilder.NOTIFICATION_ID_INCOMING, notification)

        // Open incoming call activity
        val incomingIntent = Intent(context, IncomingCallActivity::class.java).apply {
            putExtra(CallNotificationBuilder.EXTRA_CALL_ID, CALL_ID)
            putExtra(CallNotificationBuilder.EXTRA_CALLEE_ID, CALLEE_ID)
            putExtra(CallNotificationBuilder.EXTRA_CALLEE_NAME, calleeName)
            putExtra(CallNotificationBuilder.EXTRA_IS_VIDEO, isVideo)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(incomingIntent)
    }

    private fun handleAnswerCall(context: Context, intent: Intent) {
        val callId = intent.getStringExtra(CallNotificationBuilder.EXTRA_CALL_ID) ?: return
        val isVideo = intent.getBooleanExtra(CallNotificationBuilder.EXTRA_IS_VIDEO, false)

        Log.d(TAG, "Answering call: $callId, video: $isVideo")

        // Dismiss incoming call notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(CallNotificationBuilder.NOTIFICATION_ID_INCOMING)

        // Start ongoing call service
        val serviceIntent = Intent(context, CallService::class.java).apply {
            putExtra(CallNotificationBuilder.EXTRA_CALL_ID, callId)
            putExtra(CallNotificationBuilder.EXTRA_IS_VIDEO, isVideo)
            action = CallService.ACTION_START_CALL
        }
        context.startForegroundService(serviceIntent)
    }

    private fun handleRejectCall(context: Context, intent: Intent) {
        val callId = intent.getStringExtra(CallNotificationBuilder.EXTRA_CALL_ID) ?: return

        Log.d(TAG, "Rejecting call: $callId")

        // Dismiss notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(CallNotificationBuilder.NOTIFICATION_ID_INCOMING)

        // TODO: Notify call service to end call
    }
}
