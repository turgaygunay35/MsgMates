package com.msgmates.app.core.network

import retrofit2.Response
import retrofit2.http.GET

interface EchoApi {
    @GET("/v1/echo")
    suspend fun echo(): Response<Unit>
}
