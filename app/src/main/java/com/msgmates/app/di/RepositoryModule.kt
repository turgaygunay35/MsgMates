package com.msgmates.app.di

import android.content.Context
import com.msgmates.app.data.chats.ChatsRepository
import com.msgmates.app.data.groups.GroupsRepository
import com.msgmates.app.data.local.db.dao.CachedFileDao
import com.msgmates.app.data.local.db.dao.JournalDao
import com.msgmates.app.data.local.db.dao.JournalPhotoDao
import com.msgmates.app.data.local.db.dao.JournalTagDao
import com.msgmates.app.data.local.db.dao.UserDao
import com.msgmates.app.data.local.prefs.SettingsPreferences
import com.msgmates.app.data.local.prefs.UserPrefsDataStore
import com.msgmates.app.data.repository.FilesRepository
import com.msgmates.app.data.repository.JournalRepository
import com.msgmates.app.data.repository.NotificationsRepository
import com.msgmates.app.data.repository.UserRepository
import com.msgmates.app.data.settings.SettingsRepository
import com.msgmates.app.network.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideUserPrefsDataStore(@ApplicationContext context: Context): UserPrefsDataStore = UserPrefsDataStore(context)

    // AuthRepository moved to NetworkModule (OTP-based)

    @Provides
    @Singleton
    fun provideUserRepository(
        apiService: ApiService,
        userDao: UserDao
    ): UserRepository = UserRepository(apiService, userDao)

    @Provides
    @Singleton
    fun provideFilesRepository(
        apiService: ApiService,
        cachedFileDao: CachedFileDao
    ): FilesRepository = FilesRepository(apiService, cachedFileDao)

    @Provides
    @Singleton
    fun provideNotificationsRepository(@ApplicationContext context: Context): NotificationsRepository = NotificationsRepository(context)

    @Provides
    @Singleton
    fun provideJournalRepository(
        journalDao: JournalDao,
        journalTagDao: JournalTagDao,
        journalPhotoDao: JournalPhotoDao
    ): JournalRepository = JournalRepository(journalDao, journalTagDao, journalPhotoDao)

    @Provides
    @Singleton
    fun provideChatsRepository(): ChatsRepository = ChatsRepository()

    @Provides
    @Singleton
    fun provideGroupsRepository(): GroupsRepository = GroupsRepository()

    @Provides
    @Singleton
    fun provideSettingsPreferences(@ApplicationContext context: Context): SettingsPreferences = SettingsPreferences(context)

    @Provides
    @Singleton
    fun provideSettingsRepository(
        settingsPreferences: SettingsPreferences
    ): SettingsRepository = SettingsRepository(settingsPreferences)
}
