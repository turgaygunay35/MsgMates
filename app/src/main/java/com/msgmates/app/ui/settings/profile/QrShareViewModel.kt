package com.msgmates.app.ui.settings.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.msgmates.app.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class QrShareViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QrShareUiState())
    val uiState: StateFlow<QrShareUiState> = _uiState.asStateFlow()

    fun generateQrCode() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val username = settingsRepository.getProfileName().first()
                val phone = settingsRepository.getProfilePhone().first()

                val qrData = "msgmates://profile?username=$username&phone=$phone"
                val qrBitmap = generateQRCodeBitmap(qrData, 512, 512)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    qrBitmap = qrBitmap
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "QR kod oluşturulamadı"
                )
            }
        }
    }

    fun shareQrCode() {
        viewModelScope.launch {
            // TODO: Implement QR code sharing
            _uiState.value = _uiState.value.copy(
                showSuccessMessage = true,
                successMessage = "QR kod paylaşıldı"
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(showSuccessMessage = false, successMessage = null)
    }

    private fun generateQRCodeBitmap(data: String, width: Int, height: Int): Bitmap {
        // Simple QR code generation (in real app, use ZXing or similar library)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        // Draw a simple pattern (replace with actual QR code generation)
        val paint = android.graphics.Paint().apply {
            color = Color.BLACK
            style = android.graphics.Paint.Style.FILL
        }

        // Draw simple squares pattern
        val squareSize = width / 25
        for (i in 0 until 25) {
            for (j in 0 until 25) {
                if ((i + j) % 2 == 0) {
                    canvas.drawRect(
                        i * squareSize.toFloat(),
                        j * squareSize.toFloat(),
                        (i + 1) * squareSize.toFloat(),
                        (j + 1) * squareSize.toFloat(),
                        paint
                    )
                }
            }
        }

        return bitmap
    }
}

data class QrShareUiState(
    val isLoading: Boolean = false,
    val qrBitmap: Bitmap? = null,
    val showSuccessMessage: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null
)
