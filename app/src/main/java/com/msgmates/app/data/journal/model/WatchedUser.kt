package com.msgmates.app.data.journal.model

data class WatchedUser(
    val userId: String,
    val userName: String,
    val profileImageUrl: String,
    val lastWatchedAt: Long
)
