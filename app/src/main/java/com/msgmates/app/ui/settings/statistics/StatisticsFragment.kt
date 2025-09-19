package com.msgmates.app.ui.settings.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.msgmates.app.databinding.FragmentStatisticsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StatisticsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
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
            // Cleanup wizard
            btnCleanupWizard.setOnClickListener {
                viewModel.startCleanupWizard()
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.apply {
                    // Update message statistics
                    tvSentMessages.text = state.sentMessages.toString()
                    tvReceivedMessages.text = state.receivedMessages.toString()
                    tvTotalMessages.text = (state.sentMessages + state.receivedMessages).toString()

                    // Update storage statistics
                    tvTextStorage.text = state.textStorageUsed
                    tvMediaStorage.text = state.mediaStorageUsed
                    tvTotalStorage.text = state.totalStorageUsed

                    // Update contact and group statistics
                    tvContactCount.text = state.contactCount.toString()
                    tvGroupCount.text = state.groupCount.toString()

                    // Update backup information
                    tvLastBackup.text = state.lastBackupDate

                    // Loading state
                    if (state.isLoading) {
                        progressBar.visibility = View.VISIBLE
                    } else {
                        progressBar.visibility = View.GONE
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
