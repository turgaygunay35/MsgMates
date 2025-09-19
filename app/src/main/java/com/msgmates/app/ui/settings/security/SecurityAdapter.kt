package com.msgmates.app.ui.settings.security

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.msgmates.app.databinding.ItemSecurityBinding

class SecurityAdapter(
    private val onItemClick: (SecurityItem) -> Unit
) : ListAdapter<SecurityItem, SecurityAdapter.SecurityViewHolder>(SecurityDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SecurityViewHolder {
        val binding = ItemSecurityBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SecurityViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: SecurityViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SecurityViewHolder(
        private val binding: ItemSecurityBinding,
        private val onItemClick: (SecurityItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SecurityItem) {
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

    class SecurityDiffCallback : DiffUtil.ItemCallback<SecurityItem>() {
        override fun areItemsTheSame(oldItem: SecurityItem, newItem: SecurityItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SecurityItem, newItem: SecurityItem): Boolean {
            return oldItem == newItem
        }
    }
}
