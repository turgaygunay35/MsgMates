package com.msgmates.app.ui.settings.general

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.msgmates.app.databinding.FragmentGeneralBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GeneralFragment : Fragment() {

    private var _binding: FragmentGeneralBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GeneralViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGeneralBinding.inflate(inflater, container, false)
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
        val generalItems = listOf(
            GeneralItem(
                id = "media_download",
                title = "Medya İndirme",
                subtitle = "Fotoğraf, video ve ses dosyalarını otomatik indir",
                showArrow = true
            ),
            GeneralItem(
                id = "backup",
                title = "Yedekleme",
                subtitle = "Sohbet geçmişini yedekle ve geri yükle",
                showArrow = true
            ),
            GeneralItem(
                id = "shortcuts",
                title = "Kısayollar",
                subtitle = "Ana ekran kısayollarını yönet",
                showArrow = true
            ),
            GeneralItem(
                id = "storage",
                title = "Depolama",
                subtitle = "Uygulama verilerini temizle",
                showArrow = true
            ),
            GeneralItem(
                id = "advanced",
                title = "Gelişmiş",
                subtitle = "Gelişmiş ayarlar ve seçenekler",
                showArrow = true
            )
        )

        val adapter = GeneralAdapter { item ->
            when (item.id) {
                "media_download" -> {
                    // Navigate to media download settings
                    viewModel.openMediaDownloadSettings()
                }
                "backup" -> {
                    // Navigate to backup settings
                    viewModel.openBackupSettings()
                }
                "shortcuts" -> {
                    // Navigate to shortcuts settings
                    viewModel.openShortcutsSettings()
                }
                "storage" -> {
                    // Navigate to storage settings
                    viewModel.openStorageSettings()
                }
                "advanced" -> {
                    // Navigate to advanced settings
                    viewModel.openAdvancedSettings()
                }
            }
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        adapter.submitList(generalItems)
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
