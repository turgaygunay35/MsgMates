package com.msgmates.app.ui.contacts.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar
import com.msgmates.app.R
import com.msgmates.app.databinding.FragmentContactDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ContactDetailFragment : Fragment() {

    private var _binding: FragmentContactDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ContactDetailViewModel by viewModels()
    private lateinit var phoneNumberAdapter: PhoneNumberAdapter

    companion object {
        private const val ARG_CONTACT_ID = "contactId"

        fun newInstance(contactId: Long): ContactDetailFragment {
            val fragment = ContactDetailFragment()
            val args = Bundle().apply {
                putLong(ARG_CONTACT_ID, contactId)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupPhoneNumbersRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                com.msgmates.app.R.id.action_open_in_system -> {
                    viewModel.onOpenInSystem()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupPhoneNumbersRecyclerView() {
        phoneNumberAdapter = PhoneNumberAdapter { phoneNumber ->
            startCall(phoneNumber)
        }

        binding.recyclerViewPhones.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = phoneNumberAdapter
        }
    }

    private fun setupClickListeners() {
        binding.fabFavorite.setOnClickListener {
            viewModel.onToggleFavorite()
        }

        binding.buttonMessage.setOnClickListener {
            viewModel.onMessageClick()
        }

        binding.buttonCall.setOnClickListener {
            // Get primary phone number
            val contact = viewModel.uiState.value.contact
            val primaryPhone = contact?.phones?.firstOrNull()
            if (primaryPhone != null) {
                viewModel.onCallClick(primaryPhone.rawNumber)
            }
        }

        binding.buttonVideoCall.setOnClickListener {
            viewModel.onVideoCallClick()
        }

        binding.buttonInvite.setOnClickListener {
            viewModel.onInviteClick()
        }

        binding.buttonDisasterMode.setOnClickListener {
            viewModel.onDisasterModeClick()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                updateUI(uiState)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.events.collect { event ->
                event?.let { handleEvent(it) }
            }
        }
    }

    private fun updateUI(uiState: ContactDetailUiState) {
        if (uiState.isLoading) {
            showLoadingState()
        } else if (uiState.error != null) {
            showErrorState(uiState.error)
        } else if (uiState.contact != null) {
            showContactState(uiState.contact)
        }
    }

    private fun showLoadingState() {
        binding.root.visibility = View.GONE
        // Show loading indicator
    }

    private fun showErrorState(error: String) {
        binding.root.visibility = View.GONE
        // Show error state
        showSnackbar(error)
    }

    private fun showContactState(contact: com.msgmates.app.domain.contacts.model.Contact) {
        binding.root.visibility = View.VISIBLE

        // Set contact name - handle unnamed contacts
        val displayName = if (contact.displayName.isBlank() || contact.displayName == "Unknown") {
            contact.phones.firstOrNull()?.rawNumber ?: "Bilinmeyen"
        } else {
            contact.displayName
        }
        binding.textName.text = displayName

        // Set MsgMates status
        binding.chipMsgMates.visibility = if (contact.isMsgMatesUser) {
            View.VISIBLE
        } else {
            View.GONE
        }

        // Set last seen
        binding.textLastSeen.text = formatLastSeen(contact.lastSeenEpoch)

        // Set favorite button state
        binding.fabFavorite.isSelected = contact.favorite

        // Load contact photo with Glide
        Glide.with(binding.imageAvatar.context)
            .load(contact.photoUri)
            .placeholder(R.drawable.ic_person)
            .error(R.drawable.ic_person)
            .apply(RequestOptions.bitmapTransform(CircleCrop()))
            .into(binding.imageAvatar)

        // Setup phone numbers RecyclerView
        phoneNumberAdapter.submitList(contact.phones)

        // Show/hide action buttons based on MsgMates status
        if (contact.isMsgMatesUser) {
            binding.buttonMessage.visibility = View.VISIBLE
            binding.buttonVideoCall.visibility = View.VISIBLE
            binding.buttonInvite.visibility = View.GONE
        } else {
            binding.buttonMessage.visibility = View.GONE
            binding.buttonVideoCall.visibility = View.GONE
            binding.buttonInvite.visibility = View.VISIBLE
        }
    }

    private fun formatLastSeen(lastSeenEpoch: Long?): String {
        if (lastSeenEpoch == null) return "Never seen"

        val now = System.currentTimeMillis()
        val diff = now - lastSeenEpoch

        return when {
            diff < 60_000 -> "Just now"
            diff < 3600_000 -> "${diff / 60_000}m ago"
            diff < 86400_000 -> "${diff / 3600_000}h ago"
            else -> {
                val date = java.util.Date(lastSeenEpoch)
                val formatter = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                "Last seen ${formatter.format(date)}"
            }
        }
    }

    private fun handleEvent(event: ContactDetailEvent) {
        when (event) {
            is ContactDetailEvent.NavigateToChat -> {
                // Navigate to chat with contactId
                findNavController().navigate(R.id.dest_contacts) // Geçici olarak contacts sayfasına git
            }
            is ContactDetailEvent.StartCall -> {
                startCall(event.phoneNumber)
            }
            is ContactDetailEvent.StartVideoCall -> {
                // Navigate to video call with contactId
                findNavController().navigate(R.id.dest_contacts) // Geçici olarak contacts sayfasına git
            }
            is ContactDetailEvent.InviteToMsgMates -> {
                inviteToMsgMates()
            }
            is ContactDetailEvent.OpenInSystem -> {
                openInSystem()
            }
            is ContactDetailEvent.NavigateToDisasterMode -> {
                // Navigate to disaster mode with contactId
                findNavController().navigate(R.id.dest_disaster_mode)
            }
            is ContactDetailEvent.FavoriteToggled -> {
                showSnackbar(
                    if (event.isFavorite) "Added to favorites" else "Removed from favorites"
                )
            }
            is ContactDetailEvent.Error -> {
                showSnackbar(event.message)
            }
        }
    }

    private fun startCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        startActivity(intent)
    }

    private fun inviteToMsgMates() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Join me on MsgMates! Download the app from Play Store.")
        }
        startActivity(Intent.createChooser(shareIntent, "Invite to MsgMates"))
    }

    private fun openInSystem() {
        val contact = viewModel.uiState.value.contact
        if (contact != null) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.withAppendedPath(
                    android.provider.ContactsContract.Contacts.CONTENT_URI,
                    contact.id.toString()
                )
            }
            startActivity(intent)
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
