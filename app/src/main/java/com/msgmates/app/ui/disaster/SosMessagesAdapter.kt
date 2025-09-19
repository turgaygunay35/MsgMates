package com.msgmates.app.ui.disaster

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.msgmates.app.R

class SosMessagesAdapter(
    private val messages: List<String>,
    private val onMessageClick: (String) -> Unit
) : RecyclerView.Adapter<SosMessagesAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textViewSosMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sos_message, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = messages[position]
        holder.textView.text = message

        holder.itemView.setOnClickListener {
            onMessageClick(message)
        }
    }

    override fun getItemCount(): Int = messages.size
}
