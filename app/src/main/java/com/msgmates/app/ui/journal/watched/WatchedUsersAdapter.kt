package com.msgmates.app.ui.journal.watched

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.msgmates.app.R
import com.msgmates.app.data.journal.model.WatchedUser
import com.msgmates.app.databinding.ItemWatchedUserBinding

class WatchedUsersAdapter : ListAdapter<WatchedUser, WatchedUsersAdapter.WatchedUserViewHolder>(
    WatchedUserDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatchedUserViewHolder {
        val binding = ItemWatchedUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return WatchedUserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WatchedUserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class WatchedUserViewHolder(
        private val binding: ItemWatchedUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(watchedUser: WatchedUser) {
            binding.apply {
                // User name
                tvUserName.text = watchedUser.userName

                // Profile image
                Glide.with(ivProfile.context)
                    .load(watchedUser.profileImageUrl)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .circleCrop()
                    .into(ivProfile)

                // Last watched time
                val relativeTime = DateUtils.getRelativeTimeSpanString(
                    watchedUser.lastWatchedAt,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
                )
                tvLastWatched.text = "En son: $relativeTime"
            }
        }
    }

    class WatchedUserDiffCallback : DiffUtil.ItemCallback<WatchedUser>() {
        override fun areItemsTheSame(oldItem: WatchedUser, newItem: WatchedUser): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(oldItem: WatchedUser, newItem: WatchedUser): Boolean {
            return oldItem == newItem
        }
    }
}
