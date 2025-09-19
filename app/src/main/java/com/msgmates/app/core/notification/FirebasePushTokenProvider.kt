package com.msgmates.app.core.notification

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebasePushTokenProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : PushTokenProvider {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("push_token_prefs", Context.MODE_PRIVATE)
    }

    override suspend fun current(): String? {
        // Şimdilik test değeri döndür
        // Gerçek uygulamada Firebase Cloud Messaging'den alınacak
        return prefs.getString("test_push_token", "test_token_12345")
    }

    fun setTestToken(token: String) {
        prefs.edit().putString("test_push_token", token).apply()
    }
}
