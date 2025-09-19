package com.msgmates.app.ui.journal.muted

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.msgmates.app.R
import com.msgmates.app.databinding.ItemMutedUserBinding

class MutedUsersAdapter(
    private val onUnmuteClick: (String) -> Unit
) : ListAdapter<MutedUserItem, MutedUsersAdapter.MutedUserViewHolder>(MutedUserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MutedUserViewHolder {
        val binding = ItemMutedUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MutedUserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MutedUserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MutedUserViewHolder(
        private val binding: ItemMutedUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MutedUserItem) {
            binding.apply {
                // User name
                tvUserName.text = item.userName

                // Profile image
                Glide.with(ivProfile.context)
                    .load(item.profileImageUrl)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .circleCrop()
                    .into(ivProfile)

                // Unmute button
                btnUnmute.setOnClickListener {
                    onUnmuteClick(item.userId)
                }
            }
        }
    }

    class MutedUserDiffCallback : DiffUtil.ItemCallback<MutedUserItem>() {
        override fun areItemsTheSame(oldItem: MutedUserItem, newItem: MutedUserItem): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(oldItem: MutedUserItem, newItem: MutedUserItem): Boolean {
            return oldItem == newItem
        }
    }
}

data class MutedUserItem(
    val userId: String,
    val userName: String,
    val profileImageUrl: String
)
