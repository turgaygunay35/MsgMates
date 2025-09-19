package com.msgmates.app.ui.journal

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import com.google.android.material.tabs.TabLayout
import com.msgmates.app.databinding.ActivityJournalPrivacyBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class JournalPrivacyActivity : ComponentActivity() {

    private lateinit var binding: ActivityJournalPrivacyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityJournalPrivacyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    private fun setupUI() {
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Save button
        binding.btnSave.setOnClickListener {
            saveSettings()
        }

        // Tab layout
        setupTabLayout()

        // Privacy settings
        setupPrivacySettings()
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showMutedUsers()
                    1 -> showFollowingUsers()
                    2 -> showPrivacySettings()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Show muted users by default
        showMutedUsers()
    }

    internal fun showMutedUsers() {
        hideAllContent()
        binding.layoutMuted.visibility = View.VISIBLE

        // TODO: Setup muted users list
        binding.btnAddMuted.setOnClickListener {
            // TODO: Add muted user
        }
    }

    internal fun showFollowingUsers() {
        hideAllContent()
        binding.layoutFollowing.visibility = View.VISIBLE

        // TODO: Setup following users list
        binding.btnAddFollowing.setOnClickListener {
            // TODO: Add following user
        }
    }

    internal fun showPrivacySettings() {
        hideAllContent()
        binding.layoutPrivacy.visibility = View.VISIBLE
    }

    private fun hideAllContent() {
        binding.layoutMuted.visibility = View.GONE
        binding.layoutFollowing.visibility = View.GONE
        binding.layoutPrivacy.visibility = View.GONE
    }

    private fun setupPrivacySettings() {
        // Privacy radio buttons
        binding.radioEveryone.setOnClickListener {
            // TODO: Set privacy to everyone
        }

        binding.radioSelected.setOnClickListener {
            // TODO: Set privacy to selected
        }

        binding.radioPrivate.setOnClickListener {
            // TODO: Set privacy to private
        }

        // Notification switch
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            // TODO: Save notification preference
        }
    }

    private fun saveSettings() {
        // TODO: Save all settings
        finish()
    }
}
