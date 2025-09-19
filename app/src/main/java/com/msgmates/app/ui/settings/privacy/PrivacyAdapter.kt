package com.msgmates.app.ui.settings.privacy

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.msgmates.app.databinding.ItemPrivacyBinding

class PrivacyAdapter(
    private val onItemClick: (PrivacyItem) -> Unit
) : ListAdapter<PrivacyItem, PrivacyAdapter.PrivacyViewHolder>(PrivacyDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrivacyViewHolder {
        val binding = ItemPrivacyBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PrivacyViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: PrivacyViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PrivacyViewHolder(
        private val binding: ItemPrivacyBinding,
        private val onItemClick: (PrivacyItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PrivacyItem) {
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

    class PrivacyDiffCallback : DiffUtil.ItemCallback<PrivacyItem>() {
        override fun areItemsTheSame(oldItem: PrivacyItem, newItem: PrivacyItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PrivacyItem, newItem: PrivacyItem): Boolean {
            return oldItem == newItem
        }
    }
}
