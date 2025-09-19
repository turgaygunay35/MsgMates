package com.msgmates.app.core.messaging.remote

import com.msgmates.app.core.messaging.remote.dto.ReceiptDTO
import com.msgmates.app.core.messaging.remote.dto.SendMessageRequest
import com.msgmates.app.core.messaging.remote.dto.SendMessageResponse
import com.msgmates.app.core.messaging.remote.dto.SyncRequest
import com.msgmates.app.core.messaging.remote.dto.SyncResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface MessagingApi {

    @POST("/v1/messages/send")
    suspend fun send(@Body body: SendMessageRequest): SendMessageResponse

    @POST("/v1/messages/sync")
    suspend fun sync(@Body body: SyncRequest): SyncResponse

    @POST("/v1/messages/receipts")
    suspend fun receipts(@Body body: ReceiptDTO)
}
