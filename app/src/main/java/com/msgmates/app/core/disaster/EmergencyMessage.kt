package com.msgmates.app.core.disaster

import android.location.Location
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class EmergencyMessage(
    val messageId: String,
    val senderId: String,
    val messageType: EmergencyMessageType,
    val content: String,
    val location: EmergencyLocation?,
    val timestamp: Long,
    val priority: EmergencyPriority = EmergencyPriority.HIGH
)

@Serializable
data class EmergencyLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val altitude: Double? = null,
    val address: String? = null
)

@Serializable
enum class EmergencyMessageType {
    VOICE_HELP, // Sesli yardım çağrısı
    SOS, // SOS mesajı
    WATER_NEEDED, // Su ihtiyacı
    OK_STATUS, // İyi durumda
    EARTHQUAKE_ALERT, // Deprem uyarısı
    GENERAL_HELP // Genel yardım
}

@Serializable
enum class EmergencyPriority {
    LOW, // Düşük öncelik
    MEDIUM, // Orta öncelik
    HIGH, // Yüksek öncelik
    CRITICAL // Kritik öncelik
}

object EmergencyMessageSerializer {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun serialize(message: EmergencyMessage): String {
        return json.encodeToString(message)
    }

    fun deserialize(jsonString: String): EmergencyMessage? {
        return try {
            json.decodeFromString<EmergencyMessage>(jsonString)
        } catch (e: Exception) {
            null
        }
    }

    fun createVoiceHelpMessage(
        senderId: String,
        location: Location?,
        helpKeyword: String
    ): EmergencyMessage {
        return EmergencyMessage(
            messageId = generateMessageId(),
            senderId = senderId,
            messageType = EmergencyMessageType.VOICE_HELP,
            content = "🚨 YARDIM! '$helpKeyword' sesli komutu algılandı! Enkaz altındayım!",
            location = location?.let {
                EmergencyLocation(
                    latitude = it.latitude,
                    longitude = it.longitude,
                    accuracy = it.accuracy,
                    altitude = it.altitude
                )
            },
            timestamp = System.currentTimeMillis(),
            priority = EmergencyPriority.CRITICAL
        )
    }

    fun createSOSMessage(
        senderId: String,
        location: Location?
    ): EmergencyMessage {
        return EmergencyMessage(
            messageId = generateMessageId(),
            senderId = senderId,
            messageType = EmergencyMessageType.SOS,
            content = "🚨 SOS! Acil yardım gerekli!",
            location = location?.let {
                EmergencyLocation(
                    latitude = it.latitude,
                    longitude = it.longitude,
                    accuracy = it.accuracy,
                    altitude = it.altitude
                )
            },
            timestamp = System.currentTimeMillis(),
            priority = EmergencyPriority.CRITICAL
        )
    }

    fun createWaterNeededMessage(
        senderId: String,
        location: Location?
    ): EmergencyMessage {
        return EmergencyMessage(
            messageId = generateMessageId(),
            senderId = senderId,
            messageType = EmergencyMessageType.WATER_NEEDED,
            content = "💧 Su lazım! Su ihtiyacım var!",
            location = location?.let {
                EmergencyLocation(
                    latitude = it.latitude,
                    longitude = it.longitude,
                    accuracy = it.accuracy,
                    altitude = it.altitude
                )
            },
            timestamp = System.currentTimeMillis(),
            priority = EmergencyPriority.HIGH
        )
    }

    fun createOKStatusMessage(
        senderId: String,
        location: Location?
    ): EmergencyMessage {
        return EmergencyMessage(
            messageId = generateMessageId(),
            senderId = senderId,
            messageType = EmergencyMessageType.OK_STATUS,
            content = "✅ İyiyim! Güvende olduğumu bildiriyorum.",
            location = location?.let {
                EmergencyLocation(
                    latitude = it.latitude,
                    longitude = it.longitude,
                    accuracy = it.accuracy,
                    altitude = it.altitude
                )
            },
            timestamp = System.currentTimeMillis(),
            priority = EmergencyPriority.MEDIUM
        )
    }

    private fun generateMessageId(): String {
        return "emergency_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
}
