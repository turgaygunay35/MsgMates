package com.msgmates.app.ui.archive

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.msgmates.app.databinding.ItemArchiveBinding
import com.msgmates.app.domain.model.ArchiveItem

class ArchiveListAdapter : ListAdapter<ArchiveItem, ArchiveListAdapter.VH>(DIFF) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemArchiveBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }
    override fun onBindViewHolder(holder: VH, pos: Int) = holder.bind(getItem(pos))

    inner class VH(private val b: ItemArchiveBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: ArchiveItem) {
            b.tvTitle.text = item.title
            b.tvDesc.text = item.desc
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<ArchiveItem>() {
            override fun areItemsTheSame(o: ArchiveItem, n: ArchiveItem) = o.id == n.id
            override fun areContentsTheSame(o: ArchiveItem, n: ArchiveItem) = o == n
        }
    }
}
