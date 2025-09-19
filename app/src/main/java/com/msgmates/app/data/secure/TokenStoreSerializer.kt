package com.msgmates.app.data.secure

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.crypto.tink.Aead
import com.msgmates.app.core.logging.StructuredLogger
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenStoreSerializer @Inject constructor(
    private val aead: Aead
) : Serializer<TokenStore> {

    override val defaultValue: TokenStore = TokenStore.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): TokenStore {
        return try {
            val bytes = input.readBytes()
            if (bytes.isEmpty()) {
                defaultValue
            } else {
                // bytes is actual ciphertext produced by writeTo
                val plain = aead.decrypt(bytes, /* associatedData */ null)
                TokenStore.parseFrom(plain)
            }
        } catch (e: Exception) {
            StructuredLogger.logEvent(
                "ERROR",
                "TOKEN_STORE_DECRYPT_FAIL",
                "Failed to decrypt token store: ${e.message}"
            )
            throw CorruptionException("Cannot read proto.", e)
        }
    }

    override suspend fun writeTo(t: TokenStore, output: OutputStream) {
        try {
            val plain = t.toByteArray()
            val cipher = aead.encrypt(plain, /* associatedData */ null)
            output.write(cipher)
        } catch (e: Exception) {
            StructuredLogger.logEvent(
                "ERROR",
                "TOKEN_STORE_ENCRYPT_FAIL",
                "Failed to encrypt token store: ${e.message}"
            )
            throw e
        }
    }
}
