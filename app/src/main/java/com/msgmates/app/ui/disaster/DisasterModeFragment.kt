package com.msgmates.app.ui.disaster

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.msgmates.app.R
import com.msgmates.app.core.navigation.SafeNavigation
import com.msgmates.app.databinding.FragmentDisasterModeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DisasterModeFragment : Fragment() {

    private var _binding: FragmentDisasterModeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DisasterModeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDisasterModeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        // Main disaster mode toggle
        binding.switchDisasterMode.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleDisasterMode()
        }

        // Main safety button
        binding.btnImSafe.setOnClickListener {
            viewModel.broadcastImSafe()
        }

        // Quick message buttons
        binding.btnSos.setOnClickListener {
            viewModel.sendQuickMessage("SOS")
        }

        binding.btnWater.setOnClickListener {
            viewModel.sendQuickMessage("Su Lazım")
        }

        binding.btnOk.setOnClickListener {
            viewModel.sendQuickMessage("İyiyim")
        }

        // Tools
        binding.btnFlashlight.setOnClickListener {
            viewModel.toggleFlashlight()
        }

        binding.btnSiren.setOnClickListener {
            viewModel.toggleSiren()
        }

        // Energy saving switch
        binding.switchEnergySaving.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setEnergySaving(isChecked)
        }

        // Offline chat
        binding.btnOfflineChat.setOnClickListener {
            viewModel.openOfflineChat()
        }

        // Emergency info (always available)
        binding.btnEmergencyInfo.setOnClickListener {
            openEmergencyNumbers()
        }

        // Disaster settings
        binding.btnDisasterSettings.setOnClickListener {
            openDisasterSettings()
        }

        // Back button
        binding.btnBack.setOnClickListener {
            SafeNavigation.safeNavigate(this, findNavController().currentDestination?.id ?: 0) {
                findNavController().navigateUp()
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
    }

    private fun openEmergencyNumbers() {
        val intent = Intent(requireContext(), EmergencyNumbersActivity::class.java)
        startActivity(intent)
    }

    private fun openDisasterSettings() {
        val intent = Intent(requireContext(), DisasterSettingsActivity::class.java)
        startActivity(intent)
    }

    private fun updateUI(state: DisasterModeUiState) {
        // Update main toggle only if changed
        if (binding.switchDisasterMode.isChecked != state.isDisasterEnabled) {
            binding.switchDisasterMode.isChecked = state.isDisasterEnabled
        }

        // Update status text and color
        val statusText = when {
            !state.isDisasterEnabled -> "Afet modu kapalı - Özellikler devre dışı"
            state.broadcastSuccess -> "✅ Mesaj başarıyla yayınlandı"
            state.error != null -> "❌ Hata: ${state.error}"
            state.isBroadcasting -> "📡 Mesaj yayınlanıyor..."
            else -> "Afet modu aktif - Tüm özellikler kullanılabilir"
        }

        if (binding.tvStatus.text != statusText) {
            binding.tvStatus.text = statusText
        }

        val statusColor = when {
            !state.isDisasterEnabled -> requireContext().getColor(android.R.color.darker_gray)
            state.broadcastSuccess -> requireContext().getColor(android.R.color.holo_green_dark)
            state.error != null -> requireContext().getColor(android.R.color.holo_red_dark)
            state.isBroadcasting -> requireContext().getColor(android.R.color.holo_blue_dark)
            else -> requireContext().getColor(android.R.color.holo_green_dark)
        }

        if (binding.tvStatus.currentTextColor != statusColor) {
            binding.tvStatus.setTextColor(statusColor)
        }

        // Enable/disable all disaster-related buttons based on mode
        val isEnabled = state.isDisasterEnabled && !state.isBroadcasting

        // Only update button states if they changed
        if (binding.btnImSafe.isEnabled != isEnabled) {
            binding.btnImSafe.isEnabled = isEnabled
        }
        if (binding.btnSos.isEnabled != isEnabled) {
            binding.btnSos.isEnabled = isEnabled
        }
        if (binding.btnWater.isEnabled != isEnabled) {
            binding.btnWater.isEnabled = isEnabled
        }
        if (binding.btnOk.isEnabled != isEnabled) {
            binding.btnOk.isEnabled = isEnabled
        }
        if (binding.btnFlashlight.isEnabled != isEnabled) {
            binding.btnFlashlight.isEnabled = isEnabled
        }
        if (binding.btnSiren.isEnabled != isEnabled) {
            binding.btnSiren.isEnabled = isEnabled
        }
        if (binding.btnOfflineChat.isEnabled != state.isDisasterEnabled) {
            binding.btnOfflineChat.isEnabled = state.isDisasterEnabled
        }
        if (binding.switchEnergySaving.isEnabled != state.isDisasterEnabled) {
            binding.switchEnergySaving.isEnabled = state.isDisasterEnabled
        }

        // Progress bar visibility
        val progressVisibility = if (state.isBroadcasting) View.VISIBLE else View.GONE
        if (binding.progressBar.visibility != progressVisibility) {
            binding.progressBar.visibility = progressVisibility
        }

        // Flashlight button text
        val flashlightText = if (state.isFlashlightOn) "🔦 Fener (Açık)" else "🔦 Fener"
        if (binding.btnFlashlight.text != flashlightText) {
            binding.btnFlashlight.text = flashlightText
        }

        // Siren button text
        val sirenText = if (state.isSirenOn) "🚨 Siren (Açık)" else "🚨 Siren"
        if (binding.btnSiren.text != sirenText) {
            binding.btnSiren.text = sirenText
        }

        // Energy saving switch
        if (binding.switchEnergySaving.isChecked != state.isEnergySaving) {
            binding.switchEnergySaving.isChecked = state.isEnergySaving
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
