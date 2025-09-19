package com.msgmates.app.ui.notes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.msgmates.app.databinding.ItemNoteSimpleBinding

class NotesAdapter(
    private var items: List<Note>,
    private val onDelete: (Note) -> Unit
) : RecyclerView.Adapter<NotesAdapter.VH>() {

    inner class VH(val b: ItemNoteSimpleBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inf = LayoutInflater.from(parent.context)
        return VH(ItemNoteSimpleBinding.inflate(inf, parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.b.tvNote.text = item.text
        holder.b.btnDelete.setOnClickListener { onDelete(item) }
    }

    override fun getItemCount() = items.size

    fun submit(newItems: List<Note>) {
        items = newItems
        notifyDataSetChanged()
    }
}
