package com.msgmates.app.ui.groups

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.msgmates.app.R

class SelectedMembersAdapter(
    private val onRemoveClick: (Contact) -> Unit
) : RecyclerView.Adapter<SelectedMembersAdapter.SelectedMemberViewHolder>() {

    private var members = listOf<Contact>()

    fun submitList(newMembers: List<Contact>) {
        members = newMembers
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedMemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_selected_member, parent, false)
        return SelectedMemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: SelectedMemberViewHolder, position: Int) {
        holder.bind(members[position])
    }

    override fun getItemCount(): Int = members.size

    inner class SelectedMemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivAvatar: ImageView = itemView.findViewById(R.id.iv_avatar)
        private val tvName: TextView = itemView.findViewById(R.id.tv_name)
        private val btnRemove: ImageView = itemView.findViewById(R.id.btn_remove)

        fun bind(contact: Contact) {
            tvName.text = contact.name

            // Avatar'da ilk harfi göster
            val firstLetter = contact.name.firstOrNull()?.uppercaseChar() ?: '?'
            tvName.text = firstLetter.toString()

            btnRemove.setOnClickListener {
                onRemoveClick(contact)
            }
        }
    }
}
