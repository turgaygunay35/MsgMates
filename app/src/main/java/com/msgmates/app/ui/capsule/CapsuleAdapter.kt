package com.msgmates.app.ui.capsule

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.msgmates.app.databinding.ItemCapsuleBinding
import java.util.concurrent.TimeUnit

class CapsuleAdapter(
    private val onClick: (Int) -> Unit,
    private val onLong: (Int) -> Unit
) : RecyclerView.Adapter<CapsuleAdapter.VH>() {

    private var items: List<Capsule> = emptyList()
    inner class VH(val b: ItemCapsuleBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inf = LayoutInflater.from(parent.context)
        return VH(ItemCapsuleBinding.inflate(inf, parent, false))
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val c = items[position]
        holder.b.tvTitle.text = c.title.ifEmpty { "(Başlıksız)" }
        holder.b.tvBody.text = c.body
        val now = System.currentTimeMillis()
        val diff = c.openAtMillis - now
        holder.b.tvEta.text = if (diff <= 0) {
            "AÇIK"
        } else {
            val d = TimeUnit.MILLISECONDS.toDays(diff)
            val h = TimeUnit.MILLISECONDS.toHours(diff - TimeUnit.DAYS.toMillis(d))
            val m = TimeUnit.MILLISECONDS.toMinutes(diff - TimeUnit.DAYS.toMillis(d) - TimeUnit.HOURS.toMillis(h))
            String.format("Kalan: %d gün %02d:%02d", d, h, m)
        }
        holder.b.root.setOnClickListener { onClick(position) }
        holder.b.root.setOnLongClickListener { onLong(position); true }
    }

    override fun getItemCount() = items.size
    fun submit(list: List<Capsule>) { items = list; notifyDataSetChanged() }
}
