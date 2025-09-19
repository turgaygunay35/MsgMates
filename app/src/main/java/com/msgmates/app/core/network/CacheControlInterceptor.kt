package com.msgmates.app.core.network

import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor that adds cache control headers to GET requests
 * Only applies to GET requests to avoid caching POST/PUT/DELETE operations
 */
@Singleton
class CacheControlInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        // Only add cache headers to GET requests
        if (request.method == "GET") {
            val cacheControl = CacheControl.Builder()
                .maxAge(NetworkConfig.CACHE_MAX_AGE_SECONDS.toInt(), TimeUnit.SECONDS)
                .maxStale(NetworkConfig.CACHE_MAX_STALE_DAYS.toInt(), TimeUnit.DAYS)
                .build()

            return response.newBuilder()
                .header("Cache-Control", cacheControl.toString())
                .build()
        }

        return response
    }
}
