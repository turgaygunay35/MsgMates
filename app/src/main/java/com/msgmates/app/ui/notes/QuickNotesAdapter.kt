package com.msgmates.app.ui.notes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.msgmates.app.databinding.ItemQuickNoteBinding
import com.msgmates.app.domain.model.QuickNote

class QuickNotesAdapter :
    ListAdapter<QuickNote, QuickNotesAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<QuickNote>() {
            override fun areItemsTheSame(oldItem: QuickNote, newItem: QuickNote) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: QuickNote, newItem: QuickNote) = oldItem == newItem
        }
    }

    inner class VH(val b: ItemQuickNoteBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemQuickNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.b.tvText.text = "${item.title}: ${item.body}"
        holder.b.tvTime.text = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date(item.createdAt))
    }
}
