package com.msgmates.app.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class JournalEntry(
    val id: String,
    val title: String,
    val content: String,
    val imagePath: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val mood: JournalMood? = null,
    val tags: List<String> = emptyList()
) : Parcelable

enum class JournalMood(val emoji: String, val displayName: String) {
    HAPPY("😊", "Mutlu"),
    SAD("😢", "Üzgün"),
    EXCITED("🤩", "Heyecanlı"),
    ANGRY("😠", "Kızgın"),
    CALM("😌", "Sakin"),
    WORRIED("😟", "Endişeli"),
    GRATEFUL("🙏", "Müteşekkir"),
    NOSTALGIC("😌", "Nostaljik"),
    HOPEFUL("🌟", "Umutlu"),
    TIRED("😴", "Yorgun")
}
