package com.msgmates.app.ui.journal

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.msgmates.app.R
import com.msgmates.app.data.journal.model.JournalEntry
import com.msgmates.app.databinding.ItemJournalUserBinding

class JournalAdapter(
    private val onItemClick: (JournalEntry) -> Unit,
    private val onItemLongClick: (JournalEntry) -> Unit,
    private val onOverflowClick: (JournalEntry, android.view.View) -> Unit
) : ListAdapter<JournalEntry, JournalAdapter.JournalViewHolder>(JournalDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JournalViewHolder {
        val binding = ItemJournalUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return JournalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JournalViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class JournalViewHolder(
        private val binding: ItemJournalUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: JournalEntry) {
            binding.apply {
                // User name
                tvUserName.text = entry.userName

                // Profile image
                Glide.with(ivProfile.context)
                    .load(entry.profileImageUrl)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .circleCrop()
                    .into(ivProfile)

                // New story indicator
                vNewStoryIndicator.visibility = if (entry.hasNewStory) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }

                // Story count (mock - gerçekte birden fazla story varsa gösterilecek)
                tvStoryCount.visibility = if (entry.hasNewStory) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }

                // Click listeners
                root.setOnClickListener {
                    onItemClick(entry)
                }

                root.setOnLongClickListener {
                    onItemLongClick(entry)
                    true
                }

                // Overflow button click listener
                btnOverflow.setOnClickListener { view ->
                    onOverflowClick(entry, view)
                }
            }
        }
    }

    class JournalDiffCallback : DiffUtil.ItemCallback<JournalEntry>() {
        override fun areItemsTheSame(oldItem: JournalEntry, newItem: JournalEntry): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: JournalEntry, newItem: JournalEntry): Boolean {
            return oldItem == newItem
        }
    }
}
