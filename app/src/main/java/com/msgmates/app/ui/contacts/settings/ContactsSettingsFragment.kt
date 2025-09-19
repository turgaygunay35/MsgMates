package com.msgmates.app.ui.contacts.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.msgmates.app.databinding.FragmentContactsSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ContactsSettingsFragment : Fragment() {

    private var _binding: FragmentContactsSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ContactsSettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactsSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupClickListeners() {
        // Highlight MsgMates users switch
        binding.switchHighlightMsgMates.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setHighlightMsgMatesUsers(isChecked)
        }

        // Sync only WiFi switch
        binding.switchSyncOnlyWifi.setOnCheckedChangeListener { _, isChecked ->
            // viewModel.setSyncOnlyWifi(isChecked) // Geçici olarak yoruma alındı
        }

        // Sync period radio buttons
        binding.radioHourly.setOnClickListener {
            // viewModel.setSyncPeriod(SyncPeriod.HOURLY) // Geçici olarak yoruma alındı
        }

        binding.radioDaily.setOnClickListener {
            // viewModel.setSyncPeriod(SyncPeriod.DAILY) // Geçici olarak yoruma alındı
        }

        binding.radioOff.setOnClickListener {
            // viewModel.setSyncPeriod(SyncPeriod.OFF) // Geçici olarak yoruma alındı
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.highlightFlow.collect { isHighlighted ->
                // Update UI based on highlight preference
                binding.switchHighlightMsgMates.isChecked = isHighlighted
            }
        }
    }

    // updateUI fonksiyonu artık gerekli değil - observeViewModel'de hallediyoruz

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
