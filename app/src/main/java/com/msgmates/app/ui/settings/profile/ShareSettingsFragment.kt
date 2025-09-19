package com.msgmates.app.ui.settings.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.msgmates.app.databinding.FragmentShareSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ShareSettingsFragment : Fragment() {

    private var _binding: FragmentShareSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ShareSettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShareSettingsBinding.inflate(inflater, container, false)
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
            switchShareName.setOnCheckedChangeListener { _, isChecked ->
                viewModel.updateShareName(isChecked)
            }

            switchShareStatus.setOnCheckedChangeListener { _, isChecked ->
                viewModel.updateShareStatus(isChecked)
            }

            switchSharePhoto.setOnCheckedChangeListener { _, isChecked ->
                viewModel.updateSharePhoto(isChecked)
            }

            btnSave.setOnClickListener {
                viewModel.saveShareSettings()
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.apply {
                    switchShareName.isChecked = state.shareName
                    switchShareStatus.isChecked = state.shareStatus
                    switchSharePhoto.isChecked = state.sharePhoto

                    if (state.isLoading) {
                        progressBar.visibility = View.VISIBLE
                        btnSave.isEnabled = false
                    } else {
                        progressBar.visibility = View.GONE
                        btnSave.isEnabled = true
                    }

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
