package com.msgmates.app.core.security

interface CaptchaGate { suspend fun getTokenOrNull(): String? }

class NoopCaptchaGate : CaptchaGate { override suspend fun getTokenOrNull() = null }
