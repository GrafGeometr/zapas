package com.example.zapas.di

import com.example.zapas.domain.repository.AuthRepository
import com.example.zapas.domain.usecase.auth.CheckAuthStatusUseCase
import com.example.zapas.domain.usecase.auth.LoginUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides
    @ViewModelScoped
    fun provideLoginUseCase(
        authRepository: AuthRepository
    ): LoginUseCase {
        return LoginUseCase(authRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideCheckAuthStatusUseCase(
        authRepository: AuthRepository
    ): CheckAuthStatusUseCase {
        return CheckAuthStatusUseCase(authRepository)
    }
}