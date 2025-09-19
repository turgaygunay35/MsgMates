package com.msgmates.app.core.ws

import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random
import kotlinx.coroutines.delay

/**
 * Exponential backoff implementation for WebSocket reconnection
 * Provides jittered delays to prevent thundering herd problems
 */
class WsBackoff(
    private val baseDelayMs: Long = 500L,
    private val maxDelayMs: Long = 15000L,
    private val multiplier: Double = 2.0,
    private val jitterFactor: Double = 0.1
) {
    private var currentDelayMs = baseDelayMs
    private var attemptCount = 0

    /**
     * Get the next delay duration with exponential backoff and jitter
     * @return delay in milliseconds
     */
    suspend fun nextDelay(): Long {
        val delay = calculateDelay()
        attemptCount++
        delay(delay)
        return delay
    }

    /**
     * Reset backoff to initial state
     */
    fun reset() {
        currentDelayMs = baseDelayMs
        attemptCount = 0
    }

    /**
     * Get current attempt count
     */
    fun getAttemptCount(): Int = attemptCount

    /**
     * Check if max attempts reached
     */
    fun hasReachedMaxAttempts(maxAttempts: Int = 10): Boolean = attemptCount >= maxAttempts

    private fun calculateDelay(): Long {
        // Calculate exponential delay
        val exponentialDelay = (baseDelayMs * multiplier.pow(attemptCount)).toLong()
        val cappedDelay = min(exponentialDelay, maxDelayMs)

        // Add jitter to prevent thundering herd
        val jitterRange = (cappedDelay * jitterFactor).toLong()
        val jitter = Random.nextLong(-jitterRange, jitterRange + 1)

        val finalDelay = maxOf(baseDelayMs, cappedDelay + jitter)
        currentDelayMs = finalDelay

        return finalDelay
    }
}
