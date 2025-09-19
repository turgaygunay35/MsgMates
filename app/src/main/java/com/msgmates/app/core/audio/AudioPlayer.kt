package com.msgmates.app.core.audio

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class AudioPlayer @Inject constructor(
    private val context: Context
) {

    private var exoPlayer: ExoPlayer? = null
    private var currentUri: Uri? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    init {
        initializePlayer()
    }

    private fun initializePlayer() {
        exoPlayer = ExoPlayer.Builder(context).build().apply {
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_ENDED -> {
                            _isPlaying.value = false
                            _currentPosition.value = 0L
                        }
                        Player.STATE_READY -> {
                            _duration.value = duration
                        }
                    }
                }
            })
        }
    }

    fun playAudio(uri: Uri) {
        try {
            currentUri = uri

            val mediaItem = MediaItem.fromUri(uri)
            exoPlayer?.setMediaItem(mediaItem)
            exoPlayer?.prepare()
            exoPlayer?.play()

            Log.d("AudioPlayer", "Playing audio: $uri")
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Failed to play audio", e)
        }
    }

    fun pauseAudio() {
        exoPlayer?.pause()
    }

    fun resumeAudio() {
        exoPlayer?.play()
    }

    fun stopAudio() {
        exoPlayer?.stop()
        _isPlaying.value = false
        _currentPosition.value = 0L
        currentUri = null
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
    }

    fun isCurrentlyPlaying(uri: Uri): Boolean {
        return _isPlaying.value && currentUri == uri
    }

    fun getCurrentPosition(): Long {
        return exoPlayer?.currentPosition ?: 0L
    }

    fun getDuration(): Long {
        return exoPlayer?.duration ?: 0L
    }

    fun release() {
        exoPlayer?.release()
        exoPlayer = null
        currentUri = null
        _isPlaying.value = false
        _currentPosition.value = 0L
        _duration.value = 0L
    }
}
