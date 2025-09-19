package com.msgmates.app.core.call

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Singleton
class CallSignalingManager @Inject constructor() {

    private val _callEvents = MutableSharedFlow<CallEvent>()
    val callEvents: SharedFlow<CallEvent> = _callEvents.asSharedFlow()

    private val json = Json { ignoreUnknownKeys = true }

    fun handleIncomingCallEvent(eventType: String, payload: String) {
        try {
            when (eventType) {
                "call_offer" -> {
                    val offer = json.decodeFromString<com.msgmates.app.core.messaging.ws.CallOffer>(payload)
                    _callEvents.tryEmit(CallEvent.Offer(offer))
                }
                "call_answer" -> {
                    val answer = json.decodeFromString<com.msgmates.app.core.messaging.ws.CallAnswer>(payload)
                    _callEvents.tryEmit(CallEvent.Answer(answer))
                }
                "call_candidate" -> {
                    val candidate = json.decodeFromString<com.msgmates.app.core.messaging.ws.CallCandidate>(payload)
                    _callEvents.tryEmit(CallEvent.Candidate(candidate))
                }
                "call_end" -> {
                    val end = json.decodeFromString<com.msgmates.app.core.messaging.ws.CallEnd>(payload)
                    _callEvents.tryEmit(CallEvent.End(end))
                }
            }
        } catch (e: Exception) {
            Log.e("CallSignalingManager", "Failed to parse call event", e)
        }
    }

    fun sendCallOffer(calleeId: String, callId: String, video: Boolean, sdpOffer: String? = null) {
        val offer = com.msgmates.app.core.messaging.ws.CallOffer(
            calleeId = calleeId,
            callId = callId,
            video = video,
            sdpOffer = sdpOffer
        )
        val payload = json.encodeToString(offer)
        // TODO: Send via WebSocket
        Log.d("CallSignalingManager", "Sending call offer: $payload")
    }

    fun sendCallAnswer(callId: String, sdpAnswer: String? = null) {
        val answer = com.msgmates.app.core.messaging.ws.CallAnswer(
            callId = callId,
            sdpAnswer = sdpAnswer
        )
        val payload = json.encodeToString(answer)
        // TODO: Send via WebSocket
        Log.d("CallSignalingManager", "Sending call answer: $payload")
    }

    fun sendCallCandidate(callId: String, candidate: String) {
        val candidateEvent = com.msgmates.app.core.messaging.ws.CallCandidate(
            callId = callId,
            candidate = candidate
        )
        val payload = json.encodeToString(candidateEvent)
        // TODO: Send via WebSocket
        Log.d("CallSignalingManager", "Sending call candidate: $payload")
    }

    fun sendCallEnd(callId: String, reason: String) {
        val end = com.msgmates.app.core.messaging.ws.CallEnd(
            callId = callId,
            reason = reason
        )
        val payload = json.encodeToString(end)
        // TODO: Send via WebSocket
        Log.d("CallSignalingManager", "Sending call end: $payload")
    }
}

sealed class CallEvent {
    data class Offer(val offer: com.msgmates.app.core.messaging.ws.CallOffer) : CallEvent()
    data class Answer(val answer: com.msgmates.app.core.messaging.ws.CallAnswer) : CallEvent()
    data class Candidate(val candidate: com.msgmates.app.core.messaging.ws.CallCandidate) : CallEvent()
    data class End(val end: com.msgmates.app.core.messaging.ws.CallEnd) : CallEvent()
}
