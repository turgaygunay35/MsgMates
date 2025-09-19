package com.msgmates.app.ui.chats

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.msgmates.app.databinding.ItemChatBinding

data class ChatItem(
    val id: Long,
    val name: String,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ChatItem
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

class ChatsAdapterSimple : ListAdapter<ChatItem, ChatsAdapterSimple.ChatViewHolder>(ChatItemDiffCallback()) {

    private var onItemLongClickListener: ((Int) -> Boolean)? = null

    fun setOnItemLongClickListener(listener: (Int) -> Boolean) {
        onItemLongClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        try {
            val item = getItem(position)
            if (item != null) {
                holder.bind(item)
            }
        } catch (t: Throwable) {
            android.util.Log.e("CrashGuard", "ChatsAdapter onBindViewHolder failed at position $position", t)
        }
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int, payloads: MutableList<Any>) {
        try {
            if (payloads.isEmpty()) {
                onBindViewHolder(holder, position)
            } else {
                val chat = getItem(position)
                if (chat != null) {
                    holder.bindPartial(chat, payloads)
                }
            }
        } catch (t: Throwable) {
            android.util.Log.e(
                "CrashGuard",
                "ChatsAdapter onBindViewHolder with payloads failed at position $position",
                t
            )
        }
    }

    inner class ChatViewHolder(private val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnLongClickListener {
                onItemLongClickListener?.invoke(adapterPosition) ?: false
            }
        }

        fun bind(item: ChatItem) {
            try {
                binding.apply {
                    tvName.text = item.name ?: ""
                    tvLastMessage.text = item.lastMessage ?: ""
                    tvTimestamp.text = item.timestamp ?: ""

                    // Unread count
                    if (item.unreadCount > 0) {
                        tvUnreadCount.text = if (item.unreadCount > 99) "99+" else item.unreadCount.toString()
                        tvUnreadCount.visibility = View.VISIBLE
                    } else {
                        tvUnreadCount.visibility = View.GONE
                    }

                    // Online status
                    ivOnlineStatus.visibility = if (item.isOnline) View.VISIBLE else View.GONE
                }
            } catch (t: Throwable) {
                android.util.Log.e("CrashGuard", "ChatViewHolder bind failed", t)
            }
        }

        fun bindPartial(item: ChatItem, payloads: MutableList<Any>) {
            val changes = payloads.filterIsInstance<List<String>>().flatten().toSet()

            binding.apply {
                if ("name" in changes) {
                    tvName.text = item.name
                }
                if ("lastMessage" in changes) {
                    tvLastMessage.text = item.lastMessage
                }
                if ("timestamp" in changes) {
                    tvTimestamp.text = item.timestamp
                }
                if ("unreadCount" in changes) {
                    if (item.unreadCount > 0) {
                        tvUnreadCount.text = if (item.unreadCount > 99) "99+" else item.unreadCount.toString()
                        tvUnreadCount.visibility = View.VISIBLE
                    } else {
                        tvUnreadCount.visibility = View.GONE
                    }
                }
                if ("isOnline" in changes) {
                    ivOnlineStatus.visibility = if (item.isOnline) View.VISIBLE else View.GONE
                }
            }
        }
    }
}
