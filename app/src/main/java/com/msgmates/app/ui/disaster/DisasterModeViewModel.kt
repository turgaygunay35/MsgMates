package com.msgmates.app.ui.disaster

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msgmates.app.core.disaster.mesh.MeshService
import com.msgmates.app.core.location.LocationRepository
import com.msgmates.app.data.local.prefs.DisasterPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@HiltViewModel
class DisasterModeViewModel @Inject constructor(
    private val disasterPreferences: DisasterPreferences,
    private val meshService: MeshService,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DisasterModeUiState())
    val uiState: StateFlow<DisasterModeUiState> = _uiState.asStateFlow()

    init {
        // Observe disaster mode state from DataStore
        viewModelScope.launch {
            combine(
                disasterPreferences.isDisasterEnabled,
                disasterPreferences.energySaving,
                disasterPreferences.autoEnableOnEarthquake
            ) { isEnabled, energySaving, autoEnable ->
                val currentState = _uiState.value
                // Only update if values actually changed to prevent flickering
                if (currentState.isDisasterEnabled != isEnabled || currentState.isEnergySaving != energySaving || currentState.autoEnableOnEarthquake != autoEnable) {
                    _uiState.value = currentState.copy(
                        isDisasterEnabled = isEnabled,
                        isEnergySaving = energySaving,
                        autoEnableOnEarthquake = autoEnable
                    )
                }
            }.collect { }
        }
    }

    fun toggleDisasterMode() {
        viewModelScope.launch {
            val currentState = _uiState.value.isDisasterEnabled
            disasterPreferences.setDisasterEnabled(!currentState)
        }
    }

    fun broadcastImSafe() {
        if (!_uiState.value.isDisasterEnabled) return

        viewModelScope.launch {
            val currentState = _uiState.value
            // Only update if not already broadcasting
            if (!currentState.isBroadcasting) {
                _uiState.value = currentState.copy(
                    isBroadcasting = true,
                    broadcastSuccess = false,
                    error = null
                )
            }

            try {
                // Mevcut konumu al (Flow'dan değer al)
                var currentLocation: android.location.Location? = null
                locationRepository.getCurrentLocation().collect { location ->
                    currentLocation = location
                }

                // BLE mesh ile konum tabanlı mesaj yayınla
                meshService.broadcastOKStatusMessage(
                    senderId = "user_${System.currentTimeMillis()}", // TODO: Gerçek kullanıcı ID'si
                    location = currentLocation
                )

                kotlinx.coroutines.delay(2000)

                val currentState = _uiState.value
                if (currentState.isBroadcasting || !currentState.broadcastSuccess || currentState.error != null) {
                    _uiState.value = currentState.copy(
                        isBroadcasting = false,
                        broadcastSuccess = true,
                        error = null
                    )
                }
            } catch (e: Exception) {
                val currentState = _uiState.value
                val errorMessage = e.message ?: "Bilinmeyen hata"
                if (currentState.isBroadcasting || currentState.broadcastSuccess || currentState.error != errorMessage) {
                    _uiState.value = currentState.copy(
                        isBroadcasting = false,
                        broadcastSuccess = false,
                        error = errorMessage
                    )
                }
            }
        }
    }

    fun sendQuickMessage(message: String) {
        if (!_uiState.value.isDisasterEnabled) return

        viewModelScope.launch {
            val currentState = _uiState.value
            // Only update if not already broadcasting
            if (!currentState.isBroadcasting) {
                _uiState.value = currentState.copy(
                    isBroadcasting = true,
                    broadcastSuccess = false,
                    error = null
                )
            }

            try {
                // Mevcut konumu al (Flow'dan değer al)
                var currentLocation: android.location.Location? = null
                locationRepository.getCurrentLocation().collect { location ->
                    currentLocation = location
                }

                // Mesaj tipine göre uygun fonksiyonu çağır
                when (message) {
                    "SOS" -> meshService.broadcastSOSMessage(
                        senderId = "user_${System.currentTimeMillis()}",
                        location = currentLocation
                    )
                    "Su Lazım" -> meshService.broadcastWaterNeededMessage(
                        senderId = "user_${System.currentTimeMillis()}",
                        location = currentLocation
                    )
                    "İyiyim" -> meshService.broadcastOKStatusMessage(
                        senderId = "user_${System.currentTimeMillis()}",
                        location = currentLocation
                    )
                }

                kotlinx.coroutines.delay(1500)

                val currentState = _uiState.value
                if (currentState.isBroadcasting || !currentState.broadcastSuccess || currentState.error != null) {
                    _uiState.value = currentState.copy(
                        isBroadcasting = false,
                        broadcastSuccess = true,
                        error = null
                    )
                }
            } catch (e: Exception) {
                val currentState = _uiState.value
                val errorMessage = e.message ?: "Mesaj gönderilemedi"
                if (currentState.isBroadcasting || currentState.broadcastSuccess || currentState.error != errorMessage) {
                    _uiState.value = currentState.copy(
                        isBroadcasting = false,
                        broadcastSuccess = false,
                        error = errorMessage
                    )
                }
            }
        }
    }

    fun toggleFlashlight() {
        if (!_uiState.value.isDisasterEnabled) return

        viewModelScope.launch {
            try {
                // TODO: Implement flashlight helper
                // flashlightHelper.toggle()
                val currentState = _uiState.value.isFlashlightOn
                _uiState.value = _uiState.value.copy(isFlashlightOn = !currentState)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Fener açılamadı: ${e.message}")
            }
        }
    }

    fun toggleSiren() {
        if (!_uiState.value.isDisasterEnabled) return

        viewModelScope.launch {
            try {
                // TODO: Implement siren player
                // sirenPlayer.toggle()
                val currentState = _uiState.value.isSirenOn
                _uiState.value = _uiState.value.copy(isSirenOn = !currentState)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Siren çalınamadı: ${e.message}")
            }
        }
    }

    fun setEnergySaving(enabled: Boolean) {
        if (!_uiState.value.isDisasterEnabled) return

        viewModelScope.launch {
            disasterPreferences.setEnergySaving(enabled)
        }
    }

    fun openOfflineChat() {
        if (!_uiState.value.isDisasterEnabled) return
        // TODO: Navigate to offline chat fragment
        // findNavController().navigate(R.id.dest_offline_chat)
    }

    fun showEmergencyInfo() {
        // Emergency info is always available (static information)
        // TODO: Show emergency information dialog
        // This would typically show local emergency numbers, safety tips, etc.
    }

    fun openDisasterSettings() {
        // This will be handled by the Activity
    }
}

data class DisasterModeUiState(
    val isDisasterEnabled: Boolean = false,
    val isBroadcasting: Boolean = false,
    val broadcastSuccess: Boolean = false,
    val error: String? = null,
    val isFlashlightOn: Boolean = false,
    val isSirenOn: Boolean = false,
    val isEnergySaving: Boolean = false,
    val autoEnableOnEarthquake: Boolean = false
)
