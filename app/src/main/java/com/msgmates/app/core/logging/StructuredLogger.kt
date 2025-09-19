package com.msgmates.app.core.logging

import android.util.Log
import com.msgmates.app.BuildConfig
import java.util.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class LogEvent(
    val timestamp: Long = System.currentTimeMillis(),
    val level: String,
    val eventId: String,
    val message: String,
    val appVersion: String,
    val buildType: String,
    val deviceModel: String,
    val osVersion: String,
    val networkType: String? = null,
    val sessionId: String? = null,
    val userHash: String? = null,
    val requestId: String? = null,
    val traceId: String? = null,
    val spanId: String? = null,
    val extra: Map<String, String> = emptyMap()
)

object StructuredLogger {
    private val json = Json { ignoreUnknownKeys = true }

    fun logEvent(
        level: String,
        eventId: String,
        message: String,
        extra: Map<String, String> = emptyMap()
    ) {
        val event = LogEvent(
            level = level,
            eventId = eventId,
            message = message,
            appVersion = BuildConfig.VERSION_NAME,
            buildType = if (BuildConfig.DEBUG) "debug" else "release",
            deviceModel = android.os.Build.MODEL,
            osVersion = android.os.Build.VERSION.RELEASE,
            networkType = getNetworkType(),
            sessionId = getSessionId(),
            userHash = getUserHash(),
            requestId = getRequestId(),
            traceId = getTraceId(),
            spanId = getSpanId(),
            extra = extra
        )

        val jsonString = json.encodeToString(LogEvent.serializer(), event)
        Log.d("StructuredLogger", jsonString)
    }

    fun authRefreshStart(refreshTokenAge: Long? = null) {
        logEvent(
            "INFO",
            "AUTH_REFRESH_START",
            "Token refresh initiated", mapOf("token_age_sec" to (refreshTokenAge?.toString() ?: "unknown"))
        )
    }

    fun authRefreshOk(latencyMs: Long, newTokenAge: Long? = null) {
        logEvent(
            "INFO",
            "AUTH_REFRESH_OK",
            "Token refresh successful",
            mapOf(
                "latency_ms" to latencyMs.toString(),
                "new_token_age_sec" to (newTokenAge?.toString() ?: "unknown")
            )
        )
    }

    fun authRefreshFail(error: String, latencyMs: Long? = null) {
        logEvent(
            "ERROR",
            "AUTH_REFRESH_FAIL",
            "Token refresh failed: $error",
            mapOf("latency_ms" to (latencyMs?.toString() ?: "unknown"))
        )
    }

    fun authLogoutStart() {
        logEvent("INFO", "AUTH_LOGOUT_START", "User logout initiated")
    }

    fun authLogoutOk() {
        logEvent("INFO", "AUTH_LOGOUT_OK", "User logout completed")
    }

    fun netReqStart(endpoint: String, method: String) {
        logEvent(
            "DEBUG",
            "NET_REQ_START",
            "Network request started",
            mapOf("endpoint" to endpoint, "method" to method)
        )
    }

    fun netReqOk(endpoint: String, latencyMs: Long, statusCode: Int) {
        logEvent(
            "DEBUG",
            "NET_REQ_OK",
            "Network request successful",
            mapOf("endpoint" to endpoint, "latency_ms" to latencyMs.toString(), "status_code" to statusCode.toString())
        )
    }

    fun netReqFail(endpoint: String, error: String, statusCode: Int? = null) {
        logEvent(
            "WARN",
            "NET_REQ_FAIL",
            "Network request failed: $error",
            mapOf("endpoint" to endpoint, "status_code" to (statusCode?.toString() ?: "unknown"))
        )
    }

    fun wsConnect() {
        logEvent("INFO", "WS_CONNECT", "WebSocket connected")
    }

    fun wsDisconnect(reason: String? = null) {
        logEvent(
            "INFO",
            "WS_DISCONNECT",
            "WebSocket disconnected",
            mapOf("reason" to (reason ?: "unknown"))
        )
    }

    fun wsError(error: String) {
        logEvent("ERROR", "WS_ERROR", "WebSocket error: $error")
    }

    // Private helper methods
    private fun getNetworkType(): String? = null // TODO: Implement network type detection
    private fun getSessionId(): String? = null // TODO: Get from session manager
    private fun getUserHash(): String? = null // TODO: Get hashed user ID
    private fun getRequestId(): String? = MDC.getRequestId()
    private fun getTraceId(): String? = MDC.getTraceId()
    private fun getSpanId(): String? = MDC.getSpanId()
}
