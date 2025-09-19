package com.msgmates.app.data.repository

import android.content.Context
import com.msgmates.app.util.Notif

class NotificationsRepository(
    private val context: Context
) {

    fun showDisasterModeNotification() {
        Notif.showCapsule(
            ctx = context,
            title = "Afet Modu Aktif",
            text = "Yakındaki cihazlara düşük güçte yayın yapılıyor"
        )
    }

    fun showMessageCapsuleNotification(title: String, message: String) {
        Notif.showCapsule(
            ctx = context,
            title = title,
            text = message
        )
    }

    fun showMessageReceivedNotification(senderName: String, message: String) {
        Notif.showCapsule(
            ctx = context,
            title = "Yeni Mesaj: $senderName",
            text = message
        )
    }

    fun showCallIncomingNotification(callerName: String) {
        Notif.showCapsule(
            ctx = context,
            title = "Gelen Arama",
            text = "$callerName sizi arıyor"
        )
    }
}
