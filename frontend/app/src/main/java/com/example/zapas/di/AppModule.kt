package com.example.zapas.di

import android.content.Context
import com.example.zapas.data.api.services.AuthApiService
import com.example.zapas.data.api.AuthInterceptor
import com.example.zapas.data.api.services.RoomsApiService
import com.example.zapas.data.api.services.UserApiService
import com.example.zapas.data.api.util.NetworkErrorMapper
import com.example.zapas.data.local.TokenManager
import com.example.zapas.data.local.storage.KeyValueStorage
import com.example.zapas.data.local.storage.SharedPreferenceStorage
import com.example.zapas.data.repository.AuthRepositoryImpl
import com.example.zapas.data.repository.RoomsRepositoryImpl
import com.example.zapas.data.repository.UserRepositoryImpl
import com.example.zapas.domain.repository.AuthRepository
import com.example.zapas.domain.repository.RoomsRepository
import com.example.zapas.domain.repository.UserRepository
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
    fun provideKeyValueStorage(@ApplicationContext context: Context): KeyValueStorage =
        SharedPreferenceStorage(context)

    @Provides
    @Singleton
    fun provideTokenManager(storage: KeyValueStorage): TokenManager = TokenManager(storage)

    @Provides
    @Singleton
    fun provideAuthInterceptor(
        tokenManager: TokenManager, authRepository: dagger.Lazy<AuthRepository>
    ): AuthInterceptor = AuthInterceptor(tokenManager, authRepository)

    @Provides
    @Singleton
    fun provideAuthRepository(
        authApi: AuthApiService,
        tokenManager: TokenManager,
        networkErrorMapper: NetworkErrorMapper
    ): AuthRepository = AuthRepositoryImpl(authApi, tokenManager, networkErrorMapper)

    @Provides
    @Singleton
    fun provideUserRepository(
        userApi: UserApiService,
        tokenManager: TokenManager,
        networkErrorMapper: NetworkErrorMapper
    ): UserRepository = UserRepositoryImpl(userApi, tokenManager, networkErrorMapper)

    @Provides
    @Singleton
    fun provideRoomsRepository(
        roomsApi: RoomsApiService,
        networkErrorMapper: NetworkErrorMapper
    ): RoomsRepository = RoomsRepositoryImpl(roomsApi, networkErrorMapper)
}
