package com.msgmates.app.ui.journal.watched

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.msgmates.app.databinding.ActivityWatchedUsersBinding
import com.msgmates.app.ui.journal.JournalViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WatchedUsersActivity : ComponentActivity() {

    private lateinit var binding: ActivityWatchedUsersBinding
    private lateinit var viewModel: JournalViewModel
    private lateinit var adapter: WatchedUsersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWatchedUsersBinding.inflate(layoutInflater)
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
        adapter = WatchedUsersAdapter()

        binding.rvWatchedUsers.layoutManager = LinearLayoutManager(this)
        binding.rvWatchedUsers.adapter = adapter
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.watchedUsers.collect { watchedUsers ->
                if (watchedUsers.isEmpty()) {
                    binding.layoutEmpty.visibility = android.view.View.VISIBLE
                    binding.rvWatchedUsers.visibility = android.view.View.GONE
                } else {
                    binding.layoutEmpty.visibility = android.view.View.GONE
                    binding.rvWatchedUsers.visibility = android.view.View.VISIBLE
                    adapter.submitList(watchedUsers)
                }
            }
        }
    }
}
