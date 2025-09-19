package com.msgmates.app.core.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.msgmates.app.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

object NetworkModule {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val requestUrlLogger = Interceptor { chain ->
        val req = chain.request()
        // Nihai URL kanıtı (prefix hatalarını yakalar)
        android.util.Log.d("HTTP", "→ ${req.method} ${req.url}")
        val res = chain.proceed(req)
        android.util.Log.d("HTTP", "← ${res.code} ${req.url}")
        res
    }

    private fun loggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // debug'da kalsın
        }

    private fun okHttp(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(requestUrlLogger)
            .addInterceptor(loggingInterceptor())
            .build()

    fun retrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL) // "https://api.msgmates.com/"
            .client(okHttp())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
}
