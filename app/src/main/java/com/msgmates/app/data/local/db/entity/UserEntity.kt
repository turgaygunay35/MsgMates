package com.msgmates.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
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
