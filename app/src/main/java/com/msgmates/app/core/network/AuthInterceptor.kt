package com.msgmates.app.core.network

import com.msgmates.app.core.auth.TokenRepository
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.Interceptor
import okhttp3.Response

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenRepo: TokenRepository
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val access = tokenRepo.getAccessToken()
        val req = if (!access.isNullOrBlank()) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $access")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(req)
    }
}
