package com.msgmates.app.network

import com.msgmates.app.data.remote.model.request.LoginRequest
import com.msgmates.app.data.remote.model.request.RefreshTokenRequest
import com.msgmates.app.data.remote.model.request.RegisterRequest
import com.msgmates.app.data.remote.model.response.ApiResponse
import com.msgmates.app.data.remote.model.response.AuthResponse
import com.msgmates.app.data.remote.model.response.FileUploadResponse
import com.msgmates.app.data.remote.model.response.UserResponse
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Authentication
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>

    // User Management
    @GET("user/profile")
    suspend fun getUserProfile(): Response<UserResponse>

    @PUT("user/profile")
    suspend fun updateUserProfile(@Body user: UserResponse): Response<UserResponse>

    @DELETE("user/account")
    suspend fun deleteAccount(): Response<ApiResponse<Unit>>

    // File Management
    @Multipart
    @POST("files/upload")
    suspend fun uploadFile(
        @Part file: okhttp3.MultipartBody.Part,
        @Part("type") type: String
    ): Response<FileUploadResponse>

    @GET("files/{fileId}")
    suspend fun downloadFile(@Path("fileId") fileId: String): Response<okhttp3.ResponseBody>

    @DELETE("files/{fileId}")
    suspend fun deleteFile(@Path("fileId") fileId: String): Response<ApiResponse<Unit>>

    // Messages
    @GET("messages")
    suspend fun getMessages(
        @Query("chatId") chatId: String,
        @Query("page") page: Int = 0,
        @Query("limit") limit: Int = 50
    ): Response<ApiResponse<List<com.msgmates.app.domain.model.Message>>>

    @POST("messages")
    suspend fun sendMessage(@Body message: com.msgmates.app.domain.model.Message): Response<ApiResponse<com.msgmates.app.domain.model.Message>>

    // Contacts
    @GET("contacts")
    suspend fun getContacts(): Response<ApiResponse<List<com.msgmates.app.domain.model.Contact>>>

    @POST("contacts/invite")
    suspend fun inviteContact(@Body phoneNumber: String): Response<ApiResponse<Unit>>

    // Disaster Mode
    @POST("disaster/alive")
    suspend fun sendAliveSignal(@Body location: String?): Response<ApiResponse<Unit>>

    @GET("disaster/nearby")
    suspend fun getNearbyUsers(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radius: Int = 1000
    ): Response<ApiResponse<List<com.msgmates.app.domain.model.User>>>

    // Journal
    @POST("journal/story")
    suspend fun uploadStory(
        @Part video: okhttp3.MultipartBody.Part,
        @Part("caption") caption: String?
    ): Response<ApiResponse<com.msgmates.app.domain.model.Story>>

    @GET("journal/stories")
    suspend fun getStories(
        @Query("page") page: Int = 0,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<List<com.msgmates.app.domain.model.Story>>>

    // Message Capsule
    @POST("capsule")
    suspend fun createCapsule(@Body capsule: com.msgmates.app.domain.model.MessageCapsule): Response<ApiResponse<com.msgmates.app.domain.model.MessageCapsule>>

    @GET("capsule")
    suspend fun getCapsules(): Response<ApiResponse<List<com.msgmates.app.domain.model.MessageCapsule>>>

    @DELETE("capsule/{capsuleId}")
    suspend fun deleteCapsule(@Path("capsuleId") capsuleId: String): Response<ApiResponse<Unit>>
}
