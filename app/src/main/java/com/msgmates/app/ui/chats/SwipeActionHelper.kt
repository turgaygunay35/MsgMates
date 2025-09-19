package com.msgmates.app.ui.chats

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.msgmates.app.R
import com.msgmates.app.domain.chats.Conversation

/**
 * Helper class for handling swipe actions on conversation items
 */
class SwipeActionHelper(
    private val onArchive: (Conversation) -> Unit,
    private val onMute: (Conversation) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    private val archiveIcon: Drawable? = null // TODO: Add archive icon
    private val muteIcon: Drawable? = null // TODO: Add mute icon
    private val background = ColorDrawable()
    private val clearPaint = Paint().apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false // No drag and drop support
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        val adapter = viewHolder.itemView.parent as? RecyclerView
        val chatsAdapter = adapter?.adapter as? ChatsAdapter
        val conversation = chatsAdapter?.getItemAt(position)

        if (conversation != null) {
            when (direction) {
                ItemTouchHelper.RIGHT -> {
                    // Swipe right = Archive
                    onArchive(conversation)
                }
                ItemTouchHelper.LEFT -> {
                    // Swipe left = Mute/Unmute
                    onMute(conversation)
                }
            }
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val itemView = viewHolder.itemView
        val itemHeight = itemView.height
        val isCancelled = dX == 0f && !isCurrentlyActive

        if (isCancelled) {
            clearCanvas(
                c,
                itemView.right + dX,
                itemView.top.toFloat(),
                itemView.right.toFloat(),
                itemView.bottom.toFloat()
            )
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        when {
            dX > 0 -> {
                // Swiping right - Archive action
                background.color = ContextCompat.getColor(itemView.context, R.color.colorArchive)
                background.setBounds(
                    itemView.left,
                    itemView.top,
                    itemView.left + dX.toInt(),
                    itemView.bottom
                )
                background.draw(c)

                // Draw archive icon
                val iconMargin = (itemHeight - (archiveIcon?.intrinsicHeight ?: 0)) / 2
                val iconTop = itemView.top + iconMargin
                val iconBottom = iconTop + (archiveIcon?.intrinsicHeight ?: 0)
                val iconLeft = itemView.left + iconMargin
                val iconRight = iconLeft + (archiveIcon?.intrinsicWidth ?: 0)

                archiveIcon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                archiveIcon?.draw(c)
            }
            dX < 0 -> {
                // Swiping left - Mute action
                background.color = ContextCompat.getColor(itemView.context, R.color.colorMute)
                background.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
                background.draw(c)

                // Draw mute icon
                val iconMargin = (itemHeight - (muteIcon?.intrinsicHeight ?: 0)) / 2
                val iconTop = itemView.top + iconMargin
                val iconBottom = iconTop + (muteIcon?.intrinsicHeight ?: 0)
                val iconLeft = itemView.right - iconMargin - (muteIcon?.intrinsicWidth ?: 0)
                val iconRight = itemView.right - iconMargin

                muteIcon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                muteIcon?.draw(c)
            }
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    private fun clearCanvas(c: Canvas, left: Float, top: Float, right: Float, bottom: Float) {
        c.drawRect(left, top, right, bottom, clearPaint)
    }
}
