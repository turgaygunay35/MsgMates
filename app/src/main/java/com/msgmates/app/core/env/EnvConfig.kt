package com.msgmates.app.core.env

import kotlinx.serialization.Serializable

@Serializable
data class EnvConfig(
    val baseUrl: String = "",
    val wsUrl: String = "",
    val auth: AuthConfig = AuthConfig(),
    val timeouts: TimeoutConfig = TimeoutConfig(),
    val tls: TlsConfig = TlsConfig(),
    val wsCfg: WsConfig = WsConfig()
)

@Serializable
data class AuthConfig(
    val loginStart: String = "",
    val verifyOtp: String = "",
    val loginPassword: String = "",
    val refresh: String = "",
    val revoke: String = "",
    val registerDevice: String = ""
)

@Serializable
data class TimeoutConfig(
    val connect: Int = 30000,
    val read: Int = 30000,
    val write: Int = 30000
)

@Serializable
data class TlsConfig(
    val certificatePins: List<String> = emptyList()
)

@Serializable
data class WsConfig(
    val pingInterval: Int = 30000,
    val reconnectDelay: Int = 5000,
    val maxReconnectAttempts: Int = 5
)
