package com.msgmates.app.ui.chats

import androidx.recyclerview.widget.DiffUtil

class ChatItemDiffCallback : DiffUtil.ItemCallback<ChatItem>() {

    override fun areItemsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: ChatItem, newItem: ChatItem): Any? {
        // Return specific changes for partial updates
        val payload = mutableListOf<String>()

        if (oldItem.name != newItem.name) {
            payload.add("name")
        }
        if (oldItem.lastMessage != newItem.lastMessage) {
            payload.add("lastMessage")
        }
        if (oldItem.timestamp != newItem.timestamp) {
            payload.add("timestamp")
        }
        if (oldItem.unreadCount != newItem.unreadCount) {
            payload.add("unreadCount")
        }
        if (oldItem.isOnline != newItem.isOnline) {
            payload.add("isOnline")
        }

        return if (payload.isNotEmpty()) payload else null
    }
}
