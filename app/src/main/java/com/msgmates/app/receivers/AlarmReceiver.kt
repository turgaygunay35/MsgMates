package com.msgmates.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.msgmates.app.util.Notif

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Hatırlatıcı"
        val body = intent.getStringExtra("body") ?: "Zamanı geldi."
        Notif.notifyCapsule(context, title, body)
    }
}
