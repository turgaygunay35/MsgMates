package com.msgmates.app.ui.settings.security

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.msgmates.app.databinding.FragmentSecurityBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SecurityFragment : Fragment() {

    private var _binding: FragmentSecurityBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SecurityViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecurityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        val securityItems = listOf(
            SecurityItem(
                id = "biometric",
                title = "Biyometrik Kimlik Doğrulama",
                subtitle = "Parmak izi veya yüz tanıma ile giriş",
                showSwitch = true
            ),
            SecurityItem(
                id = "auto_lock",
                title = "Otomatik Kilitleme",
                subtitle = "Belirli süre sonra uygulamayı kilitle",
                showSwitch = true
            ),
            SecurityItem(
                id = "screen_capture",
                title = "Ekran Görüntüsü",
                subtitle = "Ekran görüntüsü almayı engelle",
                showSwitch = true
            ),
            SecurityItem(
                id = "two_factor",
                title = "İki Faktörlü Kimlik Doğrulama",
                subtitle = "SMS veya uygulama ile doğrulama",
                showArrow = true
            ),
            SecurityItem(
                id = "session_management",
                title = "Oturum Yönetimi",
                subtitle = "Aktif oturumları görüntüle ve yönet",
                showArrow = true
            ),
            SecurityItem(
                id = "security_log",
                title = "Güvenlik Günlüğü",
                subtitle = "Güvenlik olaylarını görüntüle",
                showArrow = true
            ),
            SecurityItem(
                id = "change_password",
                title = "Şifre Değiştir",
                subtitle = "Hesap şifrenizi değiştirin",
                showArrow = true
            )
        )

        val adapter = SecurityAdapter { item ->
            when (item.id) {
                "biometric" -> {
                    viewModel.toggleBiometric()
                }
                "auto_lock" -> {
                    viewModel.toggleAutoLock()
                }
                "screen_capture" -> {
                    viewModel.toggleScreenCapture()
                }
                "two_factor" -> {
                    viewModel.openTwoFactorAuth()
                }
                "session_management" -> {
                    viewModel.openSessionManagement()
                }
                "security_log" -> {
                    viewModel.openSecurityLog()
                }
                "change_password" -> {
                    viewModel.openChangePassword()
                }
            }
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        adapter.submitList(securityItems)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.uiState.collect { state ->
                if (state.showSuccessMessage) {
                    // Show success message
                    viewModel.clearSuccessMessage()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
