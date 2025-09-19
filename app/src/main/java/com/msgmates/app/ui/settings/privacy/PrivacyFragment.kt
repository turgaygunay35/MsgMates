package com.msgmates.app.ui.settings.privacy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.msgmates.app.databinding.FragmentPrivacyBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PrivacyFragment : Fragment() {

    private var _binding: FragmentPrivacyBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PrivacyViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrivacyBinding.inflate(inflater, container, false)
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
        val privacyItems = listOf(
            PrivacyItem(
                id = "last_seen",
                title = "Son Görülme",
                subtitle = "Son görülme zamanını göster",
                showSwitch = true
            ),
            PrivacyItem(
                id = "online_status",
                title = "Çevrimiçi Durumu",
                subtitle = "Çevrimiçi olduğunu göster",
                showSwitch = true
            ),
            PrivacyItem(
                id = "profile_photo",
                title = "Profil Fotoğrafı",
                subtitle = "Profil fotoğrafını kimler görebilir",
                showArrow = true
            ),
            PrivacyItem(
                id = "status_message",
                title = "Durum Mesajı",
                subtitle = "Durum mesajını kimler görebilir",
                showArrow = true
            ),
            PrivacyItem(
                id = "blocked_users",
                title = "Engellenen Kullanıcılar",
                subtitle = "Engellenen kişileri yönet",
                showArrow = true
            ),
            PrivacyItem(
                id = "blocked_by",
                title = "Beni Engelleyenler",
                subtitle = "Sizi engelleyen kişileri gör",
                showArrow = true
            ),
            PrivacyItem(
                id = "read_receipts",
                title = "Okundu Bilgisi",
                subtitle = "Mesajların okunduğunu göster",
                showSwitch = true
            )
        )

        val adapter = PrivacyAdapter { item ->
            when (item.id) {
                "last_seen" -> {
                    viewModel.toggleLastSeen()
                }
                "online_status" -> {
                    viewModel.toggleOnlineStatus()
                }
                "profile_photo" -> {
                    viewModel.openProfilePhotoSettings()
                }
                "status_message" -> {
                    viewModel.openStatusMessageSettings()
                }
                "blocked_users" -> {
                    viewModel.openBlockedUsers()
                }
                "blocked_by" -> {
                    viewModel.openBlockedBy()
                }
                "read_receipts" -> {
                    viewModel.toggleReadReceipts()
                }
            }
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = adapter
        }

        adapter.submitList(privacyItems)
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
