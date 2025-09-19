package com.msgmates.app.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: String,
    val username: String,
    val email: String?,
    val phoneNumber: String,
    val profileImageUrl: String?,
    val isOnline: Boolean = false,
    val lastSeen: Long? = null,
    val isVerified: Boolean = false,
    val bio: String? = null,
    val createdAt: Long,
    val updatedAt: Long
) : Parcelable

@Parcelize
data class Contact(
    val id: String,
    val user: User,
    val isBlocked: Boolean = false,
    val isFavorite: Boolean = false,
    val addedAt: Long
) : Parcelable

@Parcelize
data class Message(
    val id: String,
    val chatId: String,
    val senderId: String,
    val content: String,
    val type: MessageType,
    val timestamp: Long,
    val isRead: Boolean = false,
    val isDelivered: Boolean = false,
    val replyToMessageId: String? = null,
    val attachments: List<Attachment> = emptyList()
) : Parcelable

@Parcelize
data class Attachment(
    val id: String,
    val fileName: String,
    val fileUrl: String,
    val fileType: AttachmentType,
    val fileSize: Long,
    val thumbnailUrl: String? = null
) : Parcelable

enum class MessageType {
    TEXT, IMAGE, VIDEO, AUDIO, FILE, LOCATION, CONTACT, STORY
}

enum class AttachmentType {
    IMAGE, VIDEO, AUDIO, DOCUMENT, OTHER
}

@Parcelize
data class Chat(
    val id: String,
    val name: String,
    val type: ChatType,
    val participants: List<User>,
    val lastMessage: Message?,
    val unreadCount: Int = 0,
    val isMuted: Boolean = false,
    val isPinned: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
) : Parcelable

enum class ChatType {
    PRIVATE, GROUP, CHANNEL
}

@Parcelize
data class Story(
    val id: String,
    val userId: String,
    val videoUrl: String,
    val thumbnailUrl: String?,
    val caption: String?,
    val duration: Int, // in seconds
    val views: Int = 0,
    val likes: Int = 0,
    val comments: Int = 0,
    val createdAt: Long,
    val expiresAt: Long
) : Parcelable

@Parcelize
data class MessageCapsule(
    val id: String,
    val title: String,
    val message: String,
    val scheduledTime: Long,
    val isOpened: Boolean = false,
    val createdAt: Long
) : Parcelable
