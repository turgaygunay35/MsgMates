package com.msgmates.app.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages notification channels for the application
 */
@Singleton
class NotificationChannels @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val CHANNEL_CALLS = "calls"
        const val CHANNEL_MESSAGES = "messages"
        const val CHANNEL_DISASTER = "disaster"
    }

    fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Calls channel
            val callsChannel = NotificationChannel(
                CHANNEL_CALLS,
                "Aramalar",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Sesli ve görüntülü arama bildirimleri"
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            // Messages channel
            val messagesChannel = NotificationChannel(
                CHANNEL_MESSAGES,
                "Mesajlar",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Sohbet mesaj bildirimleri"
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
            }

            // Disaster channel
            val disasterChannel = NotificationChannel(
                CHANNEL_DISASTER,
                "Afet Modu",
                NotificationManager.IMPORTANCE_MAX
            ).apply {
                description = "Afet modu bildirimleri"
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            notificationManager.createNotificationChannels(
                listOf(callsChannel, messagesChannel, disasterChannel)
            )
        }
    }

    fun areNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun isChannelEnabled(channelId: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = notificationManager.getNotificationChannel(channelId)
            channel?.importance != NotificationManager.IMPORTANCE_NONE
        } else {
            true
        }
    }
}
