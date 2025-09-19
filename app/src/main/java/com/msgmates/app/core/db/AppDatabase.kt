package com.msgmates.app.core.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.msgmates.app.core.db.dao.AttachmentDao
import com.msgmates.app.core.db.dao.MessageDao
import com.msgmates.app.core.db.dao.OutboxDao
import com.msgmates.app.core.db.entity.AttachmentEntity
import com.msgmates.app.core.db.entity.MessageEntity
import com.msgmates.app.core.db.entity.OutboxEntity

@Database(
    entities = [
        MessageEntity::class,
        AttachmentEntity::class,
        OutboxEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun outboxDao(): OutboxDao

    companion object {
        const val DATABASE_NAME = "msgmates.db"
    }
}
