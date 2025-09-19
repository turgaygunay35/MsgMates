package com.msgmates.app.ui.contacts

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.msgmates.app.R
import com.msgmates.app.databinding.FragmentContactsBinding
import com.msgmates.app.ui.contacts.permission.ContactsPermissionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ContactsFragment : Fragment() {

    private var _binding: FragmentContactsBinding? = null
    internal val binding get() = _binding!!

    internal val viewModel: ContactsViewModel by viewModels()

    @Inject
    lateinit var permissionManager: ContactsPermissionManager

    private lateinit var contactsAdapter: ContactsAdapter
    private var permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        handlePermissionResult(isGranted)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeRefresh()
        setupSearch()
        setupFilters()
        setupPermissionButtons()
        observeViewModel()
        checkPermissionAndLoadContacts()
    }

    private fun setupRecyclerView() {
        contactsAdapter = ContactsAdapter(
            onContactClick = { contact ->
                // Navigate to contact detail with contactId
                findNavController().navigate(R.id.dest_contact_detail)
            },
            onCallClick = { phoneNumber ->
                makeCall(phoneNumber)
            },
            onMessageClick = { contact ->
                // Navigate to chat with contactId
                findNavController().navigate(R.id.dest_contacts) // Geçici olarak contacts sayfasına git
            },
            onVideoCallClick = { contact ->
                // Navigate to video call with contactId
                findNavController().navigate(R.id.dest_contacts) // Geçici olarak contacts sayfasına git
            },
            onFavoriteToggle = { contact ->
                viewModel.toggleFavorite(contact.id)
            },
            onShareClick = { contact ->
                shareContact(contact)
            },
            onOpenInSystemClick = { contact ->
                openContactInSystem(contact.id)
            }
        )

        binding.rvContacts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = contactsAdapter

            // Performance optimizations for large lists
            setHasFixedSize(true) // RecyclerView knows item sizes are fixed
            setItemViewCacheSize(20) // Cache 20 views for smooth scrolling
            recycledViewPool.setMaxRecycledViews(0, 20) // Recycle view holders
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshContacts()
        }
    }

    private fun setupSearch() {
        // Debounced search for better performance
        var searchRunnable: Runnable? = null
        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                // Cancel previous search
                searchRunnable?.let { binding.etSearch.removeCallbacks(it) }

                // Schedule new search with 300ms delay
                searchRunnable = Runnable {
                    viewModel.searchContacts(s.toString())
                }
                binding.etSearch.postDelayed(searchRunnable!!, 300)
            }
        })
    }

    private fun setupFilters() {
        binding.chipFavorites.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setFavoritesFilter(isChecked)
        }

        binding.chipMsgMates.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setMsgMatesFilter(isChecked)
        }
    }

    private fun setupPermissionButtons() {
        binding.btnPermissionGrant.setOnClickListener {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }

        binding.btnPermissionSettings.setOnClickListener {
            permissionManager.openAppSettings(requireContext())
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
    }

    private fun updateUI(state: ContactsUiState) {
        when (state) {
            is ContactsUiState.Loading -> {
                showLoadingState()
            }
            is ContactsUiState.PermissionRequired -> {
                showPermissionRequiredState()
            }
            is ContactsUiState.Empty -> {
                showEmptyState(state.message)
            }
            is ContactsUiState.Error -> {
                showErrorState(state.message)
            }
            is ContactsUiState.Success -> {
                showSuccessState(state.contacts)
            }
        }

        binding.swipeRefresh.isRefreshing = state.isRefreshing
    }

    private fun showLoadingState() {
        binding.apply {
            rvContacts.visibility = View.GONE
            layoutEmpty.visibility = View.GONE
            layoutError.visibility = View.GONE
            layoutPermission.visibility = View.GONE
            shimmerLayout.visibility = View.VISIBLE
        }
    }

    private fun showPermissionRequiredState() {
        binding.apply {
            rvContacts.visibility = View.GONE
            layoutEmpty.visibility = View.GONE
            layoutError.visibility = View.GONE
            shimmerLayout.visibility = View.GONE
            layoutPermission.visibility = View.VISIBLE

            val rationale = permissionManager.getPermissionRationale()
            tvPermissionTitle.text = rationale.title
            tvPermissionMessage.text = rationale.message
            btnPermissionGrant.text = rationale.positiveButtonText
            btnPermissionSettings.text = rationale.settingsButtonText
        }
    }

    private fun showEmptyState(message: String) {
        binding.apply {
            rvContacts.visibility = View.GONE
            layoutPermission.visibility = View.GONE
            layoutError.visibility = View.GONE
            shimmerLayout.visibility = View.GONE
            layoutEmpty.visibility = View.VISIBLE

            tvEmptyMessage.text = message
        }
    }

    private fun showErrorState(message: String) {
        binding.apply {
            rvContacts.visibility = View.GONE
            layoutPermission.visibility = View.GONE
            layoutEmpty.visibility = View.GONE
            shimmerLayout.visibility = View.GONE
            layoutError.visibility = View.VISIBLE

            tvErrorMessage.text = message

            // Setup retry button
            btnRetry.setOnClickListener {
                viewModel.loadContacts()
            }
        }
    }

    private fun showSuccessState(contacts: List<com.msgmates.app.domain.contacts.model.Contact>) {
        binding.apply {
            layoutPermission.visibility = View.GONE
            layoutEmpty.visibility = View.GONE
            layoutError.visibility = View.GONE
            shimmerLayout.visibility = View.GONE
            rvContacts.visibility = View.VISIBLE

            contactsAdapter.submitList(contacts)

            // Show offline indicator if needed
            showOfflineIndicatorIfNeeded()
        }
    }

    private fun showOfflineIndicatorIfNeeded() {
        // Check if we're in offline mode and show appropriate indicator
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val lastSyncTime = viewModel.getLastSyncTime()
                if (lastSyncTime != null) {
                    val timeSinceSync = System.currentTimeMillis() - lastSyncTime
                    val isOffline = timeSinceSync > 300_000L // 5 minutes

                    if (isOffline) {
                        // Show offline indicator
                        binding.tvOfflineIndicator.visibility = View.VISIBLE
                        binding.tvOfflineIndicator.text = "Çevrimdışı mod - Son güncelleme: ${formatLastUpdateTime(
                            lastSyncTime
                        )}"
                    } else {
                        binding.tvOfflineIndicator.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                // Hide offline indicator on error
                binding.tvOfflineIndicator.visibility = View.GONE
            }
        }
    }

    private fun formatLastUpdateTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60_000L -> "Az önce"
            diff < 3600_000L -> "${diff / 60_000L} dakika önce"
            diff < 86400_000L -> "${diff / 3600_000L} saat önce"
            else -> "${diff / 86400_000L} gün önce"
        }
    }

    private fun checkPermissionAndLoadContacts() {
        if (permissionManager.hasReadContactsPermission(requireContext())) {
            viewModel.loadContacts()
        } else {
            viewModel.setPermissionRequired()
        }
    }

    private fun handlePermissionResult(isGranted: Boolean) {
        if (isGranted) {
            viewModel.loadContacts()
            Toast.makeText(requireContext(), "Rehber erişimi verildi", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.setPermissionRequired()
            Snackbar.make(binding.root, "Rehber erişimi gerekli", Snackbar.LENGTH_LONG)
                .setAction("Ayarlar") {
                    permissionManager.openAppSettings(requireContext())
                }
                .show()
        }
    }

    private fun makeCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        startActivity(intent)
    }

    private fun shareContact(contact: com.msgmates.app.domain.contacts.model.Contact) {
        val shareText = "${contact.displayName}\n${contact.phones.joinToString("\n") { it.rawNumber }}"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, "Kişiyi Paylaş"))
    }

    private fun openContactInSystem(contactId: Long) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.withAppendedPath(
                android.provider.ContactsContract.Contacts.CONTENT_URI,
                contactId.toString()
            )
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
