package com.msgmates.app.domain.chats

import java.io.Serializable
import java.time.LocalDateTime

/**
 * Domain model representing a conversation/chat in the app.
 * Contains all necessary information for displaying in the conversations list.
 */
data class Conversation(
    val id: String,
    val title: String,
    val lastMessage: String,
    val time: LocalDateTime,
    val unreadCount: Int = 0,
    val isMuted: Boolean = false,
    val isGroup: Boolean = false,
    val avatarUrl: String? = null
) : Serializable {
    /**
     * Formatted time string for display in the UI
     */
    fun getFormattedTime(): String {
        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        val conversationDate = time.toLocalDate()

        return when {
            conversationDate == today -> {
                // Today - show time only
                String.format("%02d:%02d", time.hour, time.minute)
            }
            conversationDate == today.minusDays(1) -> {
                // Yesterday
                "Dün"
            }
            conversationDate.isAfter(today.minusDays(7)) -> {
                // Within last week - show day name
                when (time.dayOfWeek.value) {
                    1 -> "Pazartesi"
                    2 -> "Salı"
                    3 -> "Çarşamba"
                    4 -> "Perşembe"
                    5 -> "Cuma"
                    6 -> "Cumartesi"
                    7 -> "Pazar"
                    else -> ""
                }
            }
            else -> {
                // Older - show date
                String.format("%02d.%02d", time.dayOfMonth, time.monthValue)
            }
        }
    }

    /**
     * Check if conversation has unread messages
     */
    fun hasUnreadMessages(): Boolean = unreadCount > 0

    /**
     * Get display text for unread count (handles 99+ case)
     */
    fun getUnreadCountText(): String = when {
        unreadCount <= 0 -> ""
        unreadCount > 99 -> "99+"
        else -> unreadCount.toString()
    }
}
