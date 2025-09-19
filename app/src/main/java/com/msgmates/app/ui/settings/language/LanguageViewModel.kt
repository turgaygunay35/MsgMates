package com.msgmates.app.ui.settings.language

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
class LanguageViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LanguageUiState())
    val uiState: StateFlow<LanguageUiState> = _uiState.asStateFlow()

    init {
        loadLanguageSettings()
    }

    private fun loadLanguageSettings() {
        viewModelScope.launch {
            settingsRepository.getLanguage().collect { languageString ->
                val language = Language.valueOf(languageString.uppercase())
                _uiState.value = _uiState.value.copy(
                    language = language,
                    isLoading = false
                )
            }
        }
    }

    fun updateLanguage(language: Language) {
        viewModelScope.launch {
            val currentLanguage = _uiState.value.language
            if (currentLanguage != language) {
                settingsRepository.setLanguage(language.name.lowercase())
                _uiState.value = _uiState.value.copy(
                    language = language,
                    showRestartDialog = true
                )
            }
        }
    }

    fun clearRestartDialog() {
        _uiState.value = _uiState.value.copy(showRestartDialog = false)
    }

    fun restartApp() {
        // TODO: Implement app restart logic
        // This would typically involve restarting the app or showing a message
        // to the user to manually restart the app
    }
}

data class LanguageUiState(
    val isLoading: Boolean = true,
    val language: Language = Language.TURKISH,
    val showRestartDialog: Boolean = false
)
