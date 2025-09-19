package com.msgmates.app.core.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.msgmates.app.R
import com.msgmates.app.domain.call.CallInfo
import com.msgmates.app.domain.call.CallType
import com.msgmates.app.ui.call.IncomingCallActivity
import com.msgmates.app.ui.call.OngoingCallActivity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallNotificationBuilder @Inject constructor() {

    fun createIncomingCallNotification(
        context: Context,
        callInfo: CallInfo
    ): Notification {
        val fullScreenIntent = Intent(context, IncomingCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("callId", callInfo.callId)
            putExtra("callerId", callInfo.callerId)
            putExtra("callerName", callInfo.callerName)
            putExtra("callerAvatar", callInfo.callerAvatar)
            putExtra("callType", callInfo.callType.name)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val answerIntent = Intent(context, com.msgmates.app.core.call.CallService::class.java).apply {
            action = "ACTION_ANSWER_CALL"
            putExtra("callId", callInfo.callId)
            putExtra("callType", callInfo.callType.name)
        }
        val answerPendingIntent = PendingIntent.getService(
            context,
            1,
            answerIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val rejectIntent = Intent(context, com.msgmates.app.core.call.CallService::class.java).apply {
            action = "ACTION_REJECT_CALL"
            putExtra("callId", callInfo.callId)
        }
        val rejectPendingIntent = PendingIntent.getService(
            context,
            2,
            rejectIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, NotificationChannels.CHANNEL_CALLS)
            .setContentTitle("Gelen Arama")
            .setContentText(
                "${callInfo.callerName} ${if (callInfo.callType == CallType.VIDEO) "görüntülü" else "sesli"} arama yapıyor"
            )
            .setSmallIcon(R.drawable.ic_phone)
            // .setLargeIcon(null) // TODO: Load caller avatar
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(
                R.drawable.ic_phone_hangup,
                "Reddet",
                rejectPendingIntent
            )
            .addAction(
                if (callInfo.callType == CallType.VIDEO) R.drawable.ic_videocam else R.drawable.ic_phone,
                "Yanıtla",
                answerPendingIntent
            )
            .build()
    }

    fun createOngoingCallNotification(
        context: Context,
        callInfo: CallInfo
    ): Notification {
        val intent = Intent(context, OngoingCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("callId", callInfo.callId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val endCallIntent = Intent(context, com.msgmates.app.core.call.CallService::class.java).apply {
            action = "ACTION_END_CALL"
            putExtra("callId", callInfo.callId)
        }
        val endCallPendingIntent = PendingIntent.getService(
            context,
            1,
            endCallIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val muteIntent = Intent(context, com.msgmates.app.core.call.CallService::class.java).apply {
            action = "ACTION_MUTE_TOGGLE"
            putExtra("callId", callInfo.callId)
        }
        val mutePendingIntent = PendingIntent.getService(
            context,
            2,
            muteIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val speakerIntent = Intent(context, com.msgmates.app.core.call.CallService::class.java).apply {
            action = "ACTION_SPEAKER_TOGGLE"
            putExtra("callId", callInfo.callId)
        }
        val speakerPendingIntent = PendingIntent.getService(
            context,
            3,
            speakerIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val durationText = formatDuration(callInfo.duration)

        return NotificationCompat.Builder(context, NotificationChannels.CHANNEL_CALLS)
            .setContentTitle(callInfo.callerName)
            .setContentText(
                "$durationText • ${if (callInfo.callType == CallType.VIDEO) "Görüntülü" else "Sesli"} arama"
            )
            .setSmallIcon(R.drawable.ic_phone)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                R.drawable.ic_phone_hangup,
                "Bitir",
                endCallPendingIntent
            )
            .addAction(
                if (callInfo.isMuted) R.drawable.ic_mic_off else R.drawable.ic_mic,
                if (callInfo.isMuted) "Sesi Aç" else "Sessize Al",
                mutePendingIntent
            )
            .addAction(
                if (callInfo.isSpeakerOn) R.drawable.ic_speaker else R.drawable.ic_speaker_off,
                if (callInfo.isSpeakerOn) "Hoparlör Kapat" else "Hoparlör Aç",
                speakerPendingIntent
            )
            .build()
    }

    private fun formatDuration(durationMs: Long): String {
        val totalSeconds = durationMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}
