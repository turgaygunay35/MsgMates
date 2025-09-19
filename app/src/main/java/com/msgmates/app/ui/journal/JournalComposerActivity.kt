package com.msgmates.app.ui.journal

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import com.google.android.material.tabs.TabLayout
import com.msgmates.app.databinding.ActivityJournalComposerBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class JournalComposerActivity : ComponentActivity() {

    private lateinit var binding: ActivityJournalComposerBinding
    private var selectedDuration = 24 // Default 24 hours

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityJournalComposerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        // Cancel button
        binding.btnCancel.setOnClickListener {
            finish()
        }

        // Publish button
        binding.btnPublish.setOnClickListener {
            publishStory()
        }

        // Duration buttons
        setupDurationButtons()

        // Tab layout
        setupTabLayout()
    }

    private fun setupDurationButtons() {
        val durationButtons = listOf(
            binding.btnDuration6 to 6,
            binding.btnDuration12 to 12,
            binding.btnDuration24 to 24,
            binding.btnDuration48 to 48
        )

        durationButtons.forEach { (button, duration) ->
            button.setOnClickListener {
                selectDuration(duration, button)
            }
        }

        // Select default duration
        selectDuration(24, binding.btnDuration24)
    }

    private fun selectDuration(duration: Int, button: View) {
        selectedDuration = duration

        // Reset all buttons
        binding.btnDuration6.isSelected = false
        binding.btnDuration12.isSelected = false
        binding.btnDuration24.isSelected = false
        binding.btnDuration48.isSelected = false

        // Select current button
        button.isSelected = true
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showCameraContent()
                    1 -> showGalleryContent()
                    2 -> showTextContent()
                    3 -> showAudioContent()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Show camera by default
        showCameraContent()
    }

    internal fun showCameraContent() {
        hideAllContent()
        binding.layoutCamera.visibility = View.VISIBLE

        // TODO: Setup camera
        binding.btnCapture.setOnClickListener {
            // TODO: Capture photo/video
        }
    }

    internal fun showGalleryContent() {
        hideAllContent()
        binding.rvGallery.visibility = View.VISIBLE

        // TODO: Setup gallery
    }

    internal fun showTextContent() {
        hideAllContent()
        binding.layoutText.visibility = View.VISIBLE

        // TODO: Setup text editor
        setupTextEditor()
    }

    internal fun showAudioContent() {
        hideAllContent()
        binding.layoutAudio.visibility = View.VISIBLE

        // TODO: Setup audio recorder
        setupAudioRecorder()
    }

    private fun hideAllContent() {
        binding.layoutCamera.visibility = View.GONE
        binding.rvGallery.visibility = View.GONE
        binding.layoutText.visibility = View.GONE
        binding.layoutAudio.visibility = View.GONE
    }

    private fun setupTextEditor() {
        // Text alignment buttons
        binding.btnAlignLeft.setOnClickListener {
            // TODO: Set text alignment
        }

        binding.btnAlignCenter.setOnClickListener {
            // TODO: Set text alignment
        }

        binding.btnAlignRight.setOnClickListener {
            // TODO: Set text alignment
        }

        binding.btnFontBold.setOnClickListener {
            // TODO: Toggle bold
        }
    }

    private fun setupAudioRecorder() {
        binding.btnRecord.setOnClickListener {
            // TODO: Start/stop audio recording
        }
    }

    private fun publishStory() {
        // TODO: Create JournalEntry and publish
        // TODO: Show progress
        // TODO: Navigate back to JournalFragment
        finish()
    }
}
