package com.msgmates.app.data.settings

import com.msgmates.app.data.local.prefs.SettingsPreferences
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class SettingsRepository @Inject constructor(
    private val settingsPreferences: SettingsPreferences
) {

    // Profile Settings
    fun getProfileName(): Flow<String> = settingsPreferences.profileName
    suspend fun setProfileName(name: String) = settingsPreferences.setProfileName(name)

    fun getProfileBio(): Flow<String> = settingsPreferences.profileBio
    suspend fun setProfileBio(bio: String) = settingsPreferences.setProfileBio(bio)

    fun getProfilePhone(): Flow<String> = settingsPreferences.profilePhone
    suspend fun setProfilePhone(phone: String) = settingsPreferences.setProfilePhone(phone)

    fun getProfileEmail(): Flow<String> = settingsPreferences.profileEmail
    suspend fun setProfileEmail(email: String) = settingsPreferences.setProfileEmail(email)

    // Notification Settings
    fun getNotificationEnabled(): Flow<Boolean> = settingsPreferences.notificationEnabled
    suspend fun setNotificationEnabled(enabled: Boolean) = settingsPreferences.setNotificationEnabled(enabled)

    fun getMessageNotificationEnabled(): Flow<Boolean> = settingsPreferences.messageNotificationEnabled
    suspend fun setMessageNotificationEnabled(enabled: Boolean) = settingsPreferences.setMessageNotificationEnabled(
        enabled
    )

    fun getCallNotificationEnabled(): Flow<Boolean> = settingsPreferences.callNotificationEnabled
    suspend fun setCallNotificationEnabled(enabled: Boolean) = settingsPreferences.setCallNotificationEnabled(enabled)

    fun getVibrationEnabled(): Flow<Boolean> = settingsPreferences.vibrationEnabled
    suspend fun setVibrationEnabled(enabled: Boolean) = settingsPreferences.setVibrationEnabled(enabled)

    fun getSoundEnabled(): Flow<Boolean> = settingsPreferences.soundEnabled
    suspend fun setSoundEnabled(enabled: Boolean) = settingsPreferences.setSoundEnabled(enabled)

    // Privacy Settings
    fun getLastSeenEnabled(): Flow<Boolean> = settingsPreferences.lastSeenEnabled
    suspend fun setLastSeenEnabled(enabled: Boolean) = settingsPreferences.setLastSeenEnabled(enabled)

    fun getReadReceiptsEnabled(): Flow<Boolean> = settingsPreferences.readReceiptsEnabled
    suspend fun setReadReceiptsEnabled(enabled: Boolean) = settingsPreferences.setReadReceiptsEnabled(enabled)

    fun getTypingIndicatorEnabled(): Flow<Boolean> = settingsPreferences.typingIndicatorEnabled
    suspend fun setTypingIndicatorEnabled(enabled: Boolean) = settingsPreferences.setTypingIndicatorEnabled(enabled)

    fun getOnlineStatusEnabled(): Flow<Boolean> = settingsPreferences.onlineStatusEnabled
    suspend fun setOnlineStatusEnabled(enabled: Boolean) = settingsPreferences.setOnlineStatusEnabled(enabled)

    // Security Settings
    fun getBiometricEnabled(): Flow<Boolean> = settingsPreferences.biometricEnabled
    suspend fun setBiometricEnabled(enabled: Boolean) = settingsPreferences.setBiometricEnabled(enabled)

    fun getAutoLockEnabled(): Flow<Boolean> = settingsPreferences.autoLockEnabled
    suspend fun setAutoLockEnabled(enabled: Boolean) = settingsPreferences.setAutoLockEnabled(enabled)

    fun getAutoLockTimeout(): Flow<Int> = settingsPreferences.autoLockTimeout
    suspend fun setAutoLockTimeout(timeout: Int) = settingsPreferences.setAutoLockTimeout(timeout)

    fun getScreenCaptureEnabled(): Flow<Boolean> = settingsPreferences.screenCaptureEnabled
    suspend fun setScreenCaptureEnabled(enabled: Boolean) = settingsPreferences.setScreenCaptureEnabled(enabled)

    // Appearance Settings
    fun getThemeMode(): Flow<String> = settingsPreferences.themeMode
    suspend fun setThemeMode(mode: String) = settingsPreferences.setThemeMode(mode)

    fun getLanguage(): Flow<String> = settingsPreferences.language
    suspend fun setLanguage(language: String) = settingsPreferences.setLanguage(language)

    fun getFontSize(): Flow<String> = settingsPreferences.fontSize
    suspend fun setFontSize(size: String) = settingsPreferences.setFontSize(size)

    fun getAccentColor(): Flow<String> = settingsPreferences.accentColor
    suspend fun setAccentColor(color: String) = settingsPreferences.setAccentColor(color)

    // App Settings
    fun getAutoDownloadEnabled(): Flow<Boolean> = settingsPreferences.autoDownloadEnabled
    suspend fun setAutoDownloadEnabled(enabled: Boolean) = settingsPreferences.setAutoDownloadEnabled(enabled)

    fun getDataUsageMode(): Flow<String> = settingsPreferences.dataUsageMode
    suspend fun setDataUsageMode(mode: String) = settingsPreferences.setDataUsageMode(mode)

    fun getCacheSize(): Flow<Long> = settingsPreferences.cacheSize
    suspend fun setCacheSize(size: Long) = settingsPreferences.setCacheSize(size)

    // General Settings
    fun getPhotoWifiDownload(): Flow<Boolean> = settingsPreferences.photoWifiDownload
    suspend fun setPhotoWifiDownload(enabled: Boolean) = settingsPreferences.setPhotoWifiDownload(enabled)

    fun getPhotoMobileDownload(): Flow<Boolean> = settingsPreferences.photoMobileDownload
    suspend fun setPhotoMobileDownload(enabled: Boolean) = settingsPreferences.setPhotoMobileDownload(enabled)

    fun getVideoWifiDownload(): Flow<Boolean> = settingsPreferences.videoWifiDownload
    suspend fun setVideoWifiDownload(enabled: Boolean) = settingsPreferences.setVideoWifiDownload(enabled)

    fun getVideoMobileDownload(): Flow<Boolean> = settingsPreferences.videoMobileDownload
    suspend fun setVideoMobileDownload(enabled: Boolean) = settingsPreferences.setVideoMobileDownload(enabled)

    fun getAudioWifiDownload(): Flow<Boolean> = settingsPreferences.audioWifiDownload
    suspend fun setAudioWifiDownload(enabled: Boolean) = settingsPreferences.setAudioWifiDownload(enabled)

    fun getAudioMobileDownload(): Flow<Boolean> = settingsPreferences.audioMobileDownload
    suspend fun setAudioMobileDownload(enabled: Boolean) = settingsPreferences.setAudioMobileDownload(enabled)

    fun getBackupReminder(): Flow<Boolean> = settingsPreferences.backupReminder
    suspend fun setBackupReminder(enabled: Boolean) = settingsPreferences.setBackupReminder(enabled)

    // Additional notification settings
    fun getMessageNotificationsFlow(): Flow<Boolean> = settingsPreferences.messageNotificationEnabled
    suspend fun setMessageNotifications(enabled: Boolean) = settingsPreferences.setMessageNotificationEnabled(enabled)

    fun getCallNotificationsFlow(): Flow<Boolean> = settingsPreferences.callNotificationEnabled
    suspend fun setCallNotifications(enabled: Boolean) = settingsPreferences.setCallNotificationEnabled(enabled)

    fun getSilentHoursStart(): Flow<String> = settingsPreferences.silentHoursStart
    suspend fun setSilentHoursStart(time: String) = settingsPreferences.setSilentHoursStart(time)

    fun getSilentHoursEnd(): Flow<String> = settingsPreferences.silentHoursEnd
    suspend fun setSilentHoursEnd(time: String) = settingsPreferences.setSilentHoursEnd(time)

    fun getCustomRingtone(): Flow<String> = settingsPreferences.customRingtone
    suspend fun setCustomRingtone(ringtone: String) = settingsPreferences.setCustomRingtone(ringtone)

    fun getNotificationContent(): Flow<String> = settingsPreferences.notificationContent
    suspend fun setNotificationContent(content: String) = settingsPreferences.setNotificationContent(content)

    // Additional theme settings
    fun getColorTheme(): Flow<String> = settingsPreferences.colorTheme
    suspend fun setColorTheme(theme: String) = settingsPreferences.setColorTheme(theme)

    fun getBubbleDensity(): Flow<String> = settingsPreferences.bubbleDensity
    suspend fun setBubbleDensity(density: String) = settingsPreferences.setBubbleDensity(density)

    // Statistics
    fun getSentMessagesCount(): Flow<Int> = settingsPreferences.sentMessagesCount
    suspend fun setSentMessagesCount(count: Int) = settingsPreferences.setSentMessagesCount(count)

    fun getReceivedMessagesCount(): Flow<Int> = settingsPreferences.receivedMessagesCount
    suspend fun setReceivedMessagesCount(count: Int) = settingsPreferences.setReceivedMessagesCount(count)

    fun getTextStorageUsed(): Flow<Long> = settingsPreferences.textStorageUsed
    suspend fun setTextStorageUsed(size: Long) = settingsPreferences.setTextStorageUsed(size)

    fun getMediaStorageUsed(): Flow<Long> = settingsPreferences.mediaStorageUsed
    suspend fun setMediaStorageUsed(size: Long) = settingsPreferences.setMediaStorageUsed(size)

    fun getContactCount(): Flow<Int> = settingsPreferences.contactCount
    suspend fun setContactCount(count: Int) = settingsPreferences.setContactCount(count)

    fun getGroupCount(): Flow<Int> = settingsPreferences.groupCount
    suspend fun setGroupCount(count: Int) = settingsPreferences.setGroupCount(count)

    fun getLastBackupDate(): Flow<String> = settingsPreferences.lastBackupDate
    suspend fun setLastBackupDate(date: String) = settingsPreferences.setLastBackupDate(date)

    // Logout settings
    fun getCloseServerSessions(): Flow<Boolean> = settingsPreferences.closeServerSessions
    suspend fun setCloseServerSessions(close: Boolean) = settingsPreferences.setCloseServerSessions(close)

    // Utility methods
    suspend fun clearUserData() {
        // Clear all user data
        settingsPreferences.clearAll()
    }

    suspend fun closeServerSessions() {
        // Close all server sessions
        // TODO: Implement server session closing
    }
}
