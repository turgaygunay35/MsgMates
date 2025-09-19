package com.msgmates.app.core.di

import android.content.Context
import com.msgmates.app.core.env.EnvConfig
import com.msgmates.app.core.env.EnvProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EnvModule {

    @Provides
    @Singleton
    fun provideEnv(@ApplicationContext context: Context): EnvConfig = EnvProvider.load(context)
}
