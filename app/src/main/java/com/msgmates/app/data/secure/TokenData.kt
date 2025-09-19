package com.msgmates.app.data.secure

data class TokenData(
    val accessTokenCiphertext: ByteArray,
    val refreshTokenCiphertext: ByteArray,
    val issuedAtMs: Long,
    val expiresAtMs: Long,
    val keyVersion: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TokenData

        if (!accessTokenCiphertext.contentEquals(other.accessTokenCiphertext)) return false
        if (!refreshTokenCiphertext.contentEquals(other.refreshTokenCiphertext)) return false
        if (issuedAtMs != other.issuedAtMs) return false
        if (expiresAtMs != other.expiresAtMs) return false
        if (keyVersion != other.keyVersion) return false

        return true
    }

    override fun hashCode(): Int {
        var result = accessTokenCiphertext.contentHashCode()
        result = 31 * result + refreshTokenCiphertext.contentHashCode()
        result = 31 * result + issuedAtMs.hashCode()
        result = 31 * result + expiresAtMs.hashCode()
        result = 31 * result + keyVersion
        return result
    }
}
