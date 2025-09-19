package com.msgmates.app.ui.qa

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.msgmates.app.core.analytics.EventLogger
import com.msgmates.app.core.disaster.DisasterModeRepository
import com.msgmates.app.core.theme.ThemeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class QaViewModel @Inject constructor(
    private val themeManager: ThemeManager,
    private val disasterModeRepository: DisasterModeRepository
) : ViewModel() {

    private val _qaStatus = MutableLiveData<String>()
    val qaStatus: LiveData<String> = _qaStatus

    private var isDarkTheme = false
    private var isRtlSimulation = false
    private var isBadgeTest = false
    private var isOfflineSimulation = false
    private var isDisasterMode = false

    init {
        isDarkTheme = themeManager.isDarkTheme()
        isDisasterMode = disasterModeRepository.isDisasterModeActive()
        updateStatus()
    }

    fun toggleTheme() {
        themeManager.toggleTheme()
        isDarkTheme = themeManager.isDarkTheme()
        EventLogger.log("qa_toggle_theme", mapOf("dark_theme" to isDarkTheme))
        updateStatus()
    }

    fun toggleRtl() {
        isRtlSimulation = !isRtlSimulation
        EventLogger.log("qa_toggle_rtl", mapOf("rtl_simulation" to isRtlSimulation))
        updateStatus()

        // TODO: Implement RTL simulation
        // This would typically involve changing the layout direction
    }

    fun toggleBadgeTest() {
        isBadgeTest = !isBadgeTest
        EventLogger.log("qa_toggle_badge", mapOf("badge_test" to isBadgeTest))
        updateStatus()

        // TODO: Implement badge test
        // This would typically involve showing/hiding test badges
    }

    fun toggleOfflineSimulation() {
        isOfflineSimulation = !isOfflineSimulation
        EventLogger.log("qa_toggle_offline", mapOf("offline_simulation" to isOfflineSimulation))
        updateStatus()

        // TODO: Implement offline simulation
        // This would typically involve simulating network conditions
    }

    fun toggleDisasterMode() {
        disasterModeRepository.toggle()
        isDisasterMode = disasterModeRepository.isDisasterModeActive()
        EventLogger.log("qa_toggle_disaster", mapOf("disaster_mode" to isDisasterMode))
        updateStatus()
    }

    private fun updateStatus() {
        val themeStatus = if (isDarkTheme) "Karanlık" else "Açık"
        val rtlStatus = if (isRtlSimulation) "Açık" else "Kapalı"
        val badgeStatus = if (isBadgeTest) "Test" else "Normal"
        val connectionStatus = if (isOfflineSimulation) "Offline" else "Online"

        val disasterStatus = if (isDisasterMode) "Açık" else "Kapalı"

        val status = """
            Tema: $themeStatus
            RTL: $rtlStatus
            Badge: $badgeStatus
            Bağlantı: $connectionStatus
            Afet Modu: $disasterStatus
        """.trimIndent()

        _qaStatus.value = status
    }

    // Getters for external access
    fun isDarkThemeEnabled(): Boolean = isDarkTheme
    fun isRtlSimulationEnabled(): Boolean = isRtlSimulation
    fun isBadgeTestEnabled(): Boolean = isBadgeTest
    fun isOfflineSimulationEnabled(): Boolean = isOfflineSimulation
}
