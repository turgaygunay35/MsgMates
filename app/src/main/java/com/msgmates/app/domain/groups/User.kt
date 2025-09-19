package com.msgmates.app.domain.groups

data class User(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val avatarUrl: String? = null,
    val isOnline: Boolean = false,
    val lastSeen: String? = null
)
