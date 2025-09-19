package com.msgmates.app.core.call

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.msgmates.app.R
import com.msgmates.app.core.notification.CallNotificationBuilder
import com.msgmates.app.core.notification.NotificationChannels
import com.msgmates.app.domain.call.CallInfo
import com.msgmates.app.domain.call.CallState
import com.msgmates.app.domain.call.CallType
import com.msgmates.app.ui.call.OngoingCallActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CallService : Service() {

    @Inject
    lateinit var callNotificationBuilder: CallNotificationBuilder

    private val binder = CallBinder()
    private var wakeLock: PowerManager.WakeLock? = null
    private var callTimerJob: Job? = null

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _callState = MutableStateFlow<CallInfo?>(null)
    val callState: StateFlow<CallInfo?> = _callState.asStateFlow()

    companion object {
        const val ACTION_START_CALL = "ACTION_START_CALL"
        const val ACTION_END_CALL = "ACTION_END_CALL"
        const val ACTION_MUTE_TOGGLE = "ACTION_MUTE_TOGGLE"
        const val ACTION_SPEAKER_TOGGLE = "ACTION_SPEAKER_TOGGLE"
        const val ACTION_CAMERA_TOGGLE = "ACTION_CAMERA_TOGGLE"

        const val EXTRA_CALL_ID = "EXTRA_CALL_ID"
        const val EXTRA_CALLER_ID = "EXTRA_CALLER_ID"
        const val EXTRA_CALLER_NAME = "EXTRA_CALLER_NAME"
        const val EXTRA_CALLER_AVATAR = "EXTRA_CALLER_AVATAR"
        const val EXTRA_CALL_TYPE = "EXTRA_CALL_TYPE"

        private const val NOTIFICATION_ID = 1001
    }

    inner class CallBinder : Binder() {
        fun getService(): CallService = this@CallService
    }

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.createAllChannels(this)
        acquireWakeLock()
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_START_CALL -> {
                val callId = intent.getStringExtra(EXTRA_CALL_ID) ?: return START_NOT_STICKY
                val callerId = intent.getStringExtra(EXTRA_CALLER_ID) ?: return START_NOT_STICKY
                val callerName = intent.getStringExtra(EXTRA_CALLER_NAME) ?: return START_NOT_STICKY
                val callerAvatar = intent.getStringExtra(EXTRA_CALLER_AVATAR)
                val callType = CallType.valueOf(intent.getStringExtra(EXTRA_CALL_TYPE) ?: "AUDIO")

                startCall(callId, callerId, callerName, callerAvatar, callType)
            }
            ACTION_END_CALL -> {
                endCall()
            }
            ACTION_MUTE_TOGGLE -> {
                toggleMute()
            }
            ACTION_SPEAKER_TOGGLE -> {
                toggleSpeaker()
            }
            ACTION_CAMERA_TOGGLE -> {
                toggleCamera()
            }
        }

        return START_STICKY
    }

    private fun startCall(
        callId: String,
        callerId: String,
        callerName: String,
        callerAvatar: String?,
        callType: CallType
    ) {
        val callInfo = CallInfo(
            callId = callId,
            callerId = callerId,
            callerName = callerName,
            callerAvatar = callerAvatar,
            callType = callType,
            state = CallState.ONGOING,
            startTime = System.currentTimeMillis()
        )

        _callState.value = callInfo
        startForeground(NOTIFICATION_ID, createOngoingCallNotification(callInfo))
        startCallTimer()
    }

    private fun endCall() {
        callTimerJob?.cancel()
        _callState.value = null
        stopForeground(true)
        stopSelf()
    }

    private fun toggleMute() {
        _callState.value?.let { callInfo ->
            _callState.value = callInfo.copy(isMuted = !callInfo.isMuted)
            updateNotification()
        }
    }

    private fun toggleSpeaker() {
        _callState.value?.let { callInfo ->
            _callState.value = callInfo.copy(isSpeakerOn = !callInfo.isSpeakerOn)
            updateNotification()
        }
    }

    private fun toggleCamera() {
        _callState.value?.let { callInfo ->
            _callState.value = callInfo.copy(isCameraOn = !callInfo.isCameraOn)
            updateNotification()
        }
    }

    private fun startCallTimer() {
        callTimerJob = serviceScope.launch {
            while (true) {
                delay(1000)
                _callState.value?.let { callInfo ->
                    val duration = System.currentTimeMillis() - callInfo.startTime
                    _callState.value = callInfo.copy(duration = duration)
                    updateNotification()
                }
            }
        }
    }

    private fun createOngoingCallNotification(callInfo: CallInfo): Notification {
        val intent = Intent(this, OngoingCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_CALL_ID, callInfo.callId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, NotificationChannels.CHANNEL_CALLS)
            .setContentTitle(callInfo.callerName)
            .setContentText("Arama devam ediyor")
            .setSmallIcon(R.drawable.ic_phone)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(
                R.drawable.ic_phone_hangup,
                "Bitir",
                createActionPendingIntent(ACTION_END_CALL)
            )
            .addAction(
                if (callInfo.isMuted) R.drawable.ic_mic_off else R.drawable.ic_mic,
                if (callInfo.isMuted) "Sesi Aç" else "Sessize Al",
                createActionPendingIntent(ACTION_MUTE_TOGGLE)
            )
            .addAction(
                if (callInfo.isSpeakerOn) R.drawable.ic_speaker else R.drawable.ic_speaker_off,
                if (callInfo.isSpeakerOn) "Hoparlör Kapat" else "Hoparlör Aç",
                createActionPendingIntent(ACTION_SPEAKER_TOGGLE)
            )
            .build()
    }

    private fun updateNotification() {
        _callState.value?.let { callInfo ->
            val notification = createOngoingCallNotification(callInfo)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun createActionPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, CallService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "MsgMates:CallService"
        ).apply {
            acquire(10 * 60 * 1000L) // 10 minutes
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        wakeLock?.release()
        callTimerJob?.cancel()
        serviceScope.cancel()
    }
}
