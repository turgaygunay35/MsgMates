package com.msgmates.app.ui.settings.help

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.msgmates.app.databinding.ItemFaqBinding

class FaqAdapter(
    private val onItemClick: (FaqItem) -> Unit
) : ListAdapter<FaqItem, FaqAdapter.FaqViewHolder>(FaqDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
        val binding = ItemFaqBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FaqViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FaqViewHolder(
        private val binding: ItemFaqBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FaqItem) {
            binding.apply {
                tvQuestion.text = item.question
                tvAnswer.text = item.answer

                if (item.isExpanded) {
                    tvAnswer.visibility = View.VISIBLE
                    ivExpand.setRotation(180f)
                } else {
                    tvAnswer.visibility = View.GONE
                    ivExpand.setRotation(0f)
                }

                root.setOnClickListener {
                    onItemClick(item)
                }
            }
        }
    }
}

class FaqDiffCallback : DiffUtil.ItemCallback<FaqItem>() {
    override fun areItemsTheSame(oldItem: FaqItem, newItem: FaqItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FaqItem, newItem: FaqItem): Boolean {
        return oldItem == newItem
    }
}
