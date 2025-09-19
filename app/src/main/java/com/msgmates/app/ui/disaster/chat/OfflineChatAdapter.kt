package com.msgmates.app.ui.disaster.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.msgmates.app.core.disaster.mesh.MeshMessage
import com.msgmates.app.databinding.ItemOfflineMessageBinding
import java.text.SimpleDateFormat
import java.util.*

class OfflineChatAdapter : ListAdapter<MeshMessage, OfflineChatAdapter.MessageViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemOfflineMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MessageViewHolder(private val binding: ItemOfflineMessageBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: MeshMessage) {
            binding.tvSender.text = "ID: ${message.senderId.takeLast(4)}"
            binding.tvContent.text = message.content

            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            binding.tvTimestamp.text = timeFormat.format(Date(message.timestamp))
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<MeshMessage>() {
        override fun areItemsTheSame(oldItem: MeshMessage, newItem: MeshMessage): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MeshMessage, newItem: MeshMessage): Boolean {
            return oldItem == newItem
        }
    }
}
