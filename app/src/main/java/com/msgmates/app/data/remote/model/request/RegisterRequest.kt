package com.msgmates.app.data.remote.model.request

data class RegisterRequest(
    val username: String,
    val phoneNumber: String,
    val password: String,
    val deviceId: String
)
