package com.msgmates.app.core.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.msgmates.app.core.logging.StructuredLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class SecureTokenStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val mutex = Mutex()

    // Migration from old SharedPreferences
    private val oldPrefs = context.getSharedPreferences("auth_tokens", Context.MODE_PRIVATE)

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "secure_tokens",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    suspend fun setTokens(access: String?, refresh: String?) {
        mutex.withLock {
            try {
                val now = System.currentTimeMillis()
                val expiresAt = now + (24 * 60 * 60 * 1000) // 24 hours default

                encryptedPrefs.edit().apply {
                    if (access != null) {
                        putString("access_token", access)
                        putLong("issued_at", now)
                        putLong("expires_at", expiresAt)
                    } else {
                        remove("access_token")
                        remove("issued_at")
                        remove("expires_at")
                    }

                    if (refresh != null) {
                        putString("refresh_token", refresh)
                    } else {
                        remove("refresh_token")
                    }

                    putInt("key_version", 1)
                    apply()
                }

                // Migrate from old storage if exists
                migrateFromOldStorage()

                StructuredLogger.logEvent("INFO", "TOKEN_STORE_SAVE", "Tokens saved securely")
            } catch (e: Exception) {
                StructuredLogger.logEvent("ERROR", "TOKEN_STORE_SAVE_FAIL", "Failed to save tokens: ${e.message}")
                throw e
            }
        }
    }

    suspend fun getAccessToken(): String? = mutex.withLock {
        try {
            encryptedPrefs.getString("access_token", null)
        } catch (e: Exception) {
            StructuredLogger.logEvent("ERROR", "TOKEN_STORE_READ_FAIL", "Failed to read access token: ${e.message}")
            null
        }
    }

    suspend fun getRefreshToken(): String? = mutex.withLock {
        try {
            encryptedPrefs.getString("refresh_token", null)
        } catch (e: Exception) {
            StructuredLogger.logEvent("ERROR", "TOKEN_STORE_READ_FAIL", "Failed to read refresh token: ${e.message}")
            null
        }
    }

    suspend fun clear() {
        mutex.withLock {
            try {
                encryptedPrefs.edit().clear().apply()

                // Clear old storage too
                oldPrefs.edit().clear().apply()

                StructuredLogger.logEvent("INFO", "TOKEN_STORE_CLEAR", "All tokens cleared")
            } catch (e: Exception) {
                StructuredLogger.logEvent("ERROR", "TOKEN_STORE_CLEAR_FAIL", "Failed to clear tokens: ${e.message}")
                throw e
            }
        }
    }

    suspend fun hasValidTokens(): Boolean {
        val access = getAccessToken()
        val refresh = getRefreshToken()
        return !access.isNullOrBlank() && !refresh.isNullOrBlank()
    }

    private suspend fun migrateFromOldStorage() {
        try {
            val oldAccess = oldPrefs.getString("access_token", null)
            val oldRefresh = oldPrefs.getString("refresh_token", null)

            if (oldAccess != null || oldRefresh != null) {
                StructuredLogger.logEvent("INFO", "TOKEN_MIGRATION_START", "Migrating from old storage")

                // Only migrate if new storage is empty
                val hasNewTokens = encryptedPrefs.getString("access_token", null) != null || encryptedPrefs.getString("refresh_token", null) != null

                if (!hasNewTokens) {
                    setTokens(oldAccess, oldRefresh)
                    oldPrefs.edit().clear().apply()
                    StructuredLogger.logEvent("INFO", "TOKEN_MIGRATION_OK", "Migration completed")
                }
            }
        } catch (e: Exception) {
            StructuredLogger.logEvent("ERROR", "TOKEN_MIGRATION_FAIL", "Migration failed: ${e.message}")
        }
    }
}
