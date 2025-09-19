package com.msgmates.app.ui.journal.muted

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.msgmates.app.databinding.ActivityMutedUsersBinding
import com.msgmates.app.ui.journal.JournalViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MutedUsersActivity : ComponentActivity() {

    private lateinit var binding: ActivityMutedUsersBinding
    private lateinit var viewModel: JournalViewModel
    private lateinit var adapter: MutedUsersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMutedUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[JournalViewModel::class.java]

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        // Toolbar
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // RecyclerView
        adapter = MutedUsersAdapter { userId ->
            viewModel.toggleMute(userId)
        }

        binding.rvMutedUsers.layoutManager = LinearLayoutManager(this)
        binding.rvMutedUsers.adapter = adapter
    }

    private fun observeViewModel() {
        // TODO: Observe muted users and update adapter
        // For now, show empty state
        binding.layoutEmpty.visibility = android.view.View.VISIBLE
        binding.rvMutedUsers.visibility = android.view.View.GONE
    }
}
