package com.msgmates.app.core.network

/**
 * Network configuration for different modes (normal vs disaster)
 * Controls timeouts, retry behavior, and caching strategies
 */
object NetworkConfig {
    var disasterMode: Boolean = false
        private set

    // Timeout configurations
    const val NORMAL_CONNECT_TIMEOUT_SECONDS = 10L
    const val NORMAL_READ_TIMEOUT_SECONDS = 10L
    const val NORMAL_WRITE_TIMEOUT_SECONDS = 10L

    const val DISASTER_CONNECT_TIMEOUT_SECONDS = 3L
    const val DISASTER_READ_TIMEOUT_SECONDS = 3L
    const val DISASTER_WRITE_TIMEOUT_SECONDS = 3L

    // Cache configuration
    const val HTTP_CACHE_SIZE_MB = 50L
    const val CACHE_MAX_AGE_SECONDS = 60L
    const val CACHE_MAX_STALE_DAYS = 7L

    // Retry configuration
    val enableHttpRetry: Boolean
        get() = !disasterMode

    val connectTimeoutSeconds: Long
        get() = if (disasterMode) DISASTER_CONNECT_TIMEOUT_SECONDS else NORMAL_CONNECT_TIMEOUT_SECONDS

    val readTimeoutSeconds: Long
        get() = if (disasterMode) DISASTER_READ_TIMEOUT_SECONDS else NORMAL_READ_TIMEOUT_SECONDS

    val writeTimeoutSeconds: Long
        get() = if (disasterMode) DISASTER_WRITE_TIMEOUT_SECONDS else NORMAL_WRITE_TIMEOUT_SECONDS

    /**
     * Enable disaster mode - faster timeouts, no retries
     */
    fun enableDisasterMode() {
        disasterMode = true
    }

    /**
     * Disable disaster mode - normal timeouts and retries
     */
    fun disableDisasterMode() {
        disasterMode = false
    }

    /**
     * Check if disaster mode is active
     */
    fun isDisasterMode(): Boolean = disasterMode
}
