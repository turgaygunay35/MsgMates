package com.msgmates.app.ui.disaster

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.msgmates.app.databinding.ItemEmergencyNumberBinding

class EmergencyNumbersAdapter(
    private val onItemClick: (EmergencyNumber) -> Unit
) : ListAdapter<EmergencyNumber, EmergencyNumbersAdapter.EmergencyNumberViewHolder>(EmergencyNumberDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmergencyNumberViewHolder {
        val binding = ItemEmergencyNumberBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EmergencyNumberViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EmergencyNumberViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EmergencyNumberViewHolder(
        private val binding: ItemEmergencyNumberBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(emergencyNumber: EmergencyNumber) {
            binding.apply {
                tvName.text = emergencyNumber.name
                tvNumber.text = emergencyNumber.number
                tvIcon.text = emergencyNumber.icon

                // Set color based on emergency type
                val color = ContextCompat.getColor(root.context, emergencyNumber.colorRes)
                cardEmergency.setCardBackgroundColor(color)

                // Set click listener
                root.setOnClickListener {
                    onItemClick(emergencyNumber)
                }
            }
        }
    }
}

class EmergencyNumberDiffCallback : DiffUtil.ItemCallback<EmergencyNumber>() {
    override fun areItemsTheSame(oldItem: EmergencyNumber, newItem: EmergencyNumber): Boolean {
        return oldItem.number == newItem.number
    }

    override fun areContentsTheSame(oldItem: EmergencyNumber, newItem: EmergencyNumber): Boolean {
        return oldItem == newItem
    }
}
