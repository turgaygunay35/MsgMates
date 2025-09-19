package com.msgmates.app.ui.contacts.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.msgmates.app.databinding.ItemPhoneNumberBinding
import com.msgmates.app.domain.contacts.model.Phone

class PhoneNumberAdapter(
    private val onPhoneClick: (String) -> Unit
) : ListAdapter<Phone, PhoneNumberAdapter.PhoneViewHolder>(PhoneDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhoneViewHolder {
        val binding = ItemPhoneNumberBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PhoneViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhoneViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PhoneViewHolder(
        private val binding: ItemPhoneNumberBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(phone: Phone) {
            binding.apply {
                tvPhoneNumber.text = phone.rawNumber
                tvPhoneType.text = getPhoneTypeLabel(phone.type, phone.label)

                btnCallPhone.setOnClickListener {
                    onPhoneClick(phone.rawNumber)
                }

                root.setOnClickListener {
                    onPhoneClick(phone.rawNumber)
                }
            }
        }

        private fun getPhoneTypeLabel(type: String?, label: String?): String {
            return when (type) {
                "MOBILE" -> "Mobil"
                "HOME" -> "Ev"
                "WORK" -> "İş"
                "OTHER" -> "Diğer"
                else -> label ?: "Telefon"
            }
        }
    }

    class PhoneDiffCallback : DiffUtil.ItemCallback<Phone>() {
        override fun areItemsTheSame(oldItem: Phone, newItem: Phone): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Phone, newItem: Phone): Boolean {
            return oldItem == newItem
        }
    }
}
