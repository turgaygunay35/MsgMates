package com.msgmates.app.domain.call

enum class CallState {
    IDLE,
    OUTGOING,
    INCOMING,
    ONGOING,
    ENDED
}

enum class CallType {
    AUDIO,
    VIDEO
}

data class CallInfo(
    val callId: String,
    val callerId: String,
    val callerName: String,
    val callerAvatar: String? = null,
    val callType: CallType,
    val state: CallState,
    val startTime: Long = 0L,
    val duration: Long = 0L,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isCameraOn: Boolean = false
)

sealed class CallUiState {
    object Idle : CallUiState()

    data class Outgoing(
        val callId: String,
        val calleeId: String,
        val calleeName: String,
        val calleeAvatar: String? = null,
        val callType: CallType
    ) : CallUiState()

    data class Incoming(
        val callId: String,
        val callerId: String,
        val callerName: String,
        val callerAvatar: String? = null,
        val callType: CallType
    ) : CallUiState()

    data class Ongoing(
        val callId: String,
        val callerId: String,
        val callerName: String,
        val callerAvatar: String? = null,
        val callType: CallType,
        val duration: Long,
        val isMuted: Boolean,
        val isSpeakerOn: Boolean,
        val isCameraOn: Boolean
    ) : CallUiState()

    data class Ended(
        val callId: String,
        val duration: Long,
        val reason: CallEndReason
    ) : CallUiState()
}

enum class CallEndReason {
    HANGUP,
    REJECTED,
    BUSY,
    FAILED,
    TIMEOUT
}
