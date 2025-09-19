package com.msgmates.app.core.network

import com.msgmates.app.core.metrics.Metrics
import java.time.Clock
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.Interceptor
import okhttp3.Response

@Singleton
class MetricsInterceptor @Inject constructor(
    private val metrics: Metrics,
    private val clock: Clock = Clock.systemUTC()
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val reqId = UUID.randomUUID().toString()
        val request = chain.request().newBuilder()
            .header("X-Request-Id", reqId)
            .build()

        val start = clock.millis()
        val dnsStart = System.nanoTime()
        
        try {
            val response = chain.proceed(request)
            val duration = clock.millis() - start
            val dnsDuration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - dnsStart)

            // DNS resolution time
            metrics.recordTime("dns_latency_ms", dnsDuration, mapOf("host" to request.url.host))
            
            // Total API latency
            metrics.recordTime(
                "api_latency_ms", duration,
                mapOf(
                    "method" to request.method, 
                    "path" to request.url.encodedPath,
                    "host" to request.url.host
                )
            )

            // Response code metrics
            if (response.code == 401) {
                metrics.increment("api_401_count", mapOf("path" to request.url.encodedPath))
            } else if (response.code >= 200 && response.code < 300) {
                metrics.increment("api_success_count")
            } else {
                metrics.increment("api_error_count", mapOf("status_code" to response.code.toString()))
            }

            return response
        } catch (e: Exception) {
            val duration = clock.millis() - start
            val dnsDuration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - dnsStart)
            
            // Record failed DNS resolution
            metrics.recordTime("dns_latency_ms", dnsDuration, mapOf("host" to request.url.host, "error" to "true"))
            metrics.recordTime("api_latency_ms", duration, mapOf("method" to request.method, "error" to "true"))
            metrics.increment("api_error_count", mapOf("error_type" to e.javaClass.simpleName))
            throw e
        }
    }
}
