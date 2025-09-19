package com.msgmates.app.di

import android.content.Context
import androidx.room.Room
import com.msgmates.app.data.contacts.local.dao.ContactDao
import com.msgmates.app.data.contacts.local.dao.PhoneDao
import com.msgmates.app.data.local.db.AppDatabase
import com.msgmates.app.data.local.db.dao.CachedFileDao
import com.msgmates.app.data.local.db.dao.JournalDao
import com.msgmates.app.data.local.db.dao.JournalPhotoDao
import com.msgmates.app.data.local.db.dao.JournalTagDao
import com.msgmates.app.data.local.db.dao.UserDao
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
            "msgmates_database"
        ).build()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()

    @Provides
    fun provideCachedFileDao(database: AppDatabase): CachedFileDao = database.cachedFileDao()

    @Provides
    fun provideJournalDao(database: AppDatabase): JournalDao = database.journalDao()

    @Provides
    fun provideJournalTagDao(database: AppDatabase): JournalTagDao = database.journalTagDao()

    @Provides
    fun provideJournalPhotoDao(database: AppDatabase): JournalPhotoDao = database.journalPhotoDao()

    @Provides
    fun provideContactDao(database: AppDatabase): ContactDao = database.contactDao()

    @Provides
    fun providePhoneDao(database: AppDatabase): PhoneDao = database.phoneDao()

    @Provides
    @Singleton
    fun provideContentResolver(@ApplicationContext context: Context): android.content.ContentResolver = context.contentResolver
}
