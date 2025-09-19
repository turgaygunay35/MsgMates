package com.msgmates.app.ui.journal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.msgmates.app.R
import com.msgmates.app.data.journal.model.JournalEntry
import com.msgmates.app.databinding.FragmentJournalBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class JournalFragment : Fragment() {

    private var _binding: FragmentJournalBinding? = null
    private val binding get() = _binding!!

    private val viewModel: JournalViewModel by viewModels()
    private lateinit var adapter: JournalAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJournalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        // Setup RecyclerView
        adapter = JournalAdapter(
            onItemClick = { entry ->
                openStoryViewer(entry)
            },
            onItemLongClick = { entry ->
                showStoryOptions(entry)
            },
            onOverflowClick = { entry, view ->
                showOverflowMenu(entry, view)
            }
        )

        binding.rvStories.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvStories.adapter = adapter

        // Add story button - show menu with only "Günlük Paylaş"
        binding.btnAddStory.setOnClickListener {
            showAddStoryMenu()
        }

        // Bottom action buttons
        binding.btnMuted.setOnClickListener {
            openMutedStories()
        }

        binding.btnFollowing.setOnClickListener {
            openFollowingStories()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
    }

    private fun updateUI(state: JournalUiState) {
        // Loading state
        binding.progressBar.visibility = if (state.isLoading) {
            View.VISIBLE
        } else {
            View.GONE
        }

        // Empty state
        binding.layoutEmpty.visibility = if (state.stories.isEmpty() && !state.isLoading) {
            View.VISIBLE
        } else {
            View.GONE
        }

        // Stories list
        adapter.submitList(state.stories)
    }

    private fun openStoryViewer(entry: JournalEntry) {
        // Story görüntülendiğinde izlenenler listesine ekle
        viewModel.onStoryViewed(entry.userId, entry.userName, entry.profileImageUrl)

        // TODO: Navigate to JournalViewerActivity
        // findNavController().navigate(JournalFragmentDirections.actionJournalToViewer(entry))
    }

    private fun showAddStoryMenu() {
        val popupMenu = androidx.appcompat.widget.PopupMenu(requireContext(), binding.btnAddStory)
        popupMenu.menuInflater.inflate(R.menu.menu_journal_add, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_share_daily -> {
                    openStoryComposer()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun openStoryComposer() {
        // Navigate to DailyShareFragment with time options
        findNavController().navigate(R.id.dest_daily_share)
    }

    private fun openMutedStories() {
        val intent = android.content.Intent(
            requireContext(),
            com.msgmates.app.ui.journal.muted.MutedUsersActivity::class.java
        )
        startActivity(intent)
    }

    private fun openFollowingStories() {
        val intent = android.content.Intent(
            requireContext(),
            com.msgmates.app.ui.journal.watched.WatchedUsersActivity::class.java
        )
        startActivity(intent)
    }

    private fun showStoryOptions(entry: JournalEntry) {
        // TODO: Create bottom sheet layout and setup options
        // - Sessize Al
        // - Favorilere Ekle
        // - Kullanıcıyı Gizle
        // - Bildirimleri Aç/Kapat
    }

    private fun showOverflowMenu(entry: JournalEntry, anchorView: android.view.View) {
        val popupMenu = androidx.appcompat.widget.PopupMenu(requireContext(), anchorView)
        popupMenu.menuInflater.inflate(R.menu.menu_journal_overflow, popupMenu.menu)

        // Mute/Unmute menü item'ını güncelle
        val muteItem = popupMenu.menu.findItem(R.id.action_mute)
        if (viewModel.journalRepository.isMuted(entry.userId)) {
            muteItem.title = getString(R.string.journal_unmute)
        } else {
            muteItem.title = getString(R.string.journal_mute)
        }

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_mute -> {
                    viewModel.toggleMute(entry.userId)
                    val message = if (viewModel.journalRepository.isMuted(entry.userId)) {
                        "${entry.userName} sessize alındı"
                    } else {
                        "${entry.userName} sessizden çıkarıldı"
                    }
                    android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
