package com.msgmates.app.core.di

import android.content.Context
import androidx.room.Room
import com.msgmates.app.core.db.AppDatabase
import com.msgmates.app.core.db.dao.AttachmentDao
import com.msgmates.app.core.db.dao.MessageDao
import com.msgmates.app.core.db.dao.OutboxDao
import com.msgmates.app.core.db.migrations.MIGRATION_1_2
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideMessageDao(database: AppDatabase): MessageDao = database.messageDao()

    @Provides
    fun provideAttachmentDao(database: AppDatabase): AttachmentDao = database.attachmentDao()

    @Provides
    fun provideOutboxDao(database: AppDatabase): OutboxDao = database.outboxDao()
}
