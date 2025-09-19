package com.msgmates.app.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.msgmates.app.data.contacts.local.entity.ContactEntity
import com.msgmates.app.data.contacts.local.entity.ContactFts
import com.msgmates.app.data.contacts.local.entity.PhoneEntity
import com.msgmates.app.data.local.db.dao.CachedFileDao
import com.msgmates.app.data.local.db.dao.JournalDao
import com.msgmates.app.data.local.db.dao.JournalPhotoDao
import com.msgmates.app.data.local.db.dao.JournalTagDao
import com.msgmates.app.data.local.db.dao.UserDao
import com.msgmates.app.data.local.db.entity.CachedFileEntity
import com.msgmates.app.data.local.db.entity.JournalEntryEntity
import com.msgmates.app.data.local.db.entity.JournalEntryTagCrossRef
import com.msgmates.app.data.local.db.entity.JournalPhotoEntity
import com.msgmates.app.data.local.db.entity.JournalTagEntity
import com.msgmates.app.data.local.db.entity.UserEntity
import com.msgmates.app.data.local.db.migration.MIGRATION_1_TO_2

@Database(
    entities = [
        UserEntity::class,
        CachedFileEntity::class,
        JournalEntryEntity::class,
        JournalTagEntity::class,
        JournalPhotoEntity::class,
        JournalEntryTagCrossRef::class,
        ContactEntity::class,
        PhoneEntity::class,
        ContactFts::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun cachedFileDao(): CachedFileDao
    abstract fun journalDao(): JournalDao
    abstract fun journalTagDao(): JournalTagDao
    abstract fun journalPhotoDao(): JournalPhotoDao
    abstract fun contactDao(): com.msgmates.app.data.contacts.local.dao.ContactDao
    abstract fun phoneDao(): com.msgmates.app.data.contacts.local.dao.PhoneDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "msgmates_database"
                )
                    .addMigrations(MIGRATION_1_TO_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
