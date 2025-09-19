package com.msgmates.app.core.disaster

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.app.NotificationCompat
import com.msgmates.app.R
import com.msgmates.app.core.disaster.mesh.MeshService
import com.msgmates.app.core.location.LocationRepository
import com.msgmates.app.data.local.prefs.DisasterPreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VoiceCommandService : Service() {

    @Inject
    lateinit var disasterPreferences: DisasterPreferences

    @Inject
    lateinit var meshService: MeshService

    @Inject
    lateinit var locationRepository: LocationRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private var helpKeyword = "yardım"

    companion object {
        private const val NOTIFICATION_ID = 1002
        private const val CHANNEL_ID = "voice_command"
        private const val CHANNEL_NAME = "Sesli Komut"

        fun startService(context: Context) {
            val intent = Intent(context, VoiceCommandService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, VoiceCommandService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        loadSettings()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        startVoiceListening()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun loadSettings() {
        serviceScope.launch {
            // TODO: Help keyword'ü DataStore'dan yükle
            helpKeyword = "yardım" // Şimdilik sabit
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Sesli komut dinleme servisi"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("🎤 Sesli Komut Aktif")
        .setContentText("'$helpKeyword' kelimesini dinliyor...")
        .setSmallIcon(R.drawable.ic_mic)
        .setOngoing(true)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()

    private fun startVoiceListening() {
        serviceScope.launch {
            while (true) {
                try {
                    if (!isListening) {
                        startContinuousListening()
                    }
                    delay(1000) // 1 saniye bekle
                } catch (e: Exception) {
                    // Hata durumunda yeniden dene
                    delay(5000) // 5 saniye bekle
                }
            }
        }
    }

    internal fun startContinuousListening() {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer?.setRecognitionListener(createRecognitionListener())
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "tr-TR")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        try {
            speechRecognizer?.startListening(intent)
            isListening = true
        } catch (e: Exception) {
            // Ses tanıma başlatılamadı
            isListening = false
        }
    }

    private fun createRecognitionListener() = object : android.speech.RecognitionListener {
        override fun onReadyForSpeech(params: android.os.Bundle?) {
            // Hazır
        }

        override fun onBeginningOfSpeech() {
            // Konuşma başladı
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Ses seviyesi değişti
        }

        override fun onBufferReceived(buffer: ByteArray?) {
            // Ses verisi alındı
        }

        override fun onEndOfSpeech() {
            // Konuşma bitti
        }

        override fun onError(error: Int) {
            // Hata oluştu, yeniden başlat
            isListening = false
            serviceScope.launch {
                delay(2000) // 2 saniye bekle
                startContinuousListening()
            }
        }

        override fun onResults(results: android.os.Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val spokenText = matches[0].lowercase()
                checkForHelpCommand(spokenText)
            }
            isListening = false
        }

        override fun onPartialResults(partialResults: android.os.Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val spokenText = matches[0].lowercase()
                checkForHelpCommand(spokenText)
            }
        }

        override fun onEvent(eventType: Int, params: android.os.Bundle?) {
            // Diğer olaylar
        }
    }

    internal fun checkForHelpCommand(spokenText: String) {
        if (spokenText.contains(helpKeyword)) {
            // Yardım komutu algılandı!
            triggerEmergencyResponse()
        }
    }

    private fun triggerEmergencyResponse() {
        serviceScope.launch {
            // Afet modunu aktifleştir
            disasterPreferences.setDisasterEnabled(true)

            // Acil durum bildirimi gönder
            showEmergencyNotification()

            // BLE mesh ile "YARDIM" mesajı yayınla
            broadcastEmergencyMessage()
        }
    }

    private fun showEmergencyNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🚨 YARDIM KOMUTU ALGILANDI!")
            .setContentText("Sesli yardım komutu algılandı - Acil durum mesajı gönderildi")
            .setSmallIcon(R.drawable.ic_mic)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVibrate(longArrayOf(0, 1000, 500, 1000, 500, 1000))
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID + 2, notification)
    }

    private suspend fun broadcastEmergencyMessage() {
        // Mevcut konumu al (Flow'dan değer al)
        var currentLocation: android.location.Location? = null
        locationRepository.getCurrentLocation().collect { location ->
            currentLocation = location
        }

        // BLE mesh ile konum tabanlı "YARDIM" mesajı yayınla
        meshService.broadcastVoiceHelpMessage(
            senderId = "user_${System.currentTimeMillis()}", // TODO: Gerçek kullanıcı ID'si
            location = currentLocation,
            helpKeyword = helpKeyword
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
