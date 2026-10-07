package com.example.zapas.di

import com.example.zapas.domain.repository.AuthRepository
import com.example.zapas.domain.repository.RoomsRepository
import com.example.zapas.domain.repository.UserRepository
import com.example.zapas.domain.usecase.auth.CheckAuthStatusUseCase
import com.example.zapas.domain.usecase.auth.LoginUseCase
import com.example.zapas.domain.usecase.auth.LogoutUseCase
import com.example.zapas.domain.usecase.profile.ChangePasswordUseCase
import com.example.zapas.domain.usecase.profile.DeleteUserUseCase
import com.example.zapas.domain.usecase.profile.GetProfileUseCase
import com.example.zapas.domain.usecase.profile.UpdateProfileUseCase
import com.example.zapas.domain.usecase.rooms.CreateRoomUseCase
import com.example.zapas.domain.usecase.rooms.DeleteRoomUseCase
import com.example.zapas.domain.usecase.rooms.EditRoomProfileUseCase
import com.example.zapas.domain.usecase.rooms.GetInviteTokenUseCase
import com.example.zapas.domain.usecase.rooms.GetRoomMembersUseCase
import com.example.zapas.domain.usecase.rooms.GetRoomProfileUseCase
import com.example.zapas.domain.usecase.rooms.GetRoomsListUseCase
import com.example.zapas.domain.usecase.rooms.GetRoomsPagedUseCase
import com.example.zapas.domain.usecase.rooms.JoinRoomUseCase
import com.example.zapas.domain.usecase.rooms.UpdateRoomTimingsUseCase
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

    @Provides
    @ViewModelScoped
    fun provideCreateRoomUseCase(
        roomsRepository: RoomsRepository
    ): CreateRoomUseCase {
        return CreateRoomUseCase(roomsRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideGetRoomProfileUseCase(
        roomsRepository: RoomsRepository
    ): GetRoomProfileUseCase {
        return GetRoomProfileUseCase(roomsRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideGetRoomsListUseCase(
        roomsRepository: RoomsRepository
    ): GetRoomsListUseCase {
        return GetRoomsListUseCase(roomsRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideGetRoomsPagedUseCase(
        roomsRepository: RoomsRepository
    ): GetRoomsPagedUseCase {
        return GetRoomsPagedUseCase(roomsRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideEditRoomProfileUseCase(
        roomsRepository: RoomsRepository
    ): EditRoomProfileUseCase {
        return EditRoomProfileUseCase(roomsRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideUpdateRoomTimingsUseCase(
        roomsRepository: RoomsRepository
    ): UpdateRoomTimingsUseCase {
        return UpdateRoomTimingsUseCase(roomsRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideGetInviteTokenUseCase(
        roomsRepository: RoomsRepository
    ): GetInviteTokenUseCase {
        return GetInviteTokenUseCase(roomsRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideGetRoomMembersUseCase(
        roomsRepository: RoomsRepository
    ): GetRoomMembersUseCase {
        return GetRoomMembersUseCase(roomsRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideDeleteRoomUseCase(
        roomsRepository: RoomsRepository
    ): DeleteRoomUseCase {
        return DeleteRoomUseCase(roomsRepository)
    }

    @Provides
    @ViewModelScoped
    fun provideJoinRoomUseCase(
        roomsRepository: RoomsRepository
    ): JoinRoomUseCase {
        return JoinRoomUseCase(roomsRepository)
    }
}
