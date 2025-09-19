package com.msgmates.app.core.logging

import java.util.*
import java.util.concurrent.ConcurrentHashMap

object MDC {
    private val context = ConcurrentHashMap<String, String>()

    fun put(key: String, value: String?) {
        if (value != null) {
            context[key] = value
        } else {
            context.remove(key)
        }
    }

    fun get(key: String): String? = context[key]

    fun remove(key: String): String? = context.remove(key)

    fun clear() {
        context.clear()
    }

    fun getCopy(): Map<String, String> = HashMap(context)

    // Convenience methods for common keys
    fun setRequestId(requestId: String?) = put("request_id", requestId)
    fun getRequestId(): String? = get("request_id")

    fun setTraceId(traceId: String?) = put("trace_id", traceId)
    fun getTraceId(): String? = get("trace_id")

    fun setSpanId(spanId: String?) = put("span_id", spanId)
    fun getSpanId(): String? = get("span_id")

    fun setSessionId(sessionId: String?) = put("session_id", sessionId)
    fun getSessionId(): String? = get("session_id")

    fun setUserHash(userHash: String?) = put("user_hash", userHash)
    fun getUserHash(): String? = get("user_hash")

    // Generate new correlation IDs
    fun generateRequestId(): String = UUID.randomUUID().toString()
    fun generateTraceId(): String = UUID.randomUUID().toString()
    fun generateSpanId(): String = UUID.randomUUID().toString()
}
