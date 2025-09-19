package com.msgmates.app.core.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.msgmates.app.R
import com.msgmates.app.ui.call.IncomingCallActivity
import com.msgmates.app.ui.call.OngoingCallActivity

/**
 * Builder for call-related notifications
 */
class CallNotificationBuilder(private val context: Context) {

    companion object {
        const val NOTIFICATION_ID_INCOMING = 1001
        const val NOTIFICATION_ID_ONGOING = 1002

        // Actions
        const val ACTION_END = "com.msgmates.app.ACTION_END_CALL"
        const val ACTION_MUTE = "com.msgmates.app.ACTION_MUTE_CALL"
        const val ACTION_SPEAKER = "com.msgmates.app.ACTION_SPEAKER_CALL"
        const val ACTION_CAMERA_TOGGLE = "com.msgmates.app.ACTION_CAMERA_TOGGLE"
        const val ACTION_ANSWER = "com.msgmates.app.ACTION_ANSWER_CALL"
        const val ACTION_REJECT = "com.msgmates.app.ACTION_REJECT_CALL"

        // Extras
        const val EXTRA_CALL_ID = "call_id"
        const val EXTRA_CALLEE_ID = "callee_id"
        const val EXTRA_CALLEE_NAME = "callee_name"
        const val EXTRA_IS_VIDEO = "is_video"
    }

    fun buildIncomingCallNotification(
        callId: String,
        calleeId: String,
        calleeName: String,
        isVideo: Boolean
    ): Notification {
        val fullScreenIntent = Intent(context, IncomingCallActivity::class.java).apply {
            putExtra(EXTRA_CALL_ID, callId)
            putExtra(EXTRA_CALLEE_ID, calleeId)
            putExtra(EXTRA_CALLEE_NAME, calleeName)
            putExtra(EXTRA_IS_VIDEO, isVideo)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            callId.hashCode(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val answerIntent = Intent(ACTION_ANSWER).apply {
            putExtra(EXTRA_CALL_ID, callId)
            putExtra(EXTRA_IS_VIDEO, isVideo)
        }
        val answerPendingIntent = PendingIntent.getBroadcast(
            context,
            callId.hashCode() + 1,
            answerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val rejectIntent = Intent(ACTION_REJECT).apply {
            putExtra(EXTRA_CALL_ID, callId)
        }
        val rejectPendingIntent = PendingIntent.getBroadcast(
            context,
            callId.hashCode() + 2,
            rejectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, NotificationChannels.CHANNEL_CALLS)
            .setSmallIcon(R.drawable.ic_phone)
            .setContentTitle("Gelen Arama")
            .setContentText("$calleeName ${if (isVideo) "görüntülü" else "sesli"} arama yapıyor")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(
                R.drawable.ic_phone_answer,
                "Yanıtla",
                answerPendingIntent
            )
            .addAction(
                R.drawable.ic_phone_reject,
                "Reddet",
                rejectPendingIntent
            )
            .setTimeoutAfter(30000) // 30 saniye sonra otomatik kapanır
            .build()
    }

    fun buildOngoingCallNotification(
        callId: String,
        calleeName: String,
        isVideo: Boolean,
        isMuted: Boolean,
        isSpeakerOn: Boolean,
        isCameraOn: Boolean,
        duration: String
    ): Notification {
        val ongoingIntent = Intent(context, OngoingCallActivity::class.java).apply {
            putExtra(EXTRA_CALL_ID, callId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val ongoingPendingIntent = PendingIntent.getActivity(
            context,
            callId.hashCode(),
            ongoingIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val endIntent = Intent(ACTION_END).apply {
            putExtra(EXTRA_CALL_ID, callId)
        }
        val endPendingIntent = PendingIntent.getBroadcast(
            context,
            callId.hashCode() + 1,
            endIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val muteIntent = Intent(ACTION_MUTE).apply {
            putExtra(EXTRA_CALL_ID, callId)
        }
        val mutePendingIntent = PendingIntent.getBroadcast(
            context,
            callId.hashCode() + 2,
            muteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val speakerIntent = Intent(ACTION_SPEAKER).apply {
            putExtra(EXTRA_CALL_ID, callId)
        }
        val speakerPendingIntent = PendingIntent.getBroadcast(
            context,
            callId.hashCode() + 3,
            speakerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cameraIntent = Intent(ACTION_CAMERA_TOGGLE).apply {
            putExtra(EXTRA_CALL_ID, callId)
        }
        val cameraPendingIntent = PendingIntent.getBroadcast(
            context,
            callId.hashCode() + 4,
            cameraIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, NotificationChannels.CHANNEL_CALLS)
            .setSmallIcon(R.drawable.ic_phone)
            .setContentTitle(calleeName)
            .setContentText("${if (isVideo) "Görüntülü" else "Sesli"} arama - $duration")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setContentIntent(ongoingPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(
                if (isMuted) R.drawable.ic_mic_off else R.drawable.ic_mic,
                if (isMuted) "Sessiz" else "Mikrofon",
                mutePendingIntent
            )
            .addAction(
                if (isSpeakerOn) R.drawable.ic_speaker else R.drawable.ic_speaker_off,
                if (isSpeakerOn) "Hoparlör" else "Kulaklık",
                speakerPendingIntent
            )
            .apply {
                if (isVideo) {
                    addAction(
                        if (isCameraOn) R.drawable.ic_videocam else R.drawable.ic_videocam_off,
                        if (isCameraOn) "Kamera" else "Kamera Kapalı",
                        cameraPendingIntent
                    )
                }
            }
            .addAction(
                R.drawable.ic_call_end,
                "Bitir",
                endPendingIntent
            )
            .build()
    }
}
