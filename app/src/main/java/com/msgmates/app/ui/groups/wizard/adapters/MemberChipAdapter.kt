package com.msgmates.app.ui.groups.wizard.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.msgmates.app.databinding.ItemMemberChipBinding
import com.msgmates.app.domain.groups.User

class MemberChipAdapter : ListAdapter<User, MemberChipAdapter.MemberViewHolder>(object : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: User, newItem: User): Boolean = oldItem == newItem
}) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val binding = ItemMemberChipBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MemberViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MemberViewHolder(
        private val binding: ItemMemberChipBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.apply {
                tvName.text = user.name

                // Handle avatar
                if (user.avatarUrl != null) {
                    // TODO: Load avatar with Glide
                    ivAvatar.setImageResource(com.msgmates.app.R.drawable.ic_person)
                } else {
                    ivAvatar.setImageResource(com.msgmates.app.R.drawable.ic_person)
                }
            }
        }
    }
}
