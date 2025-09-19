package com.msgmates.app.ui.settings.messages

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.msgmates.app.databinding.ItemMessagesBinding

class MessagesAdapter(
    private val onItemClick: (MessagesItem) -> Unit
) : ListAdapter<MessagesItem, MessagesAdapter.MessagesViewHolder>(MessagesDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesViewHolder {
        val binding = ItemMessagesBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MessagesViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: MessagesViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MessagesViewHolder(
        private val binding: ItemMessagesBinding,
        private val onItemClick: (MessagesItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MessagesItem) {
            binding.apply {
                textTitle.text = item.title
                textSubtitle.text = item.subtitle

                if (item.showSwitch) {
                    switchOption.visibility = android.view.View.VISIBLE
                    imageArrow.visibility = android.view.View.GONE
                } else if (item.showArrow) {
                    switchOption.visibility = android.view.View.GONE
                    imageArrow.visibility = android.view.View.VISIBLE
                } else {
                    switchOption.visibility = android.view.View.GONE
                    imageArrow.visibility = android.view.View.GONE
                }

                root.setOnClickListener {
                    onItemClick(item)
                }
            }
        }
    }

    class MessagesDiffCallback : DiffUtil.ItemCallback<MessagesItem>() {
        override fun areItemsTheSame(oldItem: MessagesItem, newItem: MessagesItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MessagesItem, newItem: MessagesItem): Boolean {
            return oldItem == newItem
        }
    }
}
