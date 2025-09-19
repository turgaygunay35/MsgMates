package com.msgmates.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.msgmates.app.util.AlarmScheduler

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Örnek: 10 sn sonra bir notifikasyon (persist verin yoksa)
        val t = System.currentTimeMillis() + 10_000
        AlarmScheduler(context).schedule("Hoş geldin", "Cihaz yeniden başlatıldı", t)
    }
}
