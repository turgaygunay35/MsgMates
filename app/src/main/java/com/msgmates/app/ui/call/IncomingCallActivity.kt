package com.msgmates.app.ui.call

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.msgmates.app.R
import com.msgmates.app.databinding.ActivityIncomingCallBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IncomingCallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIncomingCallBinding
    private val viewModel: CallViewModel by viewModels()

    private var callId: String? = null
    private var callerId: String? = null
    private var callerName: String? = null
    private var callerAvatar: String? = null
    private var isVideo: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Full screen settings
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        binding = ActivityIncomingCallBinding.inflate(layoutInflater)
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
        binding.tvCallerName.text = callerName ?: "Gelen Arama"
        binding.tvCallStatus.text = "Gelen arama"

        // Update call type indicator
        if (isVideo) {
            binding.ivCallType.setImageResource(R.drawable.ic_videocam)
            binding.tvCallType.text = "Görüntülü Arama"
        } else {
            binding.ivCallType.setImageResource(R.drawable.ic_phone)
            binding.tvCallType.text = "Sesli Arama"
        }

        // TODO: Load caller avatar
        // Glide.with(this)
        //     .load(callerAvatar)
        //     .placeholder(R.drawable.ic_person)
        //     .into(binding.ivCallerAvatar)
    }

    private fun setupClickListeners() {
        binding.fabReject.setOnClickListener {
            viewModel.rejectIncomingCall()
            dismissNotification()
            finish()
        }

        binding.fabAnswerAudio.setOnClickListener {
            callId?.let { id ->
                viewModel.acceptIncomingCall(id, false)
                navigateToOngoingCall(false)
            }
        }

        binding.fabAnswerVideo.setOnClickListener {
            callId?.let { id ->
                viewModel.acceptIncomingCall(id, true)
                navigateToOngoingCall(true)
            }
        }
    }

    private fun observeViewModel() {
        // TODO: Implement proper state observation
        // For now, we'll handle the call directly
    }

    private fun navigateToOngoingCall(video: Boolean) {
        val intent = Intent(this, OngoingCallActivity::class.java).apply {
            putExtra("callId", callId)
            putExtra("callerId", callerId)
            putExtra("callerName", callerName)
            putExtra("callerAvatar", callerAvatar)
            putExtra("isVideo", video)
        }
        startActivity(intent)
        finish()
    }

    private fun dismissNotification() {
        callId?.let { id ->
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(id.hashCode())
        }
    }

    override fun onBackPressed() {
        // Prevent back button from dismissing incoming call
        // User must answer or reject
    }
}
