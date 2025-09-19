package com.msgmates.app.security

import com.msgmates.app.BuildConfig

class DebugAuthController {

    fun isEnabled(): Boolean = BuildConfig.ENABLE_PASSWORD_BYPASS

    fun codeFor(user: String): String {
        // Stabil deterministik bir kod üretimi (yalın)
        val base = (user + "|msgmates").hashCode()
        val num = (base and 0x7fffffff) % 1000000
        return num.toString().padStart(6, '0')
    }

    fun verify(user: String, code: String): Boolean =
        code == codeFor(user)
}
