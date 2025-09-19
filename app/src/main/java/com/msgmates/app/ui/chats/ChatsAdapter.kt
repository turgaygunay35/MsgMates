package com.msgmates.app.ui.chats

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.msgmates.app.R
import com.msgmates.app.databinding.ItemConversationBinding
import com.msgmates.app.domain.chats.Conversation

/**
 * Adapter for displaying conversations list with paging support
 */
class ChatsAdapter : PagingDataAdapter<Conversation, ChatsAdapter.ConversationViewHolder>(ConversationDiffCallback()) {

    private var onItemClickListener: ((Conversation) -> Unit)? = null
    private var onItemLongClickListener: ((Conversation) -> Boolean)? = null
    private var onSelectionModeChangeListener: ((Boolean) -> Unit)? = null
    private var onOverflowClickListener: ((Conversation) -> Unit)? = null

    private val selectedItems = mutableSetOf<String>()
    private var isSelectionMode = false

    fun setOnItemClickListener(listener: (Conversation) -> Unit) {
        onItemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: (Conversation) -> Boolean) {
        onItemLongClickListener = listener
    }

    fun setOnSelectionModeChangeListener(listener: (Boolean) -> Unit) {
        onSelectionModeChangeListener = listener
    }

    fun setOnOverflowClickListener(listener: (Conversation) -> Unit) {
        onOverflowClickListener = listener
    }

    fun enterSelectionMode() {
        isSelectionMode = true
        onSelectionModeChangeListener?.invoke(true)
        notifyDataSetChanged()
    }

    fun exitSelectionMode() {
        isSelectionMode = false
        selectedItems.clear()
        onSelectionModeChangeListener?.invoke(false)
        notifyDataSetChanged()
    }

    fun toggleSelection(conversation: Conversation) {
        if (selectedItems.contains(conversation.id)) {
            selectedItems.remove(conversation.id)
        } else {
            selectedItems.add(conversation.id)
        }
        val position = snapshot().items.indexOfFirst { it.id == conversation.id }
        if (position >= 0) {
            notifyItemChanged(position)
        }
    }

    fun isSelected(conversation: Conversation): Boolean {
        return selectedItems.contains(conversation.id)
    }

    fun getSelectedItems(): List<Conversation> {
        return snapshot().items.filter { selectedItems.contains(it.id) }
    }

    fun getSelectedCount(): Int = selectedItems.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val binding = ItemConversationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ConversationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val conversation = getItem(position)
        if (conversation != null) {
            holder.bind(conversation)

            // Yeni mesaj için fade-in + slide-in animasyonu
            if (position < 5) { // İlk 5 item için animasyon (yeni mesajlar)
                holder.itemView.alpha = 0f
                holder.itemView.translationY = 50f

                holder.itemView.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .setInterpolator(android.view.animation.DecelerateInterpolator())
                    .start()
            }
        }
    }

    fun getItemAt(position: Int): Conversation? {
        return getItem(position)
    }

    inner class ConversationViewHolder(
        private val binding: ItemConversationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val conversation = getItem(adapterPosition)
                if (conversation != null) {
                    if (isSelectionMode) {
                        toggleSelection(conversation)
                    } else {
                        onItemClickListener?.invoke(conversation)
                    }
                }
            }

            binding.root.setOnLongClickListener {
                val conversation = getItem(adapterPosition)
                if (conversation != null) {
                    if (!isSelectionMode) {
                        enterSelectionMode()
                        toggleSelection(conversation)
                    }
                    onItemLongClickListener?.invoke(conversation) ?: false
                } else {
                    false
                }
            }

            // Overflow button click listener
            binding.btnOverflow.setOnClickListener {
                val conversation = getItem(adapterPosition)
                if (conversation != null) {
                    onOverflowClickListener?.invoke(conversation)
                }
            }
        }

        fun bind(conversation: Conversation) {
            binding.apply {
                // Set conversation data
                tvTitle.text = conversation.title
                tvLastMessage.text = conversation.lastMessage
                tvTimestamp.text = conversation.getFormattedTime()

                // Handle unread count with animation
                if (conversation.hasUnreadMessages()) {
                    tvUnreadCount.text = conversation.getUnreadCountText()
                    if (tvUnreadCount.visibility != View.VISIBLE) {
                        tvUnreadCount.visibility = View.VISIBLE
                        // Fade-in animasyonu
                        val fadeInAnim = AnimationUtils.loadAnimation(tvUnreadCount.context, android.R.anim.fade_in)
                        tvUnreadCount.startAnimation(fadeInAnim)
                    }
                } else {
                    if (tvUnreadCount.visibility == View.VISIBLE) {
                        // Shrink animasyonu ile kaybol
                        val shrinkAnim = AnimationUtils.loadAnimation(tvUnreadCount.context, R.anim.badge_shrink)
                        tvUnreadCount.startAnimation(shrinkAnim)
                        tvUnreadCount.visibility = View.GONE
                    }
                }

                // Handle group indicator
                ivGroupIndicator.visibility = if (conversation.isGroup) View.VISIBLE else View.GONE

                // Handle mute icon
                ivMuteIcon.visibility = if (conversation.isMuted) View.VISIBLE else View.GONE

                // Show overflow button
                btnOverflow.visibility = View.VISIBLE

                // Handle avatar with colored background and first letter
                setupAvatar(conversation)

                // Handle selection mode
                if (isSelectionMode) {
                    root.setBackgroundColor(
                        if (isSelected(conversation)) {
                            root.context.getColor(R.color.primary_blue_light)
                        } else {
                            root.context.getColor(android.R.color.transparent)
                        }
                    )
                } else {
                    root.setBackgroundColor(root.context.getColor(android.R.color.transparent))
                }

                // TODO: Load avatar from URL when available
                // Glide.with(ivAvatar.context)
                //     .load(conversation.avatarUrl)
                //     .placeholder(R.drawable.ic_person)
                //     .into(ivAvatar)
            }
        }

        private fun setupAvatar(conversation: Conversation) {
            if (conversation.avatarUrl.isNullOrEmpty()) {
                // Show colored avatar with first letter
                val firstLetter = conversation.title.firstOrNull()?.uppercaseChar() ?: '?'
                val avatarColors = listOf(
                    R.drawable.bg_avatar_blue,
                    R.drawable.bg_avatar_turkuaz,
                    R.drawable.bg_avatar_green,
                    R.drawable.bg_avatar_orange,
                    R.drawable.bg_avatar_purple
                )

                // Use conversation ID to consistently assign colors
                val colorIndex = conversation.id.hashCode() % avatarColors.size
                val colorRes = if (colorIndex < 0) avatarColors[-colorIndex] else avatarColors[colorIndex]

                binding.ivAvatar.setImageResource(
                    if (conversation.isGroup) R.drawable.ic_group else R.drawable.ic_person
                )
                binding.ivAvatar.setBackgroundResource(colorRes)

                // Show first letter
                binding.tvAvatarLetter.text = firstLetter.toString()
                binding.tvAvatarLetter.visibility = View.VISIBLE
                binding.ivAvatar.visibility = View.GONE
            } else {
                // TODO: Load actual avatar from URL
                binding.ivAvatar.setImageResource(
                    if (conversation.isGroup) R.drawable.ic_group else R.drawable.ic_person
                )
                binding.ivAvatar.setBackgroundResource(R.drawable.bg_avatar_blue)
                binding.tvAvatarLetter.visibility = View.GONE
                binding.ivAvatar.visibility = View.VISIBLE
            }
        }
    }
}

/**
 * DiffUtil callback for Conversation items
 */
class ConversationDiffCallback : DiffUtil.ItemCallback<Conversation>() {

    override fun areItemsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Conversation, newItem: Conversation): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: Conversation, newItem: Conversation): Any? {
        val payload = mutableListOf<String>()

        if (oldItem.title != newItem.title) payload.add("title")
        if (oldItem.lastMessage != newItem.lastMessage) payload.add("lastMessage")
        if (oldItem.time != newItem.time) payload.add("time")
        if (oldItem.unreadCount != newItem.unreadCount) payload.add("unreadCount")
        if (oldItem.isMuted != newItem.isMuted) payload.add("isMuted")
        if (oldItem.isGroup != newItem.isGroup) payload.add("isGroup")
        if (oldItem.avatarUrl != newItem.avatarUrl) payload.add("avatarUrl")

        return if (payload.isNotEmpty()) payload else null
    }
}
