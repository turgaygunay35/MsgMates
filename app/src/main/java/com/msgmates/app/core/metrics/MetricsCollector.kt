package com.msgmates.app.core.metrics

import com.msgmates.app.core.logging.StructuredLogger
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class MetricsCollector @Inject constructor() : Metrics {

    private val mutex = Mutex()

    // Counters
    private var refreshAttempts = 0L
    private var refreshSuccess = 0L
    private var refreshFailure = 0L
    private var logoutTotal = 0L
    private var wsReconnects = 0L

    // Timers (in milliseconds)
    private var refreshLatencyMs = 0L
    private var apiLatencyMs = 0L

    // Gauges
    private var inflightRequests = 0
    private var wsState = 0 // 0=disconnected, 1=connecting, 2=connected

    suspend fun incrementRefreshAttempts() {
        mutex.withLock {
            refreshAttempts++
            StructuredLogger.logEvent("DEBUG", "METRICS_REFRESH_ATTEMPT", "Refresh attempt #$refreshAttempts")
        }
    }

    suspend fun incrementRefreshSuccess(latencyMs: Long) {
        mutex.withLock {
            refreshSuccess++
            refreshLatencyMs = latencyMs
            StructuredLogger.logEvent(
                "DEBUG",
                "METRICS_REFRESH_SUCCESS",
                "Refresh success #$refreshSuccess, latency: ${latencyMs}ms"
            )
        }
    }

    suspend fun incrementRefreshFailure() {
        mutex.withLock {
            refreshFailure++
            StructuredLogger.logEvent("DEBUG", "METRICS_REFRESH_FAILURE", "Refresh failure #$refreshFailure")
        }
    }

    suspend fun incrementLogout() {
        mutex.withLock {
            logoutTotal++
            StructuredLogger.logEvent("DEBUG", "METRICS_LOGOUT", "Logout #$logoutTotal")
        }
    }

    suspend fun incrementWsReconnects() {
        mutex.withLock {
            wsReconnects++
            StructuredLogger.logEvent("DEBUG", "METRICS_WS_RECONNECT", "WebSocket reconnect #$wsReconnects")
        }
    }

    suspend fun recordApiLatency(endpoint: String, latencyMs: Long) {
        mutex.withLock {
            apiLatencyMs = latencyMs
            StructuredLogger.logEvent("DEBUG", "METRICS_API_LATENCY", "API latency for $endpoint: ${latencyMs}ms")
        }
    }

    suspend fun setInflightRequests(count: Int) {
        mutex.withLock {
            inflightRequests = count
            StructuredLogger.logEvent("DEBUG", "METRICS_INFLIGHT", "Inflight requests: $count")
        }
    }

    suspend fun setWsState(state: Int) {
        mutex.withLock {
            wsState = state
            val stateName = when (state) {
                0 -> "disconnected"
                1 -> "connecting" 2 -> "connected"
                else -> "unknown"
            }
            StructuredLogger.logEvent("DEBUG", "METRICS_WS_STATE", "WebSocket state: $stateName")
        }
    }

    override fun increment(name: String, tags: Map<String, String>) {
        runBlocking {
            mutex.withLock {
                when (name) {
                    "refresh_attempts" -> refreshAttempts++
                    "refresh_success" -> refreshSuccess++
                    "refresh_failure" -> refreshFailure++
                    "logout_total" -> logoutTotal++
                    "ws_reconnects" -> wsReconnects++
                    else -> {
                        // Unknown metric, log for debugging
                        StructuredLogger.logEvent("DEBUG", "METRICS_UNKNOWN", "Unknown metric: $name")
                    }
                }
            }
        }
    }

    override fun recordTime(name: String, durationMs: Long, tags: Map<String, String>) {
        runBlocking {
            mutex.withLock {
                when (name) {
                    "refresh_latency_ms" -> refreshLatencyMs = durationMs
                    "api_latency_ms" -> apiLatencyMs = durationMs
                    else -> {
                        // Unknown metric, log for debugging
                        StructuredLogger.logEvent("DEBUG", "METRICS_UNKNOWN_TIME", "Unknown time metric: $name")
                    }
                }
            }
        }
    }

    override fun setGauge(name: String, value: Number, tags: Map<String, String>) {
        runBlocking {
            mutex.withLock {
                when (name) {
                    "inflight_requests" -> inflightRequests = value.toInt()
                    "ws_state" -> wsState = value.toInt()
                    else -> {
                        // Unknown metric, log for debugging
                        StructuredLogger.logEvent("DEBUG", "METRICS_UNKNOWN_GAUGE", "Unknown gauge metric: $name")
                    }
                }
            }
        }
    }

    suspend fun getMetrics(): MetricsSnapshot = mutex.withLock {
        MetricsSnapshot(
            refreshAttempts = refreshAttempts,
            refreshSuccess = refreshSuccess,
            refreshFailure = refreshFailure,
            logoutTotal = logoutTotal,
            wsReconnects = wsReconnects,
            refreshLatencyMs = refreshLatencyMs,
            apiLatencyMs = apiLatencyMs,
            inflightRequests = inflightRequests,
            wsState = wsState,
            refreshSuccessRate = if (refreshAttempts > 0) (refreshSuccess.toDouble() / refreshAttempts) * 100 else 0.0
        )
    }

    data class MetricsSnapshot(
        val refreshAttempts: Long,
        val refreshSuccess: Long,
        val refreshFailure: Long,
        val logoutTotal: Long,
        val wsReconnects: Long,
        val refreshLatencyMs: Long,
        val apiLatencyMs: Long,
        val inflightRequests: Int,
        val wsState: Int,
        val refreshSuccessRate: Double
    )
}
