package com.msgmates.app.ui.settings.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.msgmates.app.databinding.FragmentArchiveCleanupBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ArchiveCleanupFragment : Fragment() {

    private var _binding: FragmentArchiveCleanupBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ArchiveCleanupViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArchiveCleanupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupUI()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupUI() {
        binding.apply {
            // Auto archive switch
            switchAutoArchive.setOnCheckedChangeListener { _, isChecked ->
                viewModel.updateAutoArchive(isChecked)
            }

            // Auto cleanup switch
            switchAutoCleanup.setOnCheckedChangeListener { _, isChecked ->
                viewModel.updateAutoCleanup(isChecked)
            }

            // Cleanup now button
            btnCleanupNow.setOnClickListener {
                viewModel.cleanupNow()
            }

            // Archive all button
            btnArchiveAll.setOnClickListener {
                viewModel.archiveAll()
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.apply {
                    // Update switches
                    switchAutoArchive.isChecked = state.autoArchiveEnabled
                    switchAutoCleanup.isChecked = state.autoCleanupEnabled

                    // Loading state
                    if (state.isLoading) {
                        progressBar.visibility = View.VISIBLE
                        btnCleanupNow.isEnabled = false
                        btnArchiveAll.isEnabled = false
                    } else {
                        progressBar.visibility = View.GONE
                        btnCleanupNow.isEnabled = true
                        btnArchiveAll.isEnabled = true
                    }

                    // Success message
                    if (state.showSuccessMessage) {
                        // Show success message
                        viewModel.clearSuccessMessage()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
