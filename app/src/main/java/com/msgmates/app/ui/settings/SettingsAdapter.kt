package com.msgmates.app.ui.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.msgmates.app.databinding.ItemSettingsBinding

class SettingsAdapter(
    private val onItemClick: (SettingsItem) -> Unit
) : ListAdapter<SettingsItem, SettingsAdapter.SettingsViewHolder>(SettingsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsViewHolder {
        val binding = ItemSettingsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SettingsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SettingsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SettingsViewHolder(
        private val binding: ItemSettingsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SettingsItem) {
            binding.apply {
                tvTitle.text = item.title
                tvSubtitle.text = item.subtitle

                if (item.showArrow) {
                    ivArrow.visibility = android.view.View.VISIBLE
                } else {
                    ivArrow.visibility = android.view.View.GONE
                }

                root.setOnClickListener {
                    onItemClick(item)
                }
            }
        }
    }
}

class SettingsDiffCallback : DiffUtil.ItemCallback<SettingsItem>() {
    override fun areItemsTheSame(oldItem: SettingsItem, newItem: SettingsItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SettingsItem, newItem: SettingsItem): Boolean {
        return oldItem == newItem
    }
}
