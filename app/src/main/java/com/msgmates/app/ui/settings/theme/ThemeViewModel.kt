package com.msgmates.app.ui.settings.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msgmates.app.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ThemeUiState())
    val uiState: StateFlow<ThemeUiState> = _uiState.asStateFlow()

    init {
        loadThemeSettings()
    }

    private fun loadThemeSettings() {
        viewModelScope.launch {
            settingsRepository.getThemeMode().collect { themeModeString ->
                val themeMode = ThemeMode.valueOf(themeModeString.uppercase())
                _uiState.value = _uiState.value.copy(
                    themeMode = themeMode,
                    isLoading = false
                )
            }
        }

        viewModelScope.launch {
            settingsRepository.getColorTheme().collect { colorThemeString ->
                val colorTheme = ColorTheme.valueOf(colorThemeString.uppercase())
                _uiState.value = _uiState.value.copy(colorTheme = colorTheme)
            }
        }

        viewModelScope.launch {
            settingsRepository.getFontSize().collect { fontSizeString ->
                val fontSize = FontSize.valueOf(fontSizeString.uppercase())
                _uiState.value = _uiState.value.copy(fontSize = fontSize)
            }
        }

        viewModelScope.launch {
            settingsRepository.getBubbleDensity().collect { bubbleDensityString ->
                val bubbleDensity = BubbleDensity.valueOf(bubbleDensityString.uppercase())
                _uiState.value = _uiState.value.copy(bubbleDensity = bubbleDensity)
            }
        }
    }

    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(themeMode.name.lowercase())
            _uiState.value = _uiState.value.copy(
                themeMode = themeMode,
                showSuccessMessage = true
            )
        }
    }

    fun updateColorTheme(colorTheme: ColorTheme) {
        viewModelScope.launch {
            settingsRepository.setColorTheme(colorTheme.name.lowercase())
            _uiState.value = _uiState.value.copy(
                colorTheme = colorTheme,
                showSuccessMessage = true
            )
        }
    }

    fun updateFontSize(fontSize: FontSize) {
        viewModelScope.launch {
            settingsRepository.setFontSize(fontSize.name.lowercase())
            _uiState.value = _uiState.value.copy(
                fontSize = fontSize,
                showSuccessMessage = true
            )
        }
    }

    fun updateBubbleDensity(bubbleDensity: BubbleDensity) {
        viewModelScope.launch {
            settingsRepository.setBubbleDensity(bubbleDensity.name.lowercase())
            _uiState.value = _uiState.value.copy(
                bubbleDensity = bubbleDensity,
                showSuccessMessage = true
            )
        }
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(showSuccessMessage = false)
    }
}

data class ThemeUiState(
    val isLoading: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val colorTheme: ColorTheme = ColorTheme.MSGMATES_GRADIENT,
    val fontSize: FontSize = FontSize.MEDIUM,
    val bubbleDensity: BubbleDensity = BubbleDensity.MEDIUM,
    val showSuccessMessage: Boolean = false
)
