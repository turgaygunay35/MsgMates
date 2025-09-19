package com.msgmates.app.core.disaster.tools

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class SirenPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var mediaPlayer: MediaPlayer? = null
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    fun toggle(): Boolean {
        return if (_isPlaying.value) {
            stop()
        } else {
            play()
        }
    }

    fun play(): Boolean {
        return try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer().apply {
                    // Create a simple siren sound programmatically
                    // In a real implementation, you would use a pre-recorded siren sound file
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )

                    setOnCompletionListener {
                        _isPlaying.value = false
                    }

                    setOnErrorListener { _, what, extra ->
                        android.util.Log.e("SirenPlayer", "MediaPlayer error: $what, $extra")
                        _isPlaying.value = false
                        true
                    }
                }
            }

            // For now, we'll just simulate playing
            // In a real implementation, you would load and play an actual siren sound file
            _isPlaying.value = true
            android.util.Log.d("SirenPlayer", "Siren started playing")
            true
        } catch (e: Exception) {
            android.util.Log.e("SirenPlayer", "Failed to play siren", e)
            false
        }
    }

    fun stop(): Boolean {
        return try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
                mediaPlayer = null
            }
            _isPlaying.value = false
            android.util.Log.d("SirenPlayer", "Siren stopped")
            true
        } catch (e: Exception) {
            android.util.Log.e("SirenPlayer", "Failed to stop siren", e)
            false
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        _isPlaying.value = false
    }
}
