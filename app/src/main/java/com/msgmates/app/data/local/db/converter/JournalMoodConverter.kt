package com.msgmates.app.data.local.db.converter

import androidx.room.TypeConverter
import com.msgmates.app.domain.model.JournalMood

class JournalMoodConverter {
    @TypeConverter
    fun fromMood(mood: JournalMood?): String? {
        return mood?.name
    }

    @TypeConverter
    fun toMood(mood: String?): JournalMood? {
        return mood?.let {
            try {
                JournalMood.valueOf(it)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}
