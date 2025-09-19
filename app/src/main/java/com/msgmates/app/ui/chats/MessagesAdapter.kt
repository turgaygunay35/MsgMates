package com.msgmates.app.ui.chats

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.msgmates.app.R
import com.msgmates.app.core.messaging.model.UiMessage
import java.text.SimpleDateFormat
import java.util.*

class MessagesAdapter(
    private val onAttachmentClick: (String, String, String) -> Unit = { _, _, _ -> },
    private val onAudioPlayClick: (String, String) -> Unit = { _, _ -> }
) : ListAdapter<UiMessage, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_TEXT_MINE = 1
        private const val VIEW_TYPE_TEXT_OTHER = 2
        private const val VIEW_TYPE_IMAGE = 3
        private const val VIEW_TYPE_FILE = 4
        private const val VIEW_TYPE_AUDIO = 5
    }

    private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return when {
            message.msgType == "image" -> VIEW_TYPE_IMAGE
            message.msgType == "file" -> VIEW_TYPE_FILE
            message.msgType == "audio" -> VIEW_TYPE_AUDIO
            message.senderId == "current_user" -> VIEW_TYPE_TEXT_MINE
            else -> VIEW_TYPE_TEXT_OTHER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_TEXT_MINE -> {
                val view = inflater.inflate(R.layout.item_message_text_mine, parent, false)
                TextMineViewHolder(view)
            }
            VIEW_TYPE_TEXT_OTHER -> {
                val view = inflater.inflate(R.layout.item_message_text_other, parent, false)
                TextOtherViewHolder(view)
            }
            VIEW_TYPE_IMAGE -> {
                val view = inflater.inflate(R.layout.item_message_image, parent, false)
                ImageViewHolder(view)
            }
            VIEW_TYPE_AUDIO -> {
                val view = inflater.inflate(R.layout.item_message_audio, parent, false)
                AudioViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_message_text_other, parent, false)
                TextOtherViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is TextMineViewHolder -> holder.bind(message)
            is TextOtherViewHolder -> holder.bind(message)
            is ImageViewHolder -> holder.bind(message)
            is AudioViewHolder -> holder.bind(message)
        }
    }

    inner class TextMineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessageText: TextView = itemView.findViewById(R.id.tv_message_text)
        private val tvMessageTime: TextView = itemView.findViewById(R.id.tv_message_time)
        private val ivMessageStatus: ImageView = itemView.findViewById(R.id.iv_message_status)

        fun bind(message: UiMessage) {
            tvMessageText.text = message.body
            tvMessageTime.text = formatTime(message.localCreatedAt)

            when (message.status) {
                "sending" -> {
                    ivMessageStatus.visibility = View.VISIBLE
                    ivMessageStatus.setImageResource(R.drawable.ic_message_sending)
                }
                "sent" -> {
                    ivMessageStatus.visibility = View.VISIBLE
                    ivMessageStatus.setImageResource(R.drawable.ic_message_sent)
                }
                "delivered" -> {
                    ivMessageStatus.visibility = View.VISIBLE
                    ivMessageStatus.setImageResource(R.drawable.ic_message_delivered)
                }
                "read" -> {
                    ivMessageStatus.visibility = View.VISIBLE
                    ivMessageStatus.setImageResource(R.drawable.ic_message_read)
                }
                else -> {
                    ivMessageStatus.visibility = View.GONE
                }
            }
        }
    }

    inner class TextOtherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSenderName: TextView = itemView.findViewById(R.id.tv_sender_name)
        private val tvMessageText: TextView = itemView.findViewById(R.id.tv_message_text)
        private val tvMessageTime: TextView = itemView.findViewById(R.id.tv_message_time)

        fun bind(message: UiMessage) {
            tvSenderName.text = message.senderId // TODO: Get actual name
            tvMessageText.text = message.body
            tvMessageTime.text = formatTime(message.localCreatedAt)
        }
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivImage: ImageView = itemView.findViewById(R.id.iv_image)
        private val tvMessageTime: TextView = itemView.findViewById(R.id.tv_message_time)

        fun bind(message: UiMessage) {
            // TODO: Load image from attachment
            ivImage.setImageResource(R.drawable.ic_image_placeholder)
            tvMessageTime.text = formatTime(message.localCreatedAt)

            // Set click listener for download
            ivImage.setOnClickListener {
                val attachment = message.attachments.firstOrNull()
                attachment?.let {
                    onAttachmentClick(
                        it.remoteUrl ?: "",
                        it.localUri ?: "",
                        it.mime
                    )
                }
            }
        }
    }

    inner class AudioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val btnPlayPause: ImageButton = itemView.findViewById(R.id.btn_play_pause)
        private val tvDuration: TextView = itemView.findViewById(R.id.tv_audio_duration)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progress_audio)
        private val tvMessageTime: TextView = itemView.findViewById(R.id.tv_message_time)

        fun bind(message: UiMessage) {
            val attachment = message.attachments.firstOrNull()

            // Set duration
            val duration = attachment?.durationMs ?: 0L
            tvDuration.text = formatDuration(duration)
            tvMessageTime.text = formatTime(message.localCreatedAt)

            // Set click listener for audio playback
            btnPlayPause.setOnClickListener {
                attachment?.let {
                    onAudioPlayClick(
                        it.remoteUrl ?: "",
                        it.localUri ?: ""
                    )
                }
            }

            // TODO: Update play/pause button state based on current playing state
            btnPlayPause.setImageResource(R.drawable.ic_play)
        }

        private fun formatDuration(durationMs: Long): String {
            val seconds = durationMs / 1000
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            return String.format("%d:%02d", minutes, remainingSeconds)
        }
    }

    internal fun formatTime(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }
}

class MessageDiffCallback : DiffUtil.ItemCallback<UiMessage>() {
    override fun areItemsTheSame(oldItem: UiMessage, newItem: UiMessage): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: UiMessage, newItem: UiMessage): Boolean {
        return oldItem == newItem
    }
}
