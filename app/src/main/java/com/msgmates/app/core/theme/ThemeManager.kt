package com.msgmates.app.core.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    private val THEME_KEY = "theme_mode"

    fun getCurrentTheme(): ThemeMode {
        val themeValue = prefs.getInt(THEME_KEY, ThemeMode.SYSTEM.ordinal)
        return ThemeMode.values()[themeValue]
    }

    fun setTheme(themeMode: ThemeMode) {
        prefs.edit().putInt(THEME_KEY, themeMode.ordinal).apply()
        applyTheme(themeMode)
    }

    fun toggleTheme() {
        val currentTheme = getCurrentTheme()
        val newTheme = when (currentTheme) {
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.SYSTEM -> ThemeMode.DARK
        }
        setTheme(newTheme)
    }

    private fun applyTheme(themeMode: ThemeMode) {
        val mode = when (themeMode) {
            ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    fun isDarkTheme(): Boolean {
        return getCurrentTheme() == ThemeMode.DARK
    }
}

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}
