package com.msgmates.app.ui.common

enum class ConnectionStatus { LIVE, SYNC, OFFLINE, DISASTER }

data class StatusCapsuleUi(
    val status: ConnectionStatus,
    val label: String,
    val iconRes: Int
)
