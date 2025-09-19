package com.msgmates.app.ui.journal.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.msgmates.app.databinding.ItemJournalEntryBinding
import com.msgmates.app.domain.model.JournalEntry
import java.text.SimpleDateFormat
import java.util.*

class JournalListAdapter(
    private val onEntryClick: (String) -> Unit,
    private val onFavoriteClick: (String) -> Unit,
    private val onSelectionChanged: (String, Boolean) -> Unit
) : ListAdapter<JournalEntry, JournalListAdapter.JournalEntryViewHolder>(JournalEntryDiffCallback()) {

    private var multiSelectMode = false
    private val selectedEntries = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JournalEntryViewHolder {
        val binding = ItemJournalEntryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return JournalEntryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JournalEntryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setMultiSelectMode(enabled: Boolean) {
        multiSelectMode = enabled
        if (!enabled) {
            selectedEntries.clear()
        }
        notifyDataSetChanged()
    }

    inner class JournalEntryViewHolder(
        private val binding: ItemJournalEntryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: JournalEntry) {
            binding.apply {
                textTitle.text = entry.title
                textContent.text = entry.content
                textDate.text = formatDate(entry.createdAt)

                // Mood emoji
                textMood.text = entry.mood?.emoji ?: ""
                textMood.visibility = if (entry.mood != null) android.view.View.VISIBLE else android.view.View.GONE

                // Tags
                if (entry.tags.isNotEmpty()) {
                    textTags.text = entry.tags.joinToString(", ")
                    textTags.visibility = android.view.View.VISIBLE
                } else {
                    textTags.visibility = android.view.View.GONE
                }

                // Multi-select mode
                checkboxSelect.visibility = if (multiSelectMode) android.view.View.VISIBLE else android.view.View.GONE
                checkboxSelect.isChecked = selectedEntries.contains(entry.id)

                // Click listeners
                root.setOnClickListener {
                    if (multiSelectMode) {
                        toggleSelection(entry.id)
                    } else {
                        onEntryClick(entry.id)
                    }
                }

                root.setOnLongClickListener {
                    if (!multiSelectMode) {
                        toggleSelection(entry.id)
                        true
                    } else {
                        false
                    }
                }

                checkboxSelect.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedEntries.add(entry.id)
                    } else {
                        selectedEntries.remove(entry.id)
                    }
                    onSelectionChanged(entry.id, isChecked)
                }
            }
        }

        private fun toggleSelection(entryId: String) {
            val isSelected = selectedEntries.contains(entryId)
            if (isSelected) {
                selectedEntries.remove(entryId)
            } else {
                selectedEntries.add(entryId)
            }
            binding.checkboxSelect.isChecked = !isSelected
            onSelectionChanged(entryId, !isSelected)
        }

        private fun formatDate(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            return when {
                diff < 60_000 -> "Şimdi" // Less than 1 minute
                diff < 3600_000 -> "${diff / 60_000} dk önce" // Less than 1 hour
                diff < 86400_000 -> "${diff / 3600_000} sa önce" // Less than 1 day
                diff < 604800_000 -> "${diff / 86400_000} gün önce" // Less than 1 week
                else -> {
                    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    sdf.format(Date(timestamp))
                }
            }
        }
    }

    class JournalEntryDiffCallback : DiffUtil.ItemCallback<JournalEntry>() {
        override fun areItemsTheSame(oldItem: JournalEntry, newItem: JournalEntry): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: JournalEntry, newItem: JournalEntry): Boolean {
            return oldItem == newItem
        }
    }
}
