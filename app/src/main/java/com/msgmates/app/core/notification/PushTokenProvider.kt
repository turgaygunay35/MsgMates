package com.msgmates.app.core.notification

interface PushTokenProvider { suspend fun current(): String? }
