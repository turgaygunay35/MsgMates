package com.msgmates.app.data.secure

import androidx.datastore.core.DataStore
import com.google.protobuf.ByteString
import com.msgmates.app.core.logging.StructuredLogger
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class DataStoreTokenStore @Inject constructor(
    private val dataStore: DataStore<TokenStore>
) : SecureTokenStore {

    private val mutex = Mutex()

    override suspend fun getTokenData(): TokenData? = mutex.withLock {
        try {
            val proto = dataStore.data.first()
            if (proto.accessToken.isEmpty && proto.refreshToken.isEmpty) {
                return null
            }

            return TokenData(
                accessTokenCiphertext = proto.accessToken.toByteArray(),
                refreshTokenCiphertext = proto.refreshToken.toByteArray(),
                issuedAtMs = proto.issuedAtEpochMs,
                expiresAtMs = proto.expiresAtEpochMs,
                keyVersion = proto.keyVersion
            )
        } catch (e: Exception) {
            StructuredLogger.logEvent("ERROR", "TOKEN_STORE_READ_FAIL", "Failed to read token data: ${e.message}")
            return null
        }
    }

    override suspend fun saveTokenData(tokenData: TokenData) = mutex.withLock {
        try {
            dataStore.updateData { current ->
                current.toBuilder()
                    .setAccessToken(ByteString.copyFrom(tokenData.accessTokenCiphertext))
                    .setRefreshToken(ByteString.copyFrom(tokenData.refreshTokenCiphertext))
                    .setIssuedAtEpochMs(tokenData.issuedAtMs)
                    .setExpiresAtEpochMs(tokenData.expiresAtMs)
                    .setKeyVersion(tokenData.keyVersion)
                    .build()
            }
            StructuredLogger.logEvent("INFO", "TOKEN_STORE_SAVE", "Token data saved securely")
        } catch (e: Exception) {
            StructuredLogger.logEvent("ERROR", "TOKEN_STORE_SAVE_FAIL", "Failed to save token data: ${e.message}")
            throw e
        }
    }

    override suspend fun clear() = mutex.withLock {
        try {
            dataStore.updateData { current ->
                current.toBuilder()
                    .clearAccessToken()
                    .clearRefreshToken()
                    .setIssuedAtEpochMs(0L)
                    .setExpiresAtEpochMs(0L)
                    .setKeyVersion(0)
                    .build()
            }
            StructuredLogger.logEvent("INFO", "TOKEN_STORE_CLEAR", "Token data cleared")
        } catch (e: Exception) {
            StructuredLogger.logEvent("ERROR", "TOKEN_STORE_CLEAR_FAIL", "Failed to clear token data: ${e.message}")
            throw e
        }
    }
}
