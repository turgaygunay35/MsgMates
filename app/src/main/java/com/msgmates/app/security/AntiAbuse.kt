package com.msgmates.app.security

import android.content.Context
import android.provider.Settings
import androidx.core.content.edit

class AntiAbuse(private val context: Context) {

    private val sp by lazy {
        context.getSharedPreferences("anti_abuse", Context.MODE_PRIVATE)
    }

    fun installationId(): String {
        // Gereksiz !! kaldırıldı; güvenli fallback ile dönüyoruz.
        val id = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        return id ?: ""
    }

    fun markAbuse(count: Int): Boolean {
        val total = sp.getInt("abuse_count", 0) + count
        sp.edit {
            putInt("abuse_count", total)
        }
        return total > 5
    }

    fun resetAbuse(): Boolean {
        sp.edit {
            putInt("abuse_count", 0)
        }
        return true
    }
}
