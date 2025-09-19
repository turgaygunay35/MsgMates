package com.msgmates.app.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msgmates.app.analytics.ContactsTelemetryData
import com.msgmates.app.analytics.ContactsTelemetryService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ContactsTelemetryViewModel @Inject constructor(
    private val telemetryService: ContactsTelemetryService
) : ViewModel() {

    private val _telemetryData = MutableStateFlow(
        ContactsTelemetryData(
            syncDurationMs = 0L,
            batchSize = 0,
            errorCodes = emptyList(),
            pullToRefreshCount = 0,
            filterFavoritesUsage = 0,
            filterMsgMatesUsage = 0,
            lastSyncTimestamp = 0L,
            totalSyncCount = 0
        )
    )
    val telemetryData: StateFlow<ContactsTelemetryData> = _telemetryData.asStateFlow()

    fun loadTelemetryData() {
        viewModelScope.launch {
            telemetryService.telemetryData.collect { data ->
                _telemetryData.value = data
            }
        }
    }

    fun clearTelemetryData() {
        viewModelScope.launch {
            telemetryService.clearTelemetryData()
        }
    }

    fun getSyncPerformanceMetrics() {
        viewModelScope.launch {
            val metrics = telemetryService.getSyncPerformanceMetrics()
            // Handle metrics display or logging
        }
    }
}
