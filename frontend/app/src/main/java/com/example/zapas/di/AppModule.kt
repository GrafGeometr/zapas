package com.example.zapas.di

import android.content.Context
import com.example.zapas.data.api.AuthApiService
import com.example.zapas.data.api.AuthInterceptor
import com.example.zapas.data.local.TokenManager
import com.example.zapas.data.local.storage.KeyValueStorage
import com.example.zapas.data.local.storage.SharedPreferencesStorage
import com.example.zapas.data.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideKeyValueStorage(@ApplicationContext context: Context): KeyValueStorage {
        return SharedPreferencesStorage(context)
    }

    @Provides
    @Singleton
    fun provideTokenManager(storage: KeyValueStorage): TokenManager {
        return TokenManager(storage)
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenManager: TokenManager): AuthInterceptor {
        return AuthInterceptor(tokenManager)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        authApi: AuthApiService,
        tokenManager: TokenManager
    ): AuthRepository {
        return AuthRepository(authApi, tokenManager)
    }


}