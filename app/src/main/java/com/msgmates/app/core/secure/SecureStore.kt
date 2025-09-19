package com.msgmates.app.core.secure

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.msgmates.app.core.auth.AccessToken
import com.msgmates.app.core.auth.AuthTokens
import com.msgmates.app.core.auth.RefreshToken
import com.msgmates.app.core.auth.TokenKeys
import java.security.GeneralSecurityException

interface SecureStore {
    fun saveTokens(tokens: AuthTokens)
    fun readTokens(): AuthTokens
    fun saveDeviceId(id: String)
    fun getDeviceId(): String?
    fun savePushToken(token: String)
    fun getPushToken(): String?
    fun clearAll()
}

class SecureStoreImpl(private val context: Context) : SecureStore {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = try {
        EncryptedSharedPreferences.create(
            context,
            TokenKeys.PREF_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: GeneralSecurityException) {
        throw RuntimeException("Failed to create EncryptedSharedPreferences", e)
    }

    override fun saveTokens(tokens: AuthTokens) {
        val editor = sharedPreferences.edit()

        // Access token
        if (tokens.access != null) {
            editor.putString(TokenKeys.K_ACCESS, tokens.access.value)
            editor.putLong(TokenKeys.K_ACCESS_EXP, tokens.access.expiresAtEpochSec)
        } else {
            editor.remove(TokenKeys.K_ACCESS)
            editor.remove(TokenKeys.K_ACCESS_EXP)
        }

        // Refresh token
        if (tokens.refresh != null) {
            editor.putString(TokenKeys.K_REFRESH, tokens.refresh.value)
            tokens.refresh.expiresAtEpochSec?.let { exp ->
                editor.putLong(TokenKeys.K_REFRESH_EXP, exp)
            } ?: editor.remove(TokenKeys.K_REFRESH_EXP)
        } else {
            editor.remove(TokenKeys.K_REFRESH)
            editor.remove(TokenKeys.K_REFRESH_EXP)
        }

        editor.apply()
    }

    override fun readTokens(): AuthTokens {
        val accessValue = sharedPreferences.getString(TokenKeys.K_ACCESS, null)
        val accessExp = sharedPreferences.getLong(TokenKeys.K_ACCESS_EXP, -1)
        val refreshValue = sharedPreferences.getString(TokenKeys.K_REFRESH, null)
        val refreshExp = sharedPreferences.getLong(TokenKeys.K_REFRESH_EXP, -1)

        val accessToken = if (accessValue != null && accessExp != -1L) {
            AccessToken(
                value = accessValue,
                expiresAtEpochSec = accessExp
            )
        } else {
            null
        }

        val refreshToken = if (refreshValue != null) {
            RefreshToken(
                value = refreshValue,
                expiresAtEpochSec = if (refreshExp != -1L) refreshExp else null
            )
        } else {
            null
        }

        return AuthTokens(access = accessToken, refresh = refreshToken)
    }

    override fun saveDeviceId(id: String) {
        sharedPreferences.edit()
            .putString(TokenKeys.K_DEVICE_ID, id)
            .apply()
    }

    override fun getDeviceId(): String? {
        return sharedPreferences.getString(TokenKeys.K_DEVICE_ID, null)
    }

    override fun savePushToken(token: String) {
        sharedPreferences.edit()
            .putString(TokenKeys.K_PUSH_TOKEN, token)
            .apply()
    }

    override fun getPushToken(): String? {
        return sharedPreferences.getString(TokenKeys.K_PUSH_TOKEN, null)
    }

    override fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
}
