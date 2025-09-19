package com.msgmates.app.data.secure

import android.content.Context
import com.msgmates.app.core.logging.StructuredLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class TokenMigrationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val secureTokenStore: SecureTokenStore
) {
    private val oldPrefsName = "auth_tokens"
    private val accessKey = "access_token"
    private val refreshKey = "refresh_token"
    private val issuedAtKey = "issued_at"
    private val expiresAtKey = "expires_at"
    private val keyVersionKey = "key_version"

    suspend fun migrateIfNeeded() = withContext(Dispatchers.IO) {
        try {
            val existing = secureTokenStore.getTokenData()
            if (existing != null && existing.accessTokenCiphertext.isNotEmpty()) {
                // Already migrated or already present — nothing to do
                StructuredLogger.logEvent("DEBUG", "TOKEN_MIGRATION_SKIP", "Migration skipped - data already exists")
                return@withContext
            }

            val prefs = context.getSharedPreferences(oldPrefsName, Context.MODE_PRIVATE)
            val access = prefs.getString(accessKey, null)
            val refresh = prefs.getString(refreshKey, null)

            if (access != null && refresh != null) {
                StructuredLogger.logEvent("INFO", "TOKEN_MIGRATION_START", "Starting migration from old storage")

                val tokenData = TokenData(
                    accessTokenCiphertext = access.toByteArray(), // Plaintext -> will be encrypted by DataStore
                    refreshTokenCiphertext = refresh.toByteArray(),
                    issuedAtMs = prefs.getLong(issuedAtKey, 0L),
                    expiresAtMs = prefs.getLong(expiresAtKey, 0L),
                    keyVersion = prefs.getInt(keyVersionKey, 1)
                )

                secureTokenStore.saveTokenData(tokenData)

                // Wipe old storage
                prefs.edit().clear().apply()

                StructuredLogger.logEvent("INFO", "TOKEN_MIGRATION_OK", "Migration completed successfully")
            } else {
                StructuredLogger.logEvent("DEBUG", "TOKEN_MIGRATION_NO_DATA", "No old data to migrate")
            }
        } catch (e: Exception) {
            StructuredLogger.logEvent("ERROR", "TOKEN_MIGRATION_FAIL", "Migration failed: ${e.message}")
            // Don't throw - migration failure shouldn't crash the app
        }
    }
}
