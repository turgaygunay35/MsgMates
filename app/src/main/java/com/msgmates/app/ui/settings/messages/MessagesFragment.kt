package com.msgmates.app.ui.settings.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.msgmates.app.databinding.FragmentMessagesBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MessagesFragment : Fragment() {

    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MessagesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
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
        val messagesItems = listOf(
            MessagesItem(
                id = "read_receipts",
                title = "Okundu Bilgisi",
                subtitle = "Mesajların okunduğunu göster",
                showSwitch = true
            ),
            MessagesItem(
                id = "typing_indicator",
                title = "Yazıyor Göstergesi",
                subtitle = "Karşı tarafın yazdığını göster",
                showSwitch = true
            ),
            MessagesItem(
                id = "message_archive",
                title = "Mesaj Arşivi",
                subtitle = "Eski mesajları arşivle",
                showArrow = true
            ),
            MessagesItem(
                id = "message_backup",
                title = "Mesaj Yedekleme",
                subtitle = "Sohbet geçmişini yedekle",
                showArrow = true
            ),
            MessagesItem(
                id = "message_cleanup",
                title = "Mesaj Temizleme",
                subtitle = "Eski mesajları otomatik sil",
                showArrow = true
            ),
            MessagesItem(
                id = "message_export",
                title = "Mesaj Dışa Aktarma",
                subtitle = "Mesajları dosya olarak dışa aktar",
                showArrow = true
            )
        )

        val adapter = MessagesAdapter { item ->
            when (item.id) {
                "read_receipts" -> {
                    viewModel.toggleReadReceipts()
                }
                "typing_indicator" -> {
                    viewModel.toggleTypingIndicator()
                }
                "message_archive" -> {
                    viewModel.openMessageArchive()
                }
                "message_backup" -> {
                    viewModel.openMessageBackup()
                }
                "message_cleanup" -> {
                    viewModel.openMessageCleanup()
                }
                "message_export" -> {
                    viewModel.openMessageExport()
                }
            }
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        adapter.submitList(messagesItems)
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
