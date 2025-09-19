package com.msgmates.app.ui.groups

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.msgmates.app.R

class ContactsAdapter(
    private val onContactClick: (Contact) -> Unit
) : RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {

    private var contacts = listOf<Contact>()
    private val selectedContacts = mutableSetOf<String>()

    fun submitList(newContacts: List<Contact>) {
        contacts = newContacts
        notifyDataSetChanged()
    }

    fun setSelectedContacts(selected: Set<String>) {
        selectedContacts.clear()
        selectedContacts.addAll(selected)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact_selection, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(contacts[position])
    }

    override fun getItemCount(): Int = contacts.size

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_name)
        private val tvEmail: TextView = itemView.findViewById(R.id.tv_email)
        private val cbSelected: CheckBox = itemView.findViewById(R.id.cb_selected)

        fun bind(contact: Contact) {
            tvName.text = contact.name
            tvEmail.text = contact.email
            cbSelected.isChecked = selectedContacts.contains(contact.id)

            itemView.setOnClickListener {
                onContactClick(contact)
            }

            cbSelected.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked != selectedContacts.contains(contact.id)) {
                    onContactClick(contact)
                }
            }
        }
    }
}
