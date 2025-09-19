package com.msgmates.app.ui.journal

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import com.msgmates.app.databinding.ActivityJournalViewerBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class JournalViewerActivity : ComponentActivity() {

    private lateinit var binding: ActivityJournalViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Immersive full screen
        setupImmersiveMode()

        binding = ActivityJournalViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupImmersiveMode() {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Hide status and navigation bars
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
    }

    private fun setupUI() {
        // Close button
        binding.btnClose.setOnClickListener {
            finish()
        }

        // Reaction buttons
        binding.btnReaction1.setOnClickListener {
            // TODO: Handle reaction
        }

        binding.btnReaction2.setOnClickListener {
            // TODO: Handle reaction
        }

        binding.btnReaction3.setOnClickListener {
            // TODO: Handle reaction
        }

        binding.btnReaction4.setOnClickListener {
            // TODO: Handle reaction
        }

        // Play/Pause for audio
        binding.btnPlayPause.setOnClickListener {
            // TODO: Toggle audio playback
        }

        // TODO: Setup content based on story type
        // TODO: Setup gesture handling (swipe, tap)
        // TODO: Setup progress bar animation
    }

    override fun onResume() {
        super.onResume()
        // TODO: Resume video/audio playback
    }

    override fun onPause() {
        super.onPause()
        // TODO: Pause video/audio playback
    }
}
