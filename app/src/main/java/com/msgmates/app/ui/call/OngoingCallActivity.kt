package com.msgmates.app.ui.call

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.msgmates.app.R
import com.msgmates.app.databinding.ActivityOngoingCallBinding
import com.msgmates.app.domain.call.CallUiState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OngoingCallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOngoingCallBinding
    private val viewModel: CallViewModel by viewModels()

    private var callId: String? = null
    private var callerId: String? = null
    private var callerName: String? = null
    private var callerAvatar: String? = null
    private var isVideo: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Keep screen on during call
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        binding = ActivityOngoingCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get call info from intent
        callId = intent.getStringExtra("callId")
        callerId = intent.getStringExtra("callerId")
        callerName = intent.getStringExtra("callerName")
        callerAvatar = intent.getStringExtra("callerAvatar")
        isVideo = intent.getBooleanExtra("isVideo", false)

        setupUI()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupUI() {
        binding.tvCallerName.text = callerName ?: "Arama Devam Ediyor"
        binding.tvCallDuration.text = "00:00"

        // Update call type indicator
        if (isVideo) {
            binding.ivCallType.setImageResource(R.drawable.ic_videocam)
            binding.tvCallType.text = "Görüntülü Arama"
            binding.fabCamera.visibility = android.view.View.VISIBLE
        } else {
            binding.ivCallType.setImageResource(R.drawable.ic_phone)
            binding.tvCallType.text = "Sesli Arama"
            binding.fabCamera.visibility = android.view.View.GONE
        }

        // TODO: Load caller avatar
        // Glide.with(this)
        //     .load(callerAvatar)
        //     .placeholder(R.drawable.ic_person)
        //     .into(binding.ivCallerAvatar)
    }

    private fun setupClickListeners() {
        binding.fabHangup.setOnClickListener {
            viewModel.endCall()
            dismissNotification()
            finish()
        }

        binding.fabMute.setOnClickListener {
            viewModel.toggleMute()
        }

        binding.fabSpeaker.setOnClickListener {
            viewModel.toggleSpeaker()
        }

        binding.fabCamera.setOnClickListener {
            viewModel.toggleCamera()
        }
    }

    private fun observeViewModel() {
        // TODO: Implement proper state observation
        // For now, we'll handle the call directly
    }

    private fun updateOngoingUI(state: CallUiState.Ongoing) {
        binding.tvCallerName.text = state.callerName
        binding.tvCallDuration.text = formatDuration(state.duration)

        // Update mute button
        binding.fabMute.setImageResource(
            if (state.isMuted) R.drawable.ic_mic_off else R.drawable.ic_mic
        )

        // Update speaker button
        binding.fabSpeaker.setImageResource(
            if (state.isSpeakerOn) R.drawable.ic_speaker else R.drawable.ic_speaker_off
        )

        // Update camera button
        if (state.callType == com.msgmates.app.domain.call.CallType.VIDEO) {
            binding.fabCamera.visibility = android.view.View.VISIBLE
            binding.fabCamera.setImageResource(
                if (state.isCameraOn) R.drawable.ic_videocam else R.drawable.ic_videocam_off
            )
        } else {
            binding.fabCamera.visibility = android.view.View.GONE
        }
    }

    private fun formatDuration(durationMs: Long): String {
        val totalSeconds = durationMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun dismissNotification() {
        callId?.let { id ->
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(id.hashCode())
        }
    }

    override fun onBackPressed() {
        // Prevent back button from ending call
        // User must use hangup button
    }
}
