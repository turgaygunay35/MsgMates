package com.msgmates.app.di

import com.msgmates.app.core.auth.AuthRepository
import com.msgmates.app.core.auth.remote.AuthApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Auth modülü - Hilt dependency injection
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {
    
    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideAuthRepository(
        authApiService: AuthApiService,
        tokenRepository: com.msgmates.app.core.auth.TokenRepository
    ): AuthRepository {
        return AuthRepository(authApiService, tokenRepository)
    }
}
