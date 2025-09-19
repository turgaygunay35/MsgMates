package com.msgmates.app.network

import android.content.SharedPreferences
import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor @Inject constructor(
    private val prefs: SharedPreferences
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val builder = original.newBuilder()

        // Token'ı güvenli şekilde String'e çevir
        val rawToken: String = prefs.getString("token", null)?.trim().orEmpty()
        if (rawToken.isNotEmpty()) {
            val headerValue = if (rawToken.startsWith("Bearer ")) rawToken else "Bearer $rawToken"
            builder.addHeader("Authorization", headerValue)
        }

        return chain.proceed(builder.build())
    }
}
