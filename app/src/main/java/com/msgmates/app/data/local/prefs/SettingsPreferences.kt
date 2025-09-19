package com.msgmates.app.data.local.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings_prefs")

@Singleton
class SettingsPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        // Profile Settings
        private val KEY_PROFILE_NAME = stringPreferencesKey("profile_name")
        private val KEY_PROFILE_BIO = stringPreferencesKey("profile_bio")
        private val KEY_PROFILE_PHONE = stringPreferencesKey("profile_phone")
        private val KEY_PROFILE_EMAIL = stringPreferencesKey("profile_email")

        // Notification Settings
        private val KEY_NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        private val KEY_MESSAGE_NOTIFICATION_ENABLED = booleanPreferencesKey("message_notification_enabled")
        private val KEY_CALL_NOTIFICATION_ENABLED = booleanPreferencesKey("call_notification_enabled")
        private val KEY_VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        private val KEY_SOUND_ENABLED = booleanPreferencesKey("sound_enabled")

        // Privacy Settings
        private val KEY_LAST_SEEN_ENABLED = booleanPreferencesKey("last_seen_enabled")
        private val KEY_READ_RECEIPTS_ENABLED = booleanPreferencesKey("read_receipts_enabled")
        private val KEY_TYPING_INDICATOR_ENABLED = booleanPreferencesKey("typing_indicator_enabled")
        private val KEY_ONLINE_STATUS_ENABLED = booleanPreferencesKey("online_status_enabled")

        // Security Settings
        private val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        private val KEY_AUTO_LOCK_ENABLED = booleanPreferencesKey("auto_lock_enabled")
        private val KEY_AUTO_LOCK_TIMEOUT = intPreferencesKey("auto_lock_timeout")
        private val KEY_SCREEN_CAPTURE_ENABLED = booleanPreferencesKey("screen_capture_enabled")

        // Appearance Settings
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_LANGUAGE = stringPreferencesKey("language")
        private val KEY_FONT_SIZE = stringPreferencesKey("font_size")
        private val KEY_ACCENT_COLOR = stringPreferencesKey("accent_color")

        // App Settings
        private val KEY_AUTO_DOWNLOAD_ENABLED = booleanPreferencesKey("auto_download_enabled")
        private val KEY_DATA_USAGE_MODE = stringPreferencesKey("data_usage_mode")
        private val KEY_CACHE_SIZE = longPreferencesKey("cache_size")

        // General Settings
        private val KEY_PHOTO_WIFI_DOWNLOAD = booleanPreferencesKey("photo_wifi_download")
        private val KEY_PHOTO_MOBILE_DOWNLOAD = booleanPreferencesKey("photo_mobile_download")
        private val KEY_VIDEO_WIFI_DOWNLOAD = booleanPreferencesKey("video_wifi_download")
        private val KEY_VIDEO_MOBILE_DOWNLOAD = booleanPreferencesKey("video_mobile_download")
        private val KEY_AUDIO_WIFI_DOWNLOAD = booleanPreferencesKey("audio_wifi_download")
        private val KEY_AUDIO_MOBILE_DOWNLOAD = booleanPreferencesKey("audio_mobile_download")
        private val KEY_BACKUP_REMINDER = booleanPreferencesKey("backup_reminder")

        // Additional notification settings
        private val KEY_SILENT_HOURS_START = stringPreferencesKey("silent_hours_start")
        private val KEY_SILENT_HOURS_END = stringPreferencesKey("silent_hours_end")
        private val KEY_CUSTOM_RINGTONE = stringPreferencesKey("custom_ringtone")
        private val KEY_NOTIFICATION_CONTENT = stringPreferencesKey("notification_content")

        // Additional theme settings
        private val KEY_COLOR_THEME = stringPreferencesKey("color_theme")
        private val KEY_BUBBLE_DENSITY = stringPreferencesKey("bubble_density")

        // Statistics
        private val KEY_SENT_MESSAGES_COUNT = intPreferencesKey("sent_messages_count")
        private val KEY_RECEIVED_MESSAGES_COUNT = intPreferencesKey("received_messages_count")
        private val KEY_TEXT_STORAGE_USED = longPreferencesKey("text_storage_used")
        private val KEY_MEDIA_STORAGE_USED = longPreferencesKey("media_storage_used")
        private val KEY_CONTACT_COUNT = intPreferencesKey("contact_count")
        private val KEY_GROUP_COUNT = intPreferencesKey("group_count")
        private val KEY_LAST_BACKUP_DATE = stringPreferencesKey("last_backup_date")

        // Logout settings
        private val KEY_CLOSE_SERVER_SESSIONS = booleanPreferencesKey("close_server_sessions")
    }

    // Profile Settings
    val profileName: Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_PROFILE_NAME] ?: ""
    }

    suspend fun setProfileName(name: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_PROFILE_NAME] = name
        }
    }

    val profileBio: Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_PROFILE_BIO] ?: ""
    }

    suspend fun setProfileBio(bio: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_PROFILE_BIO] = bio
        }
    }

    val profilePhone: Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_PROFILE_PHONE] ?: ""
    }

    suspend fun setProfilePhone(phone: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_PROFILE_PHONE] = phone
        }
    }

    val profileEmail: Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_PROFILE_EMAIL] ?: ""
    }

    suspend fun setProfileEmail(email: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_PROFILE_EMAIL] = email
        }
    }

    // Notification Settings
    val notificationEnabled: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_NOTIFICATION_ENABLED] ?: true
    }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_NOTIFICATION_ENABLED] = enabled
        }
    }

    val messageNotificationEnabled: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_MESSAGE_NOTIFICATION_ENABLED] ?: true
    }

    suspend fun setMessageNotificationEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_MESSAGE_NOTIFICATION_ENABLED] = enabled
        }
    }

    val callNotificationEnabled: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_CALL_NOTIFICATION_ENABLED] ?: true
    }

    suspend fun setCallNotificationEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_CALL_NOTIFICATION_ENABLED] = enabled
        }
    }

    val vibrationEnabled: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_VIBRATION_ENABLED] ?: true
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_VIBRATION_ENABLED] = enabled
        }
    }

    val soundEnabled: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_SOUND_ENABLED] ?: true
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_SOUND_ENABLED] = enabled
        }
    }

    // Privacy Settings
    val lastSeenEnabled: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_LAST_SEEN_ENABLED] ?: true
    }

    suspend fun setLastSeenEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_LAST_SEEN_ENABLED] = enabled
        }
    }

    val readReceiptsEnabled: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_READ_RECEIPTS_ENABLED] ?: true
    }

    suspend fun setReadReceiptsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_READ_RECEIPTS_ENABLED] = enabled
        }
    }

    val typingIndicatorEnabled: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_TYPING_INDICATOR_ENABLED] ?: true
    }

    suspend fun setTypingIndicatorEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_TYPING_INDICATOR_ENABLED] = enabled
        }
    }

    val onlineStatusEnabled: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_ONLINE_STATUS_ENABLED] ?: true
    }

    suspend fun setOnlineStatusEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_ONLINE_STATUS_ENABLED] = enabled
        }
    }

    // Security Settings
    val biometricEnabled: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_BIOMETRIC_ENABLED] ?: false
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_BIOMETRIC_ENABLED] = enabled
        }
    }

    val autoLockEnabled: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_AUTO_LOCK_ENABLED] ?: false
    }

    suspend fun setAutoLockEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_AUTO_LOCK_ENABLED] = enabled
        }
    }

    val autoLockTimeout: Flow<Int> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_AUTO_LOCK_TIMEOUT] ?: 5
    }

    suspend fun setAutoLockTimeout(timeout: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_AUTO_LOCK_TIMEOUT] = timeout
        }
    }

    val screenCaptureEnabled: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_SCREEN_CAPTURE_ENABLED] ?: true
    }

    suspend fun setScreenCaptureEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_SCREEN_CAPTURE_ENABLED] = enabled
        }
    }

    // Appearance Settings
    val themeMode: Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_THEME_MODE] ?: "system"
    }

    suspend fun setThemeMode(mode: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode
        }
    }

    val language: Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_LANGUAGE] ?: "tr"
    }

    suspend fun setLanguage(language: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_LANGUAGE] = language
        }
    }

    val fontSize: Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_FONT_SIZE] ?: "medium"
    }

    suspend fun setFontSize(size: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_FONT_SIZE] = size
        }
    }

    val accentColor: Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_ACCENT_COLOR] ?: "blue"
    }

    suspend fun setAccentColor(color: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_ACCENT_COLOR] = color
        }
    }

    // App Settings
    val autoDownloadEnabled: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_AUTO_DOWNLOAD_ENABLED] ?: true
    }

    suspend fun setAutoDownloadEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_AUTO_DOWNLOAD_ENABLED] = enabled
        }
    }

    val dataUsageMode: Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_DATA_USAGE_MODE] ?: "wifi"
    }

    suspend fun setDataUsageMode(mode: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_DATA_USAGE_MODE] = mode
        }
    }

    val cacheSize: Flow<Long> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_CACHE_SIZE] ?: 0L
    }

    suspend fun setCacheSize(size: Long) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_CACHE_SIZE] = size
        }
    }

    // General Settings
    val photoWifiDownload: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_PHOTO_WIFI_DOWNLOAD] ?: true
    }

    suspend fun setPhotoWifiDownload(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_PHOTO_WIFI_DOWNLOAD] = enabled
        }
    }

    val photoMobileDownload: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_PHOTO_MOBILE_DOWNLOAD] ?: false
    }

    suspend fun setPhotoMobileDownload(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_PHOTO_MOBILE_DOWNLOAD] = enabled
        }
    }

    val videoWifiDownload: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_VIDEO_WIFI_DOWNLOAD] ?: true
    }

    suspend fun setVideoWifiDownload(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_VIDEO_WIFI_DOWNLOAD] = enabled
        }
    }

    val videoMobileDownload: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_VIDEO_MOBILE_DOWNLOAD] ?: false
    }

    suspend fun setVideoMobileDownload(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_VIDEO_MOBILE_DOWNLOAD] = enabled
        }
    }

    val audioWifiDownload: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_AUDIO_WIFI_DOWNLOAD] ?: true
    }

    suspend fun setAudioWifiDownload(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_AUDIO_WIFI_DOWNLOAD] = enabled
        }
    }

    val audioMobileDownload: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_AUDIO_MOBILE_DOWNLOAD] ?: true
    }

    suspend fun setAudioMobileDownload(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_AUDIO_MOBILE_DOWNLOAD] = enabled
        }
    }

    val backupReminder: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_BACKUP_REMINDER] ?: true
    }

    suspend fun setBackupReminder(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_BACKUP_REMINDER] = enabled
        }
    }

    // Additional notification settings
    val silentHoursStart: Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_SILENT_HOURS_START] ?: "22:00"
    }

    suspend fun setSilentHoursStart(time: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_SILENT_HOURS_START] = time
        }
    }

    val silentHoursEnd: Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_SILENT_HOURS_END] ?: "08:00"
    }

    suspend fun setSilentHoursEnd(time: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_SILENT_HOURS_END] = time
        }
    }

    val customRingtone: Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_CUSTOM_RINGTONE] ?: "default"
    }

    suspend fun setCustomRingtone(ringtone: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_CUSTOM_RINGTONE] = ringtone
        }
    }

    val notificationContent: Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_NOTIFICATION_CONTENT] ?: "name_and_message"
    }

    suspend fun setNotificationContent(content: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_NOTIFICATION_CONTENT] = content
        }
    }

    // Additional theme settings
    val colorTheme: Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_COLOR_THEME] ?: "blue"
    }

    suspend fun setColorTheme(theme: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_COLOR_THEME] = theme
        }
    }

    val bubbleDensity: Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_BUBBLE_DENSITY] ?: "medium"
    }

    suspend fun setBubbleDensity(density: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_BUBBLE_DENSITY] = density
        }
    }

    // Statistics
    val sentMessagesCount: Flow<Int> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_SENT_MESSAGES_COUNT] ?: 0
    }

    suspend fun setSentMessagesCount(count: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_SENT_MESSAGES_COUNT] = count
        }
    }

    val receivedMessagesCount: Flow<Int> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_RECEIVED_MESSAGES_COUNT] ?: 0
    }

    suspend fun setReceivedMessagesCount(count: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_RECEIVED_MESSAGES_COUNT] = count
        }
    }

    val textStorageUsed: Flow<Long> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_TEXT_STORAGE_USED] ?: 0L
    }

    suspend fun setTextStorageUsed(size: Long) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_TEXT_STORAGE_USED] = size
        }
    }

    val mediaStorageUsed: Flow<Long> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_MEDIA_STORAGE_USED] ?: 0L
    }

    suspend fun setMediaStorageUsed(size: Long) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_MEDIA_STORAGE_USED] = size
        }
    }

    val contactCount: Flow<Int> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_CONTACT_COUNT] ?: 0
    }

    suspend fun setContactCount(count: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_CONTACT_COUNT] = count
        }
    }

    val groupCount: Flow<Int> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_GROUP_COUNT] ?: 0
    }

    suspend fun setGroupCount(count: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_GROUP_COUNT] = count
        }
    }

    val lastBackupDate: Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_LAST_BACKUP_DATE] ?: "Hiç yedeklenmedi"
    }

    suspend fun setLastBackupDate(date: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_LAST_BACKUP_DATE] = date
        }
    }

    // Logout settings
    val closeServerSessions: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_CLOSE_SERVER_SESSIONS] ?: false
    }

    suspend fun setCloseServerSessions(close: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_CLOSE_SERVER_SESSIONS] = close
        }
    }

    // Utility methods
    suspend fun clearAll() {
        context.settingsDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
