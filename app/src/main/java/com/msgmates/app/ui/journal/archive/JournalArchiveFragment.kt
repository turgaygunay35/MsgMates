package com.msgmates.app.ui.journal.archive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.msgmates.app.databinding.FragmentJournalArchiveBinding
import com.msgmates.app.ui.journal.list.JournalListAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class JournalArchiveFragment : Fragment() {

    private var _binding: FragmentJournalArchiveBinding? = null
    private val binding get() = _binding!!

    private val viewModel: JournalArchiveViewModel by viewModels()
    private lateinit var adapter: JournalListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJournalArchiveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = JournalListAdapter(
            onEntryClick = { entryId ->
                // COMMENTED OUT FOR CLEAN BUILD
                // findNavController().navigate(
                //     JournalArchiveFragmentDirections.actionJournalArchiveToDetail(entryId)
                // )
            },
            onFavoriteClick = { _ ->
                // TODO: Implement favorite toggle
            },
            onSelectionChanged = { entryId, _ ->
                viewModel.toggleEntrySelection(entryId)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@JournalArchiveFragment.adapter
        }
    }

    private fun setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                com.msgmates.app.R.id.action_restore_selected -> {
                    viewModel.restoreSelectedEntries()
                    true
                }
                com.msgmates.app.R.id.action_delete_selected -> {
                    viewModel.deleteSelectedEntries()
                    true
                }
                else -> false
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                adapter.submitList(state.entries)

                binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                binding.emptyState.visibility = if (state.isEmpty) View.VISIBLE else View.GONE
                binding.recyclerView.visibility = if (state.isEmpty) View.GONE else View.VISIBLE

                state.error?.let { _ ->
                    // TODO: Show error snackbar
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isMultiSelectMode.collect { isMultiSelect ->
                adapter.setMultiSelectMode(isMultiSelect)
                binding.toolbar.menu.findItem(com.msgmates.app.R.id.action_restore_selected)?.isVisible = isMultiSelect
                binding.toolbar.menu.findItem(com.msgmates.app.R.id.action_delete_selected)?.isVisible = isMultiSelect
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
