package com.msgmates.app.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.msgmates.app.receivers.AlarmReceiver
import java.util.Calendar

class AlarmScheduler(private val ctx: Context) {

    fun scheduleExact(timeMillis: Long, title: String, body: String) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(ctx, AlarmReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("body", body)
        }
        val pi = PendingIntent.getBroadcast(
            ctx,
            1001,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pi)
    }

    fun scheduleAt(hour: Int, minute: Int, title: String, body: String) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        scheduleExact(cal.timeInMillis, title, body)
    }

    // Eski çağrılar için alias
    fun schedule(title: String, body: String, timeMillis: Long) =
        scheduleExact(timeMillis, title, body)
}
