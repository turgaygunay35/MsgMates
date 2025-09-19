package com.msgmates.app.ui.journal.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.msgmates.app.databinding.FragmentJournalDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class JournalDetailFragment : Fragment() {

    private var _binding: FragmentJournalDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: JournalDetailViewModel by viewModels()
    // COMMENTED OUT FOR CLEAN BUILD
    // private val args: JournalDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJournalDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeViewModel()
        // COMMENTED OUT FOR CLEAN BUILD
        // viewModel.loadEntry(args.entryId)
    }

    private fun setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                com.msgmates.app.R.id.action_edit -> {
                    // COMMENTED OUT FOR CLEAN BUILD
                    // findNavController().navigate(
                    //     JournalDetailFragmentDirections.actionJournalDetailToEdit(args.entryId)
                    // )
                    true
                }
                com.msgmates.app.R.id.action_share -> {
                    viewModel.shareEntry()
                    true
                }
                com.msgmates.app.R.id.action_favorite -> {
                    viewModel.toggleFavorite()
                    true
                }
                com.msgmates.app.R.id.action_archive -> {
                    viewModel.archiveEntry()
                    true
                }
                com.msgmates.app.R.id.action_delete -> {
                    viewModel.deleteEntry()
                    true
                }
                else -> false
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                binding.contentLayout.visibility = if (state.isLoading) View.GONE else View.VISIBLE

                state.entry?.let { entry ->
                    binding.textTitle.text = entry.title
                    binding.textContent.text = entry.content
                    binding.textDate.text = formatDate(entry.createdAt)
                    binding.textMood.text = entry.mood?.emoji ?: ""
                    binding.textMood.visibility = if (entry.mood != null) View.VISIBLE else View.GONE

                    // TODO: Load photos
                    // TODO: Load tags
                }

                state.error?.let { _ ->
                    // TODO: Show error snackbar
                }
            }
        }

        lifecycleScope.launch {
            viewModel.events.collect { event ->
                when (event) {
                    is JournalDetailEvent.FavoriteToggled -> {
                        // TODO: Update UI
                    }
                    is JournalDetailEvent.EntryArchived -> {
                        findNavController().navigateUp()
                    }
                    is JournalDetailEvent.EntryRestored -> {
                        // TODO: Update UI
                    }
                    is JournalDetailEvent.EntryDeleted -> {
                        findNavController().navigateUp()
                    }
                    is JournalDetailEvent.ShareEntry -> {
                        // TODO: Implement share
                    }
                    is JournalDetailEvent.Error -> {
                        // TODO: Show error
                    }
                    null -> { /* Clear event */ }
                }
                viewModel.clearEvent()
            }
        }
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("dd MMMM yyyy, HH:mm", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
