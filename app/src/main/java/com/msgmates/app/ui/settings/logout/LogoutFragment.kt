package com.msgmates.app.ui.settings.logout

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.msgmates.app.databinding.FragmentLogoutBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LogoutFragment : Fragment() {

    private var _binding: FragmentLogoutBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LogoutViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogoutBinding.inflate(inflater, container, false)
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
            // Server sessions switch
            switchServerSessions.setOnCheckedChangeListener { _, isChecked ->
                viewModel.updateServerSessions(isChecked)
            }

            // Logout button
            btnLogout.setOnClickListener {
                showLogoutConfirmationDialog()
            }

            // Cancel button
            btnCancel.setOnClickListener {
                findNavController().navigateUp()
            }
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Çıkış Yap")
            .setMessage("Sohbet geçmişi bu cihazdan silinecek. Çıkış yapmak istediğinizden emin misiniz?")
            .setPositiveButton("Çıkış Yap") { _, _ ->
                viewModel.logout()
            }
            .setNegativeButton("İptal") { _, _ ->
                // Do nothing
            }
            .setCancelable(true)
            .show()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.apply {
                    // Update switch
                    switchServerSessions.isChecked = state.closeServerSessions

                    // Loading state
                    if (state.isLoading) {
                        progressBar.visibility = View.VISIBLE
                        btnLogout.isEnabled = false
                        btnCancel.isEnabled = false
                    } else {
                        progressBar.visibility = View.GONE
                        btnLogout.isEnabled = true
                        btnCancel.isEnabled = true
                    }

                    // Success message
                    if (state.showSuccessMessage) {
                        // Show success message and navigate back
                        findNavController().navigateUp()
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
