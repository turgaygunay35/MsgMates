package com.msgmates.app.core.disaster.mesh

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.location.Location
import android.util.Log
import com.msgmates.app.core.disaster.EmergencyMessage
import com.msgmates.app.core.disaster.EmergencyMessageSerializer
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class MeshService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val meshRepository: MeshRepository
) {

    companion object {
        private const val TAG = "MeshService"
        private const val MESH_SERVICE_UUID = "12345678-1234-1234-1234-123456789ABC"
        private const val ADVERTISE_INTERVAL_NORMAL = 1000L // 1 second
        private const val ADVERTISE_INTERVAL_ENERGY_SAVING = 5000L // 5 seconds
    }

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val advertiser: BluetoothLeAdvertiser? = bluetoothAdapter?.bluetoothLeAdvertiser
    private val scanner = bluetoothAdapter?.bluetoothLeScanner

    private val _isAdvertising = MutableStateFlow(false)
    val isAdvertising: StateFlow<Boolean> = _isAdvertising.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _receivedMessages = MutableStateFlow<List<MeshMessage>>(emptyList())
    val receivedMessages: StateFlow<List<MeshMessage>> = _receivedMessages.asStateFlow()

    private val sentMessageIds = mutableSetOf<String>()

    fun startAdvertising(isEnergySaving: Boolean = false) {
        if (advertiser == null) {
            Log.e(TAG, "Bluetooth LE Advertiser not available")
            return
        }

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(
                if (isEnergySaving) {
                    AdvertiseSettings.ADVERTISE_MODE_LOW_POWER
                } else {
                    AdvertiseSettings.ADVERTISE_MODE_BALANCED
                }
            )
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .setConnectable(false)
            .build()

        val advertiseData = AdvertiseData.Builder()
            .addServiceUuid(android.os.ParcelUuid.fromString(MESH_SERVICE_UUID))
            .setIncludeDeviceName(false)
            .build()

        advertiser.startAdvertising(settings, advertiseData, advertiseCallback)
        _isAdvertising.value = true
        Log.d(TAG, "Started BLE advertising")
    }

    fun stopAdvertising() {
        advertiser?.stopAdvertising(advertiseCallback)
        _isAdvertising.value = false
        Log.d(TAG, "Stopped BLE advertising")
    }

    fun startScanning() {
        if (scanner == null) {
            Log.e(TAG, "Bluetooth LE Scanner not available")
            return
        }

        val filter = ScanFilter.Builder()
            .setServiceUuid(android.os.ParcelUuid.fromString(MESH_SERVICE_UUID))
            .build()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanner.startScan(listOf(filter), settings, scanCallback)
        _isScanning.value = true
        Log.d(TAG, "Started BLE scanning")
    }

    fun stopScanning() {
        scanner?.stopScan(scanCallback)
        _isScanning.value = false
        Log.d(TAG, "Stopped BLE scanning")
    }

    fun broadcastMessage(message: MeshMessage) {
        // Check for duplicate
        if (sentMessageIds.contains(message.id)) {
            Log.w(TAG, "Duplicate message detected, skipping")
            return
        }

        sentMessageIds.add(message.id)

        // Save to repository
        meshRepository.saveMessage(message)

        // Start advertising if not already
        if (!_isAdvertising.value) {
            startAdvertising()
        }

        Log.d(TAG, "Broadcasting message: ${message.content}")
    }

    fun broadcastMessage(content: String, type: MessageType) {
        val message = MeshMessage(
            id = UUID.randomUUID().toString(),
            senderId = getDeviceId(),
            latitude = 0.0, // TODO: Get real location
            longitude = 0.0, // TODO: Get real location
            timestamp = System.currentTimeMillis(),
            type = type,
            content = content
        )

        broadcastMessage(message)
    }

    private fun getDeviceId(): String {
        // TODO: Generate or retrieve unique device ID
        return "device_${System.currentTimeMillis()}"
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Log.d(TAG, "Advertising started successfully")
        }

        override fun onStartFailure(errorCode: Int) {
            Log.e(TAG, "Advertising failed to start: $errorCode")
            _isAdvertising.value = false
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            try {
                val scanRecord = result.scanRecord
                val serviceUuids = scanRecord?.serviceUuids

                if (serviceUuids?.contains(android.os.ParcelUuid.fromString(MESH_SERVICE_UUID)) == true) {
                    // Parse message from scan record
                    val message = parseMessageFromScanRecord(scanRecord)
                    if (message != null && !sentMessageIds.contains(message.id)) {
                        _receivedMessages.value = _receivedMessages.value + message
                        meshRepository.saveMessage(message)
                        Log.d(TAG, "Received message: ${message.content}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing scan result", e)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Scan failed: $errorCode")
            _isScanning.value = false
        }
    }

    internal fun parseMessageFromScanRecord(scanRecord: android.bluetooth.le.ScanRecord): MeshMessage? {
        // TODO: Implement proper message parsing from BLE scan record
        // This is a simplified implementation
        return try {
            MeshMessage(
                id = UUID.randomUUID().toString(),
                senderId = "unknown",
                latitude = 0.0,
                longitude = 0.0,
                timestamp = System.currentTimeMillis(),
                type = MessageType.ALIVE,
                content = "Received message"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse message", e)
            null
        }
    }

    // Konum tabanlı acil durum mesajı gönderme
    fun broadcastEmergencyMessage(emergencyMessage: EmergencyMessage) {
        val messageJson = EmergencyMessageSerializer.serialize(emergencyMessage)
        val meshMessage = MeshMessage(
            id = emergencyMessage.messageId,
            senderId = emergencyMessage.senderId,
            latitude = emergencyMessage.location?.latitude ?: 0.0,
            longitude = emergencyMessage.location?.longitude ?: 0.0,
            timestamp = emergencyMessage.timestamp,
            type = MessageType.EMERGENCY,
            content = messageJson,
            priority = emergencyMessage.priority.name
        )

        broadcastMessage(meshMessage)
        Log.d(TAG, "Emergency message broadcasted: ${emergencyMessage.messageType}")
    }

    // Sesli yardım komutu için konum tabanlı mesaj
    fun broadcastVoiceHelpMessage(
        senderId: String,
        location: Location?,
        helpKeyword: String
    ) {
        val emergencyMessage = EmergencyMessageSerializer.createVoiceHelpMessage(
            senderId = senderId,
            location = location,
            helpKeyword = helpKeyword
        )
        broadcastEmergencyMessage(emergencyMessage)
    }

    // SOS mesajı konum ile
    fun broadcastSOSMessage(senderId: String, location: Location?) {
        val emergencyMessage = EmergencyMessageSerializer.createSOSMessage(
            senderId = senderId,
            location = location
        )
        broadcastEmergencyMessage(emergencyMessage)
    }

    // Su ihtiyacı mesajı konum ile
    fun broadcastWaterNeededMessage(senderId: String, location: Location?) {
        val emergencyMessage = EmergencyMessageSerializer.createWaterNeededMessage(
            senderId = senderId,
            location = location
        )
        broadcastEmergencyMessage(emergencyMessage)
    }

    // İyi durum mesajı konum ile
    fun broadcastOKStatusMessage(senderId: String, location: Location?) {
        val emergencyMessage = EmergencyMessageSerializer.createOKStatusMessage(
            senderId = senderId,
            location = location
        )
        broadcastEmergencyMessage(emergencyMessage)
    }
}

enum class MessageType {
    ALIVE,
    QUICK_MESSAGE,
    SOS,
    WATER_NEEDED,
    OK_STATUS,
    EMERGENCY
}
