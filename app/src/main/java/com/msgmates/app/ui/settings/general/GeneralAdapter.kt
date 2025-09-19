package com.msgmates.app.ui.settings.general

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.msgmates.app.databinding.ItemGeneralBinding

class GeneralAdapter(
    private val onItemClick: (GeneralItem) -> Unit
) : ListAdapter<GeneralItem, GeneralAdapter.GeneralViewHolder>(GeneralDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GeneralViewHolder {
        val binding = ItemGeneralBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return GeneralViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: GeneralViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class GeneralViewHolder(
        private val binding: ItemGeneralBinding,
        private val onItemClick: (GeneralItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: GeneralItem) {
            binding.apply {
                textTitle.text = item.title
                textSubtitle.text = item.subtitle
                imageArrow.visibility = if (item.showArrow) android.view.View.VISIBLE else android.view.View.GONE

                root.setOnClickListener {
                    onItemClick(item)
                }
            }
        }
    }

    class GeneralDiffCallback : DiffUtil.ItemCallback<GeneralItem>() {
        override fun areItemsTheSame(oldItem: GeneralItem, newItem: GeneralItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: GeneralItem, newItem: GeneralItem): Boolean {
            return oldItem == newItem
        }
    }
}
