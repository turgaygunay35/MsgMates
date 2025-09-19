package com.msgmates.app.data.secure

import android.content.Context
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.aead.AeadKeyTemplates
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CryptoModule {

    @Provides
    @Singleton
    fun provideAndroidKeysetManager(@ApplicationContext context: Context): AndroidKeysetManager {
        AeadConfig.register()

        return AndroidKeysetManager.Builder()
            .withSharedPref(context, "msgmates_master_key_pref", context.packageName)
            .withKeyTemplate(AeadKeyTemplates.AES256_GCM)
            .withMasterKeyUri("android-keystore://msgmates_master_key")
            .build()
    }

    @Provides
    @Singleton
    fun provideKeysetHandle(androidKeysetManager: AndroidKeysetManager): KeysetHandle =
        androidKeysetManager.keysetHandle

    @Provides
    @Singleton
    fun provideAead(keysetHandle: KeysetHandle): Aead =
        keysetHandle.getPrimitive(Aead::class.java)
}
