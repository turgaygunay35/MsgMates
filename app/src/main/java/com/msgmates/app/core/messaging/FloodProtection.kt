package com.msgmates.app.core.messaging

import android.util.Log
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay

@Singleton
class FloodProtection @Inject constructor() {

    private val retryAfterMap = ConcurrentHashMap<String, Long>()
    private val lastRequestTime = ConcurrentHashMap<String, Long>()
    private val minRequestInterval = 1000L // 1 second minimum between requests

    suspend fun checkRateLimit(conversationId: String): Boolean {
        val now = System.currentTimeMillis()
        val lastRequest = lastRequestTime[conversationId] ?: 0L
        val retryAfter = retryAfterMap[conversationId] ?: 0L

        // Check if we're in retry-after period
        if (retryAfter > now) {
            val waitTime = retryAfter - now
            Log.w("FloodProtection", "Rate limited for conversation $conversationId, waiting ${waitTime}ms")
            delay(waitTime)
            return false
        }

        // Check minimum interval between requests
        if (now - lastRequest < minRequestInterval) {
            val waitTime = minRequestInterval - (now - lastRequest)
            Log.d("FloodProtection", "Too fast for conversation $conversationId, waiting ${waitTime}ms")
            delay(waitTime)
        }

        lastRequestTime[conversationId] = now
        return true
    }

    fun handleRateLimit(conversationId: String, retryAfterSeconds: Int) {
        val retryAfter = System.currentTimeMillis() + (retryAfterSeconds * 1000L)
        retryAfterMap[conversationId] = retryAfter
        Log.w(
            "FloodProtection",
            "Rate limit applied for conversation $conversationId, retry after $retryAfterSeconds seconds"
        )
    }

    fun clearRateLimit(conversationId: String) {
        retryAfterMap.remove(conversationId)
        lastRequestTime.remove(conversationId)
    }

    fun isRateLimited(conversationId: String): Boolean {
        val retryAfter = retryAfterMap[conversationId] ?: 0L
        return retryAfter > System.currentTimeMillis()
    }

    fun getRemainingWaitTime(conversationId: String): Long {
        val retryAfter = retryAfterMap[conversationId] ?: 0L
        val now = System.currentTimeMillis()
        return maxOf(0, retryAfter - now)
    }
}
