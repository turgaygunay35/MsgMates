package com.msgmates.app.ui.journal.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.msgmates.app.databinding.FragmentJournalListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class JournalListFragment : Fragment() {

    private var _binding: FragmentJournalListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: JournalListViewModel by viewModels()
    private lateinit var adapter: JournalListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJournalListBinding.inflate(inflater, container, false)
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
                //     JournalListFragmentDirections.actionJournalToDetail(entryId)
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
            adapter = this@JournalListFragment.adapter
        }
    }

    private fun setupClickListeners() {
        binding.fabAdd.setOnClickListener {
            // COMMENTED OUT FOR CLEAN BUILD
            // findNavController().navigate(
            //     JournalListFragmentDirections.actionJournalToEdit(null)
            // )
        }

        // TODO: Implement search functionality

        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                com.msgmates.app.R.id.action_filter -> {
                    // TODO: Show filter bottom sheet
                    true
                }
                com.msgmates.app.R.id.action_sort -> {
                    // TODO: Show sort bottom sheet
                    true
                }
                com.msgmates.app.R.id.action_multi_select -> {
                    viewModel.toggleMultiSelectMode()
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
                binding.toolbar.menu.findItem(com.msgmates.app.R.id.action_multi_select)?.isVisible = !isMultiSelect
                binding.toolbar.menu.findItem(com.msgmates.app.R.id.action_delete_selected)?.isVisible = isMultiSelect
                binding.toolbar.menu.findItem(com.msgmates.app.R.id.action_archive_selected)?.isVisible = isMultiSelect
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
