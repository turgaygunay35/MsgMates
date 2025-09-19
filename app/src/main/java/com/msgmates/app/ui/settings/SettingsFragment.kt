package com.msgmates.app.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.msgmates.app.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()
    private lateinit var settingsAdapter: SettingsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
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
        settingsAdapter = SettingsAdapter { settingItem ->
            when (settingItem.id) {
                "profile" -> findNavController().navigate(SettingsFragmentDirections.actionSettingsToProfile())
                "general" -> findNavController().navigate(SettingsFragmentDirections.actionSettingsToGeneral())
                "messages" -> findNavController().navigate(SettingsFragmentDirections.actionSettingsToMessages())
                "privacy" -> findNavController().navigate(SettingsFragmentDirections.actionSettingsToPrivacy())
                "security" -> findNavController().navigate(SettingsFragmentDirections.actionSettingsToSecurity())
                "notifications" -> findNavController().navigate(
                    SettingsFragmentDirections.actionSettingsToNotifications()
                )
                "theme" -> findNavController().navigate(SettingsFragmentDirections.actionSettingsToTheme())
                "language" -> findNavController().navigate(SettingsFragmentDirections.actionSettingsToLanguage())
                "statistics" -> findNavController().navigate(SettingsFragmentDirections.actionSettingsToStatistics())
                "help" -> findNavController().navigate(SettingsFragmentDirections.actionSettingsToHelp())
                "logout" -> viewModel.logout()
            }
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = settingsAdapter
        }

        // Set up settings items
        val settingsItems = listOf(
            SettingsItem(
                id = "profile",
                title = "Profil",
                subtitle = "Kişisel bilgilerinizi düzenleyin",
                showArrow = true
            ),
            SettingsItem(
                id = "general",
                title = "Genel",
                subtitle = "Genel uygulama ayarları",
                showArrow = true
            ),
            SettingsItem(
                id = "messages",
                title = "Mesajlar",
                subtitle = "Mesaj gönderme ve alma ayarları",
                showArrow = true
            ),
            SettingsItem(
                id = "privacy",
                title = "Gizlilik",
                subtitle = "Gizlilik ve veri koruma ayarları",
                showArrow = true
            ),
            SettingsItem(
                id = "security",
                title = "Güvenlik",
                subtitle = "Hesap güvenliği ve kimlik doğrulama",
                showArrow = true
            ),
            SettingsItem(
                id = "notifications",
                title = "Bildirimler",
                subtitle = "Bildirim ayarlarını yönetin",
                showArrow = true
            ),
            SettingsItem(
                id = "theme",
                title = "Tema",
                subtitle = "Görsel tema ve renk ayarları",
                showArrow = true
            ),
            SettingsItem(
                id = "language",
                title = "Dil",
                subtitle = "Uygulama dili seçimi",
                showArrow = true
            ),
            SettingsItem(
                id = "statistics",
                title = "İstatistikler",
                subtitle = "Kullanım istatistikleri ve raporlar",
                showArrow = true
            ),
            SettingsItem(
                id = "help",
                title = "Yardım",
                subtitle = "Yardım ve destek",
                showArrow = true
            ),
            SettingsItem(
                id = "logout",
                title = "Çıkış",
                subtitle = "Hesaptan çıkış yap",
                showArrow = false
            )
        )

        settingsAdapter.submitList(settingsItems)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
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

data class SettingsItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val showArrow: Boolean
)
