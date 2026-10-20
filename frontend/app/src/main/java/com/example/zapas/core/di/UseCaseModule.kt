package com.example.zapas.core.di

import com.example.zapas.features.auth.domain.repository.AuthRepository
import com.example.zapas.features.rooms.domain.repository.RoomsRepository
import com.example.zapas.features.profile.domain.repository.UserRepository
import com.example.zapas.features.auth.domain.usecase.CheckAuthStatusUseCase
import com.example.zapas.features.auth.domain.usecase.LoginUseCase
import com.example.zapas.features.auth.domain.usecase.LogoutUseCase
import com.example.zapas.features.profile.domain.usecase.ChangePasswordUseCase
import com.example.zapas.features.profile.domain.usecase.DeleteUserUseCase
import com.example.zapas.features.profile.domain.usecase.GetProfileUseCase
import com.example.zapas.features.profile.domain.usecase.UpdateProfileUseCase
import com.example.zapas.features.rooms.domain.usecase.CreateRoomUseCase
import com.example.zapas.features.rooms.domain.usecase.DeleteRoomUseCase
import com.example.zapas.features.rooms.domain.usecase.EditRoomProfileUseCase
import com.example.zapas.features.rooms.domain.usecase.GetInviteTokenUseCase
import com.example.zapas.features.rooms.domain.usecase.GetMembersPagedUseCase
import com.example.zapas.features.rooms.domain.usecase.GetMembersUseCase
import com.example.zapas.features.rooms.domain.usecase.GetRoomProfileUseCase
import com.example.zapas.features.rooms.domain.usecase.GetRoomsListUseCase
import com.example.zapas.features.rooms.domain.usecase.GetRoomsPagedUseCase
import com.example.zapas.features.rooms.domain.usecase.JoinRoomUseCase
import com.example.zapas.features.rooms.domain.usecase.UpdateRoomTimingsUseCase
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
    ) = LoginUseCase(authRepository)

    @Provides
    @ViewModelScoped
    fun provideCheckAuthStatusUseCase(
        authRepository: AuthRepository
    ) = CheckAuthStatusUseCase(authRepository)

    @Provides
    @ViewModelScoped
    fun provideLogoutUseCase(
        userRepository: UserRepository
    ) = LogoutUseCase(userRepository)

    @Provides
    @ViewModelScoped
    fun provideGetProfileUseCase(
        userRepository: UserRepository
    ) = GetProfileUseCase(userRepository)

    @Provides
    @ViewModelScoped
    fun provideUpdateProfileUseCase(
        userRepository: UserRepository
    ) = UpdateProfileUseCase(userRepository)

    @Provides
    @ViewModelScoped
    fun provideChangePasswordUseCase(
        userRepository: UserRepository
    ) = ChangePasswordUseCase(userRepository)

    @Provides
    @ViewModelScoped
    fun provideDeleteUserUseCase(
        userRepository: UserRepository
    ) = DeleteUserUseCase(userRepository)

    @Provides
    @ViewModelScoped
    fun provideCreateRoomUseCase(
        roomsRepository: RoomsRepository
    ) = CreateRoomUseCase(roomsRepository)

    @Provides
    @ViewModelScoped
    fun provideGetRoomProfileUseCase(
        roomsRepository: RoomsRepository
    ) = GetRoomProfileUseCase(roomsRepository)

    @Provides
    @ViewModelScoped
    fun provideGetRoomsListUseCase(
        roomsRepository: RoomsRepository
    ) = GetRoomsListUseCase(roomsRepository)

    @Provides
    @ViewModelScoped
    fun provideGetRoomsPagedUseCase(
        roomsRepository: RoomsRepository
    ) = GetRoomsPagedUseCase(roomsRepository)

    @Provides
    @ViewModelScoped
    fun provideEditRoomProfileUseCase(
        roomsRepository: RoomsRepository
    ) = EditRoomProfileUseCase(roomsRepository)

    @Provides
    @ViewModelScoped
    fun provideUpdateRoomTimingsUseCase(
        roomsRepository: RoomsRepository
    ) = UpdateRoomTimingsUseCase(roomsRepository)

    @Provides
    @ViewModelScoped
    fun provideGetInviteTokenUseCase(
        roomsRepository: RoomsRepository
    ) = GetInviteTokenUseCase(roomsRepository)

    @Provides
    @ViewModelScoped
    fun provideGetMembersUseCase(
        roomsRepository: RoomsRepository
    ) = GetMembersUseCase(roomsRepository)

    @Provides
    @ViewModelScoped
    fun provideGetMembersPagedUseCase(
        roomsRepository: RoomsRepository
    ) = GetMembersPagedUseCase(roomsRepository)

    @Provides
    @ViewModelScoped
    fun provideDeleteRoomUseCase(
        roomsRepository: RoomsRepository
    ) = DeleteRoomUseCase(roomsRepository)

    @Provides
    @ViewModelScoped
    fun provideJoinRoomUseCase(
        roomsRepository: RoomsRepository
    ) = JoinRoomUseCase(roomsRepository)
}
