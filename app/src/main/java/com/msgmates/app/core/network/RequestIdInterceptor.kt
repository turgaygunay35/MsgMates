package com.msgmates.app.core.network

import com.msgmates.app.core.logging.MDC
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.Interceptor
import okhttp3.Response

@Singleton
class RequestIdInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Generate or get existing request ID
        val requestId = MDC.getRequestId() ?: MDC.generateRequestId()
        MDC.setRequestId(requestId)

        // Add request ID to headers
        val newRequest = request.newBuilder()
            .header("X-Request-Id", requestId)
            .build()

        return chain.proceed(newRequest)
    }
}
