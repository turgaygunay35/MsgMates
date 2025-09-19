package com.msgmates.app.core.auth

import kotlinx.serialization.Serializable

@Serializable
data class AccessToken(
    val value: String,
    val expiresAtEpochSec: Long, // UNIX epoch seconds
    val scope: String? = null
)

@Serializable
data class RefreshToken(
    val value: String,
    val expiresAtEpochSec: Long? = null
)

data class AuthTokens(
    val access: AccessToken?,
    val refresh: RefreshToken?
)

object TokenKeys {
    const val PREF_FILE = "secure_auth_prefs"
    const val K_ACCESS = "access_token"
    const val K_ACCESS_EXP = "access_token_exp"
    const val K_REFRESH = "refresh_token"
    const val K_REFRESH_EXP = "refresh_token_exp"
    const val K_DEVICE_ID = "device_id"
    const val K_PUSH_TOKEN = "push_token"
}
