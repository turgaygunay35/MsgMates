package com.msgmates.app.ui.contacts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.msgmates.app.R
import com.msgmates.app.databinding.ItemContactBinding
import com.msgmates.app.domain.contacts.model.Contact
import java.text.SimpleDateFormat
import java.util.*

class ContactsAdapter(
    private val onContactClick: (Contact) -> Unit,
    private val onCallClick: (String) -> Unit,
    private val onMessageClick: (Contact) -> Unit,
    private val onVideoCallClick: (Contact) -> Unit,
    private val onFavoriteToggle: (Contact) -> Unit,
    private val onShareClick: (Contact) -> Unit,
    private val onOpenInSystemClick: (Contact) -> Unit
) : ListAdapter<Contact, ContactsAdapter.ContactViewHolder>(ContactDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemContactBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ContactViewHolder(
        private val binding: ItemContactBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: Contact) {
            binding.apply {
                // Basic info - handle unnamed contacts
                val displayName = if (contact.displayName.isBlank() || contact.displayName == "Unknown") {
                    contact.phones.firstOrNull()?.rawNumber ?: "Bilinmeyen"
                } else {
                    contact.displayName
                }
                tvContactName.text = displayName
                tvContactPhone.text = contact.phones.firstOrNull()?.rawNumber ?: ""

                // Avatar with performance optimizations
                Glide.with(ivContactAvatar.context)
                    .load(contact.photoUri)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .apply(RequestOptions.bitmapTransform(CircleCrop()))
                    .override(120, 120) // Fixed size for better memory usage
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                    .skipMemoryCache(false) // Enable memory cache for avatars
                    .into(ivContactAvatar)

                // MsgMates badge
                ivMsgMatesBadge.visibility = if (contact.isMsgMatesUser) View.VISIBLE else View.GONE

                // Presence indicator
                updatePresenceIndicator(contact.presenceOnline)

                // Last seen
                tvLastSeen.text = formatLastSeen(contact.lastSeenEpoch)
                tvLastSeen.visibility = if (contact.lastSeenEpoch != null) View.VISIBLE else View.GONE

                // Favorite star
                ivFavorite.visibility = if (contact.favorite) View.VISIBLE else View.GONE

                // Click listeners
                root.setOnClickListener { onContactClick(contact) }
                btnCall.setOnClickListener {
                    contact.phones.firstOrNull()?.rawNumber?.let { phoneNumber ->
                        onCallClick(phoneNumber)
                    }
                }
                btnMessage.setOnClickListener { onMessageClick(contact) }
                btnVideoCall.setOnClickListener { onVideoCallClick(contact) }
                ivFavorite.setOnClickListener { onFavoriteToggle(contact) }

                // Overflow menu
                btnOverflow.setOnClickListener { view ->
                    showOverflowMenu(contact, view)
                }
            }
        }

        private fun updatePresenceIndicator(isOnline: Boolean?) {
            binding.apply {
                when (isOnline) {
                    true -> {
                        ivPresenceIndicator.setImageResource(R.drawable.ic_circle_green)
                        ivPresenceIndicator.visibility = View.VISIBLE
                    }
                    false -> {
                        ivPresenceIndicator.setImageResource(R.drawable.ic_circle_gray)
                        ivPresenceIndicator.visibility = View.VISIBLE
                    }
                    else -> {
                        ivPresenceIndicator.visibility = View.GONE
                    }
                }
            }
        }

        private fun formatLastSeen(lastSeenEpoch: Long?): String {
            if (lastSeenEpoch == null) return ""

            val now = System.currentTimeMillis()
            val diff = now - lastSeenEpoch

            return when {
                diff < 60_000 -> "Şimdi"
                diff < 3600_000 -> "${diff / 60_000} dk önce"
                diff < 86400_000 -> "${diff / 3600_000} saat önce"
                diff < 604800_000 -> "${diff / 86400_000} gün önce"
                else -> {
                    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    sdf.format(Date(lastSeenEpoch))
                }
            }
        }

        private fun showOverflowMenu(contact: Contact, anchorView: View) {
            val popup = android.widget.PopupMenu(anchorView.context, anchorView)
            popup.menuInflater.inflate(R.menu.menu_contact_overflow, popup.menu)

            // Show/hide invite option based on MsgMates status
            val inviteItem = popup.menu.findItem(R.id.action_invite)
            inviteItem?.isVisible = !contact.isMsgMatesUser

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_favorite -> {
                        onFavoriteToggle(contact)
                        true
                    }
                    R.id.action_share -> {
                        onShareClick(contact)
                        true
                    }
                    R.id.action_invite -> {
                        // TODO: Implement invite functionality
                        true
                    }
                    R.id.action_open_in_system -> {
                        onOpenInSystemClick(contact)
                        true
                    }
                    else -> false
                }
            }

            popup.show()
        }
    }

    class ContactDiffCallback : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem == newItem
        }
    }
}
