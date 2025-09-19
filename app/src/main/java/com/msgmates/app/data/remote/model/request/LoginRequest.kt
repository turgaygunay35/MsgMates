package com.msgmates.app.data.remote.model.request

data class LoginRequest(
    val phoneNumber: String,
    val password: String,
    val deviceId: String
)
