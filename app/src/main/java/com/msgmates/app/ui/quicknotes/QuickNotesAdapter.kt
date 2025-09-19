package com.msgmates.app.ui.quicknotes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.msgmates.app.databinding.ItemQuickNoteBinding
import com.msgmates.app.domain.qn.QuickNote
import java.text.DateFormat
import java.util.Date

class QuickNotesAdapter(
    private var items: List<QuickNote>,
    private val onEdit: (QuickNote) -> Unit,
    private val onDelete: (QuickNote) -> Unit
) : RecyclerView.Adapter<QuickNotesAdapter.VH>() {

    inner class VH(val b: ItemQuickNoteBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inf = LayoutInflater.from(parent.context)
        return VH(ItemQuickNoteBinding.inflate(inf, parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.b.tvText.text = item.text
        holder.b.tvTime.text = DateFormat.getDateTimeInstance().format(Date(item.timestamp))
        holder.b.root.setOnClickListener { onEdit(item) }
        holder.b.root.setOnLongClickListener { onDelete(item); true }
    }

    override fun getItemCount() = items.size

    fun submit(list: List<QuickNote>) {
        items = list
        notifyDataSetChanged()
    }
}
