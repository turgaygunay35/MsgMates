package com.msgmates.app.di

import com.msgmates.app.data.repository.FilesRepository
import com.msgmates.app.data.repository.auth.AuthRepository
import com.msgmates.app.domain.usecase.GetRecommendedCleanupUseCase
import com.msgmates.app.domain.usecase.LoginUseCase
import com.msgmates.app.domain.usecase.RefreshTokenUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideLoginUseCase(authRepository: AuthRepository): LoginUseCase = LoginUseCase(authRepository)

    @Provides
    @Singleton
    fun provideRefreshTokenUseCase(authRepository: AuthRepository): RefreshTokenUseCase = RefreshTokenUseCase(authRepository)

    @Provides
    @Singleton
    fun provideGetRecommendedCleanupUseCase(filesRepository: FilesRepository): GetRecommendedCleanupUseCase = GetRecommendedCleanupUseCase(filesRepository)
}
