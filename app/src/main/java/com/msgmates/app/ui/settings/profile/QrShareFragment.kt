package com.msgmates.app.ui.settings.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.msgmates.app.databinding.FragmentQrShareBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class QrShareFragment : Fragment() {

    private var _binding: FragmentQrShareBinding? = null
    private val binding get() = _binding!!

    private val viewModel: QrShareViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQrShareBinding.inflate(inflater, container, false)
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
            btnGenerateQr.setOnClickListener {
                viewModel.generateQrCode()
            }

            btnShareQr.setOnClickListener {
                viewModel.shareQrCode()
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.apply {
                    if (state.qrBitmap != null) {
                        ivQrCode.setImageBitmap(state.qrBitmap)
                        ivQrCode.visibility = View.VISIBLE
                        btnShareQr.isEnabled = true
                    } else {
                        ivQrCode.visibility = View.GONE
                        btnShareQr.isEnabled = false
                    }

                    if (state.isLoading) {
                        progressBar.visibility = View.VISIBLE
                        btnGenerateQr.isEnabled = false
                    } else {
                        progressBar.visibility = View.GONE
                        btnGenerateQr.isEnabled = true
                    }

                    if (state.error != null) {
                        // Show error message
                        viewModel.clearError()
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
