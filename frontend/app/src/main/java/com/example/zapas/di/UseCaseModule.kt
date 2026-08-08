package com.example.zapas.di

import com.example.zapas.domain.repository.AuthRepository
import com.example.zapas.domain.repository.UserRepository
import com.example.zapas.domain.usecase.auth.CheckAuthStatusUseCase
import com.example.zapas.domain.usecase.auth.LoginUseCase
import com.example.zapas.domain.usecase.auth.LogoutUseCase
import com.example.zapas.domain.usecase.profile.ChangePasswordUseCase
import com.example.zapas.domain.usecase.profile.DeleteUserUseCase
import com.example.zapas.domain.usecase.profile.GetProfileUseCase
import com.example.zapas.domain.usecase.profile.UpdateProfileUseCase
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

    @Provides
    @ViewModelScoped
    fun provideLogoutUseCase(
        userRepository: UserRepository
    ): LogoutUseCase {
        return LogoutUseCase(userRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideGetProfileUseCase(
        userRepository: UserRepository
    ): GetProfileUseCase {
        return GetProfileUseCase(userRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideUpdateProfileUseCase(
        userRepository: UserRepository
    ): UpdateProfileUseCase {
        return UpdateProfileUseCase(userRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideChangePasswordUseCase(
        userRepository: UserRepository
    ): ChangePasswordUseCase {
        return ChangePasswordUseCase(userRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideDeleteUserUseCase(
        userRepository: UserRepository
    ): DeleteUserUseCase {
        return DeleteUserUseCase(userRepository)
    }
}
