package com.msgmates.app.di

import com.msgmates.app.data.contacts.remote.api.ContactsApiService
import com.msgmates.app.ui.contacts.permission.ContactsPermissionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object ContactsModule {

    @Provides
    @Singleton
    fun provideContactsPermissionManager(): ContactsPermissionManager {
        return ContactsPermissionManager()
    }

    @Provides
    @Singleton
    fun provideContactsApiService(retrofit: Retrofit): ContactsApiService {
        return retrofit.create(ContactsApiService::class.java)
    }
}
