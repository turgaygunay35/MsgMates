package com.msgmates.app.config

object AppConfig {
    // API Configuration - Use BuildConfig for consistency
    val BASE_URL: String get() = com.msgmates.app.BuildConfig.BASE_URL
    const val API_VERSION = "v1"
    const val TIMEOUT_SECONDS = 30L

    // App Configuration
    const val APP_NAME = "MsgMates"
    const val VERSION_NAME = "1.0.0"
    const val VERSION_CODE = 1

    // Database Configuration
    const val DATABASE_NAME = "msgmates.db"
    const val DATABASE_VERSION = 1

    // Preferences Keys
    const val PREF_USER_TOKEN = "user_token"
    const val PREF_USER_ID = "user_id"
    const val PREF_THEME_MODE = "theme_mode"
    const val PREF_DISASTER_MODE = "disaster_mode"
    const val PREF_LAST_ALIVE_TIME = "last_alive_time"

    // Disaster Mode Configuration
    const val ALIVE_COOLDOWN_SECONDS = 60L
    const val BLE_SCAN_INTERVAL_MS = 5000L
    const val BLE_ADVERTISE_INTERVAL_MS = 1000L

    // Journal Configuration
    const val MAX_VIDEO_DURATION_SECONDS = 30
    const val MAX_VIDEO_SIZE_MB = 50

    // File Upload Configuration
    const val MAX_FILE_SIZE_MB = 100
    const val ALLOWED_IMAGE_TYPES = "jpg,jpeg,png,gif"
    const val ALLOWED_VIDEO_TYPES = "mp4,avi,mov"
    const val ALLOWED_DOCUMENT_TYPES = "pdf,doc,docx,txt"

    // Network Configuration
    const val RETRY_COUNT = 3
    const val RETRY_DELAY_MS = 1000L

    // Security Configuration
    const val PASSWORD_MIN_LENGTH = 8
    const val MAX_LOGIN_ATTEMPTS = 5
    const val LOCKOUT_DURATION_MINUTES = 15
}
