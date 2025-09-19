package com.msgmates.app.ui.settings.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msgmates.app.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        loadNotificationSettings()
    }

    private fun loadNotificationSettings() {
        viewModelScope.launch {
            // Flow'ları collect et
            settingsRepository.getMessageNotificationsFlow().collect { messageNotifications ->
                _uiState.value = _uiState.value.copy(messageNotifications = messageNotifications)
            }
        }

        viewModelScope.launch {
            settingsRepository.getCallNotificationsFlow().collect { callNotifications ->
                _uiState.value = _uiState.value.copy(callNotifications = callNotifications)
            }
        }

        viewModelScope.launch {
            settingsRepository.getSilentHoursStart().collect { silentHoursStart ->
                _uiState.value = _uiState.value.copy(
                    silentHoursStart = silentHoursStart,
                    silentHoursStartText = silentHoursStart
                )
            }
        }

        viewModelScope.launch {
            settingsRepository.getSilentHoursEnd().collect { silentHoursEnd ->
                _uiState.value = _uiState.value.copy(
                    silentHoursEnd = silentHoursEnd,
                    silentHoursEndText = silentHoursEnd
                )
            }
        }

        viewModelScope.launch {
            settingsRepository.getCustomRingtone().collect { customRingtone ->
                _uiState.value = _uiState.value.copy(customRingtoneName = customRingtone)
            }
        }

        viewModelScope.launch {
            settingsRepository.getNotificationContent().collect { notificationContent ->
                _uiState.value = _uiState.value.copy(
                    notificationContent = NotificationContent.valueOf(notificationContent.uppercase()),
                    isLoading = false
                )
            }
        }
    }

    fun updateMessageNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setMessageNotifications(enabled)
            _uiState.value = _uiState.value.copy(
                messageNotifications = enabled,
                showSuccessMessage = true
            )
        }
    }

    fun updateCallNotifications(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setCallNotifications(enabled)
            _uiState.value = _uiState.value.copy(
                callNotifications = enabled,
                showSuccessMessage = true
            )
        }
    }

    fun updateSilentHoursStart(hour: Int, minute: Int) {
        viewModelScope.launch {
            val timeString = String.format("%02d:%02d", hour, minute)
            settingsRepository.setSilentHoursStart(timeString)
            _uiState.value = _uiState.value.copy(
                silentHoursStart = timeString,
                silentHoursStartText = timeString,
                showSuccessMessage = true
            )
        }
    }

    fun updateSilentHoursEnd(hour: Int, minute: Int) {
        viewModelScope.launch {
            val timeString = String.format("%02d:%02d", hour, minute)
            settingsRepository.setSilentHoursEnd(timeString)
            _uiState.value = _uiState.value.copy(
                silentHoursEnd = timeString,
                silentHoursEndText = timeString,
                showSuccessMessage = true
            )
        }
    }

    fun openRingtonePicker() {
        viewModelScope.launch {
            // TODO: Implement ringtone picker
            _uiState.value = _uiState.value.copy(
                customRingtoneName = "Varsayılan Zil Sesi",
                showSuccessMessage = true
            )
        }
    }

    fun updateNotificationContent(content: NotificationContent) {
        viewModelScope.launch {
            settingsRepository.setNotificationContent(content.name.lowercase())
            _uiState.value = _uiState.value.copy(
                notificationContent = content,
                showSuccessMessage = true
            )
        }
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(showSuccessMessage = false)
    }
}

data class NotificationsUiState(
    val isLoading: Boolean = true,
    val messageNotifications: Boolean = true,
    val callNotifications: Boolean = true,
    val silentHoursStart: String = "22:00",
    val silentHoursEnd: String = "08:00",
    val silentHoursStartText: String = "22:00",
    val silentHoursEndText: String = "08:00",
    val customRingtoneName: String = "Varsayılan Zil Sesi",
    val notificationContent: NotificationContent = NotificationContent.SUMMARY,
    val showSuccessMessage: Boolean = false
)
