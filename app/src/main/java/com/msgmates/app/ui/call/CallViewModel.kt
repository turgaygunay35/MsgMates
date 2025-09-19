package com.msgmates.app.ui.call

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.msgmates.app.core.call.CallService
import com.msgmates.app.core.call.CallSignalingManager
import com.msgmates.app.core.notification.CallNotificationBuilder
import com.msgmates.app.domain.call.CallInfo
import com.msgmates.app.domain.call.CallState
import com.msgmates.app.domain.call.CallType
import com.msgmates.app.domain.call.CallUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CallViewModel @Inject constructor(
    application: Application,
    private val callNotificationBuilder: CallNotificationBuilder,
    private val callSignalingManager: CallSignalingManager
) : AndroidViewModel(application) {

    private val _callState = MutableStateFlow<CallUiState>(CallUiState.Idle)
    val callState: StateFlow<CallUiState> = _callState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun startOutgoingCall(calleeId: String, calleeName: String, calleeAvatar: String?, video: Boolean) {
        val callId = "call_${System.currentTimeMillis()}"
        val callType = if (video) CallType.VIDEO else CallType.AUDIO

        _callState.value = CallUiState.Outgoing(
            callId = callId,
            calleeId = calleeId,
            calleeName = calleeName,
            calleeAvatar = calleeAvatar,
            callType = callType
        )

        // Send call offer via WebSocket
        callSignalingManager.sendCallOffer(
            calleeId = calleeId,
            callId = callId,
            video = video,
            sdpOffer = null // TODO: Add WebRTC SDP offer
        )

        // Mock: Start service and simulate connection
        startCallService(callId, calleeId, calleeName, calleeAvatar, callType)

        // Mock: Simulate connection after 1-2 seconds
        viewModelScope.launch {
            delay(1500)
            if (_callState.value is CallUiState.Outgoing) {
                val outgoingState = _callState.value as CallUiState.Outgoing
                _callState.value = CallUiState.Ongoing(
                    callId = outgoingState.callId,
                    callerId = outgoingState.calleeId,
                    callerName = outgoingState.calleeName,
                    callerAvatar = outgoingState.calleeAvatar,
                    callType = outgoingState.callType,
                    duration = 0L,
                    isMuted = false,
                    isSpeakerOn = false,
                    isCameraOn = video
                )
            }
        }
    }

    fun acceptIncomingCall(callId: String, video: Boolean) {
        _callState.value.let { currentState ->
            if (currentState is CallUiState.Incoming) {
                // Send call answer via WebSocket
                callSignalingManager.sendCallAnswer(
                    callId = callId,
                    sdpAnswer = null // TODO: Add WebRTC SDP answer
                )

                val callType = if (video) CallType.VIDEO else CallType.AUDIO
                _callState.value = CallUiState.Ongoing(
                    callId = currentState.callId,
                    callerId = currentState.callerId,
                    callerName = currentState.callerName,
                    callerAvatar = currentState.callerAvatar,
                    callType = callType,
                    duration = 0L,
                    isMuted = false,
                    isSpeakerOn = false,
                    isCameraOn = video
                )

                startCallService(
                    currentState.callId,
                    currentState.callerId,
                    currentState.callerName,
                    currentState.callerAvatar,
                    callType
                )
            }
        }
    }

    fun rejectIncomingCall() {
        _callState.value = CallUiState.Idle
    }

    fun endCall() {
        _callState.value.let { currentState ->
            when (currentState) {
                is CallUiState.Ongoing -> {
                    // Send call end via WebSocket
                    callSignalingManager.sendCallEnd(
                        callId = currentState.callId,
                        reason = "hangup"
                    )

                    _callState.value = CallUiState.Ended(
                        callId = currentState.callId,
                        duration = currentState.duration,
                        reason = com.msgmates.app.domain.call.CallEndReason.HANGUP
                    )
                }
                is CallUiState.Outgoing -> {
                    // Send call end via WebSocket
                    callSignalingManager.sendCallEnd(
                        callId = currentState.callId,
                        reason = "cancelled"
                    )

                    _callState.value = CallUiState.Idle
                }
                else -> {
                    _callState.value = CallUiState.Idle
                }
            }
        }

        stopCallService()
    }

    fun toggleMute() {
        _callState.value.let { currentState ->
            if (currentState is CallUiState.Ongoing) {
                _callState.value = currentState.copy(isMuted = !currentState.isMuted)
            }
        }
    }

    fun toggleSpeaker() {
        _callState.value.let { currentState ->
            if (currentState is CallUiState.Ongoing) {
                _callState.value = currentState.copy(isSpeakerOn = !currentState.isSpeakerOn)
            }
        }
    }

    fun toggleCamera() {
        _callState.value.let { currentState ->
            if (currentState is CallUiState.Ongoing) {
                _callState.value = currentState.copy(isCameraOn = !currentState.isCameraOn)
            }
        }
    }

    fun simulateIncomingCall(callerId: String, callerName: String, callerAvatar: String?, video: Boolean) {
        val callId = "incoming_${System.currentTimeMillis()}"
        val callType = if (video) CallType.VIDEO else CallType.AUDIO

        _callState.value = CallUiState.Incoming(
            callId = callId,
            callerId = callerId,
            callerName = callerName,
            callerAvatar = callerAvatar,
            callType = callType
        )

        // Show incoming call notification
        showIncomingCallNotification(callId, callerId, callerName, callerAvatar, callType)
    }

    private fun startCallService(
        callId: String,
        callerId: String,
        callerName: String,
        callerAvatar: String?,
        callType: CallType
    ) {
        val intent = Intent(getApplication(), CallService::class.java).apply {
            action = CallService.ACTION_START_CALL
            putExtra(CallService.EXTRA_CALL_ID, callId)
            putExtra(CallService.EXTRA_CALLER_ID, callerId)
            putExtra(CallService.EXTRA_CALLER_NAME, callerName)
            putExtra(CallService.EXTRA_CALLER_AVATAR, callerAvatar)
            putExtra(CallService.EXTRA_CALL_TYPE, callType.name)
        }
        getApplication<Application>().startService(intent)
    }

    private fun stopCallService() {
        val intent = Intent(getApplication(), CallService::class.java).apply {
            action = CallService.ACTION_END_CALL
        }
        getApplication<Application>().startService(intent)
    }

    private fun showIncomingCallNotification(
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
            state = CallState.INCOMING
        )

        val notification = callNotificationBuilder.createIncomingCallNotification(
            getApplication(),
            callInfo
        )

        val notificationManager = getApplication<Application>().getSystemService(
            android.content.Context.NOTIFICATION_SERVICE
        ) as android.app.NotificationManager
        notificationManager.notify(callId.hashCode(), notification)
    }

    fun resetCallState() {
        _callState.value = CallUiState.Idle
    }
}
