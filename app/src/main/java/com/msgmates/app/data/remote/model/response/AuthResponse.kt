package com.msgmates.app.data.remote.model.response

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserResponse,
    val expiresIn: Long
)

data class UserResponse(
    val id: String,
    val username: String,
    val email: String?,
    val phoneNumber: String,
    val profileImageUrl: String?,
    val isOnline: Boolean = false,
    val lastSeen: Long? = null,
    val isVerified: Boolean = false,
    val bio: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
