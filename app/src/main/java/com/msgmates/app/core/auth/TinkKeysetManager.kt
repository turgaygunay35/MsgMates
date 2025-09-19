package com.msgmates.app.core.auth

import android.content.Context
import com.google.crypto.tink.Aead
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AeadKeyTemplates
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TinkKeysetManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val _aead: Aead by lazy {
        AeadConfig.register()

        val keysetHandle = AndroidKeysetManager.Builder()
            .withSharedPref(context, "tink_keyset", "tink_prefs")
            .withKeyTemplate(AeadKeyTemplates.AES256_GCM)
            .withMasterKeyUri("android-keystore://tink_master_key")
            .build()
            .keysetHandle

        keysetHandle.getPrimitive(Aead::class.java)
    }

    fun getAead(): Aead = _aead

    fun getCurrentKeyVersion(): Int = 1 // TODO: Implement key rotation

    companion object {
        private const val MASTER_KEY_ALIAS = "tink_master_key"
    }
}
