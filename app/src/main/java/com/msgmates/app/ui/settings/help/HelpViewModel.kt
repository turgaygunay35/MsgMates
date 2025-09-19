package com.msgmates.app.ui.settings.help

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msgmates.app.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HelpViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HelpUiState())
    val uiState: StateFlow<HelpUiState> = _uiState.asStateFlow()

    init {
        loadFaqItems()
    }

    private fun loadFaqItems() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val faqItems = listOf(
                FaqItem(
                    id = "1",
                    question = "MsgMates nedir?",
                    answer = "MsgMates, güvenli ve hızlı mesajlaşma uygulamasıdır. End-to-end şifreleme ile mesajlarınızı korur."
                ),
                FaqItem(
                    id = "2",
                    question = "Mesajlarım güvenli mi?",
                    answer = "Evet, tüm mesajlarınız end-to-end şifreleme ile korunur. Sadece siz ve alıcı mesajları okuyabilir."
                ),
                FaqItem(
                    id = "3",
                    question = "Grup oluşturabilir miyim?",
                    answer = "Evet, istediğiniz kadar kişi ile grup oluşturabilir ve grup sohbetleri yapabilirsiniz."
                ),
                FaqItem(
                    id = "4",
                    question = "Medya dosyalarını nasıl gönderirim?",
                    answer = "Sohbet ekranında kamera veya galeri simgesine tıklayarak fotoğraf, video ve ses dosyaları gönderebilirsiniz."
                ),
                FaqItem(
                    id = "5",
                    question = "Hesabımı nasıl silerim?",
                    answer = "Ayarlar > Güvenlik > Hesabı Sil bölümünden hesabınızı kalıcı olarak silebilirsiniz."
                ),
                FaqItem(
                    id = "6",
                    question = "Bildirimleri nasıl kapatırım?",
                    answer = "Ayarlar > Bildirimler bölümünden istediğiniz bildirim ayarlarını yapabilirsiniz."
                ),
                FaqItem(
                    id = "7",
                    question = "Tema değiştirebilir miyim?",
                    answer = "Evet, Ayarlar > Tema bölümünden açık/koyu tema ve renk seçeneklerini değiştirebilirsiniz."
                ),
                FaqItem(
                    id = "8",
                    question = "Mesajlarımı yedekleyebilir miyim?",
                    answer = "Evet, Ayarlar > Mesajlar > Sohbet Yedekleme bölümünden mesajlarınızı yedekleyebilirsiniz."
                )
            )

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                faqItems = faqItems
            )
        }
    }

    fun toggleFaqItem(itemId: String) {
        val currentItems = _uiState.value.faqItems
        val updatedItems = currentItems.map { item ->
            if (item.id == itemId) {
                item.copy(isExpanded = !item.isExpanded)
            } else {
                item
            }
        }

        _uiState.value = _uiState.value.copy(faqItems = updatedItems)
    }

    fun openContactSupport() {
        viewModelScope.launch {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:support@msgmates.com")
                putExtra(Intent.EXTRA_SUBJECT, "MsgMates Destek Talebi")
                putExtra(Intent.EXTRA_TEXT, "Merhaba,\n\n")
            }

            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    showSuccessMessage = true
                )
            }
        }
    }

    fun openFeedback() {
        viewModelScope.launch {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:feedback@msgmates.com")
                putExtra(Intent.EXTRA_SUBJECT, "MsgMates Geri Bildirim")
                putExtra(Intent.EXTRA_TEXT, "Merhaba,\n\nUygulama hakkında geri bildirimim:\n\n")
            }

            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    showSuccessMessage = true
                )
            }
        }
    }

    fun openAbout() {
        viewModelScope.launch {
            // TODO: Navigate to about screen
            _uiState.value = _uiState.value.copy(
                showSuccessMessage = true
            )
        }
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(showSuccessMessage = false)
    }
}

data class HelpUiState(
    val isLoading: Boolean = true,
    val faqItems: List<FaqItem> = emptyList(),
    val showSuccessMessage: Boolean = false
)
