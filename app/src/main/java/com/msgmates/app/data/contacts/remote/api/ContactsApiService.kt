package com.msgmates.app.data.contacts.remote.api

import com.msgmates.app.data.contacts.remote.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ContactsApiService {

    @GET("v1/contacts/salt")
    suspend fun getSalt(): SaltResponse

    @POST("v1/contacts/match")
    suspend fun matchContacts(@Body request: MatchRequest): MatchResponse

    @POST("v1/presence")
    suspend fun getPresence(@Body request: PresenceRequest): PresenceResponse
}
