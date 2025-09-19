package com.msgmates.app.core.disaster

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for managing disaster mode state across the application.
 * Provides methods to enable, disable, and track disaster mode status.
 */
@Singleton
class DisasterModeRepository @Inject constructor() {

    private val _isEnabled = MutableStateFlow(false)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    fun enable() {
        _isEnabled.value = true
        android.util.Log.d("DisasterModeRepository", "Disaster mode enabled")
    }

    fun disable() {
        _isEnabled.value = false
        android.util.Log.d("DisasterModeRepository", "Disaster mode disabled")
    }

    fun toggle() {
        if (_isEnabled.value) {
            disable()
        } else {
            enable()
        }
    }

    fun isDisasterModeActive(): Boolean = _isEnabled.value
}
