package com.msgmates.app.core.disaster

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.msgmates.app.R
import com.msgmates.app.data.local.prefs.DisasterPreferences
import com.msgmates.app.ui.disaster.DisasterModeActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EarthquakeDetectionService : Service() {

    @Inject
    lateinit var disasterPreferences: DisasterPreferences

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "earthquake_detection"
        private const val CHANNEL_NAME = "Deprem Algılama"

        fun startService(context: Context) {
            val intent = Intent(context, EarthquakeDetectionService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, EarthquakeDetectionService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        startEarthquakeMonitoring()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Deprem algılama servisi çalışıyor"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("🚨 Deprem Algılama Aktif")
        .setContentText("Afet modu için deprem takibi yapılıyor")
        .setSmallIcon(R.drawable.ic_earthquake)
        .setOngoing(true)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()

    private fun startEarthquakeMonitoring() {
        serviceScope.launch {
            while (true) {
                try {
                    // TODO: Gerçek deprem API'si entegrasyonu
                    // Şimdilik simüle edilmiş veri
                    checkForEarthquakes()
                    delay(300000) // 5 dakikada bir kontrol et
                } catch (e: Exception) {
                    // Hata durumunda servisi yeniden başlat
                    delay(60000) // 1 dakika bekle
                }
            }
        }
    }

    private suspend fun checkForEarthquakes() {
        // TODO: AFAD veya Kandilli API'sinden deprem verilerini çek
        // Şimdilik simüle edilmiş veri

        // TODO: Check auto enable from preferences
        // val autoEnable = disasterPreferences.autoEnableOnEarthquake
        // if (!autoEnable) return

        // Simüle edilmiş deprem verisi
        val simulatedEarthquake = simulateEarthquakeData()

        if (simulatedEarthquake != null && simulatedEarthquake.magnitude >= 6.5) {
            // Deprem algılandı, afet modunu aktifleştir
            activateDisasterMode(simulatedEarthquake)
        }
    }

    private fun simulateEarthquakeData(): EarthquakeData? {
        // %1 ihtimalle 6.5+ deprem simüle et
        return if (kotlin.random.Random.nextDouble() < 0.01) {
            EarthquakeData(
                magnitude = 6.5 + kotlin.random.Random.nextDouble() * 2.0, // 6.5-8.5 arası
                location = "İstanbul",
                depth = 5.0 + kotlin.random.Random.nextDouble() * 15.0, // 5-20 km arası
                timestamp = System.currentTimeMillis()
            )
        } else {
            null
        }
    }

    private suspend fun activateDisasterMode(earthquake: EarthquakeData) {
        // Afet modunu aktifleştir
        disasterPreferences.setDisasterEnabled(true)

        // Kullanıcıyı bilgilendir
        showEarthquakeNotification(earthquake)

        // Afet modu ekranını aç
        val intent = Intent(this, DisasterModeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    private fun showEarthquakeNotification(earthquake: EarthquakeData) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(this, DisasterModeActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🚨 DEPREM ALGILANDI!")
            .setContentText("${earthquake.magnitude} büyüklüğünde deprem - Afet modu aktifleştirildi")
            .setSmallIcon(R.drawable.ic_earthquake)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVibrate(longArrayOf(0, 1000, 500, 1000))
            .build()

        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }
}

data class EarthquakeData(
    val magnitude: Double,
    val location: String,
    val depth: Double,
    val timestamp: Long
)
