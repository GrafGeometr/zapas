package com.example.zapas.features.profile.presentation.viewmodel

import arrow.core.Either
import com.example.zapas.core.domain.model.AppError
import com.example.zapas.features.auth.domain.usecase.LogoutUseCase
import com.example.zapas.features.profile.domain.model.User
import com.example.zapas.features.profile.domain.usecase.ChangePasswordUseCase
import com.example.zapas.features.profile.domain.usecase.DeleteUserUseCase
import com.example.zapas.features.profile.domain.usecase.GetProfileUseCase
import com.example.zapas.features.profile.domain.usecase.UpdateProfileUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    private val getProfile = mockk<GetProfileUseCase>()
    private val updateProfile = mockk<UpdateProfileUseCase>()
    private val changePassword = mockk<ChangePasswordUseCase>()
    private val deleteUser = mockk<DeleteUserUseCase>()
    private val logout = mockk<LogoutUseCase>()
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        coEvery { getProfile() } returns Either.Right(User("user", "login", "Name", "Bio", null, false, "2026-01-01"))
    }

    @After
    fun teardown() { Dispatchers.resetMain() }

    private fun viewModel() = ProfileViewModel(getProfile, updateProfile, changePassword, deleteUser, logout)

    @Test
    fun `failed password change keeps dialog and exposes error with loaded profile`() = runTest {
        coEvery { changePassword(any()) } returns Either.Left(AppError.ApiError(400, "Wrong password"))
        val vm = viewModel()
        advanceUntilIdle()
        vm.setShowPasswordDialog(true)
        vm.changePassword("old", "new")
        advanceUntilIdle()
        assertTrue(vm.state.value.showPasswordDialog)
        assertNotNull(vm.state.value.user)
        assertEquals("Wrong password", vm.state.value.error)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun `pending password change cannot be dismissed or sent twice`() = runTest {
        val result = CompletableDeferred<Either<AppError, Unit>>()
        coEvery { changePassword(any()) } coAnswers { result.await() }
        val vm = viewModel()
        advanceUntilIdle()
        vm.setShowPasswordDialog(true)
        vm.changePassword("old", "new")
        vm.changePassword("old", "new")
        vm.setShowPasswordDialog(false)
        advanceUntilIdle()
        assertTrue(vm.state.value.isLoading)
        assertTrue(vm.state.value.showPasswordDialog)
        result.complete(Either.Right(Unit))
        advanceUntilIdle()
        assertFalse(vm.state.value.showPasswordDialog)
        assertTrue(vm.state.value.passwordChangeSuccess)
        coVerify(exactly = 1) { changePassword(any()) }
    }

    @Test
    fun `failed deletion keeps confirmation and exposes server error`() = runTest {
        coEvery { deleteUser(any()) } returns Either.Left(AppError.ApiError(501, "Not implemented"))
        val vm = viewModel()
        advanceUntilIdle()
        vm.setShowDeleteDialog(true)
        vm.deleteAccount {}
        vm.deleteAccount {}
        advanceUntilIdle()
        assertTrue(vm.state.value.showDeleteDialog)
        assertEquals("Not implemented", vm.state.value.error)
        coVerify(exactly = 1) { deleteUser(any()) }
        coVerify(exactly = 0) { logout() }
    }
}
