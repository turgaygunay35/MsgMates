package com.msgmates.app.data.remote.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RequestCodeRequest(@SerialName("phone_number") val phoneNumber: String)

@Serializable
data class VerifyCodeRequest(
    @SerialName("phone_number") val phoneNumber: String,
    @SerialName("code") val code: String
)

@Serializable
data class UserDto(
    @SerialName("id") val id: Long,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    @SerialName("phone_number") val phoneNumber: String,
    @SerialName("is_verified") val isVerified: Int = 0,
    @SerialName("role") val role: String? = null
)

@Serializable
data class RequestCodeResponse(
    @SerialName("ok") val ok: Boolean,
    @SerialName("phone") val phone: String? = null,
    @SerialName("code") val code: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("error") val error: String? = null
)

@Serializable
data class VerifyCodeResponse(
    @SerialName("ok") val ok: Boolean,
    @SerialName("user") val user: UserDto? = null,
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("refresh_expires_at") val refreshExpiresAt: String? = null
)

@Serializable
data class RefreshTokenRequest(
    @SerialName("refresh_token") val refreshToken: String
)

@Serializable
data class RefreshTokenResponse(
    @SerialName("ok") val ok: Boolean = true,
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("refresh_expires_at") val refreshExpiresAt: String? = null
)
