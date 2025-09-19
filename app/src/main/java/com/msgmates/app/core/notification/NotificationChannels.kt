package com.msgmates.app.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.msgmates.app.R

object NotificationChannels {

    const val CHANNEL_CALLS = "calls"
    const val CHANNEL_MESSAGES = "messages"
    const val CHANNEL_GENERAL = "general"

    fun createAllChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Calls channel - HIGH importance for incoming calls
            val callsChannel = NotificationChannel(
                CHANNEL_CALLS,
                context.getString(R.string.notif_channel_calls),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notif_channel_calls_description)
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            // Messages channel - DEFAULT importance
            val messagesChannel = NotificationChannel(
                CHANNEL_MESSAGES,
                context.getString(R.string.notif_channel_messages),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notif_channel_messages_description)
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
            }

            // General channel - LOW importance
            val generalChannel = NotificationChannel(
                CHANNEL_GENERAL,
                context.getString(R.string.notif_channel_general),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.notif_channel_general_description)
                enableVibration(false)
                enableLights(false)
                setShowBadge(false)
            }

            notificationManager.createNotificationChannels(
                listOf(
                    callsChannel,
                    messagesChannel,
                    generalChannel
                )
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCallsChannel(context: Context): NotificationChannel? {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.getNotificationChannel(CHANNEL_CALLS)
    }
}
