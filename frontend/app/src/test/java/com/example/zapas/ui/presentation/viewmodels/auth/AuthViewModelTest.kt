package com.example.zapas.ui.presentation.viewmodels.auth

import app.cash.turbine.test
import arrow.core.Either
import com.example.zapas.core.domain.model.AppError
import com.example.zapas.features.auth.domain.usecase.CheckAuthStatusUseCase
import com.example.zapas.features.auth.domain.usecase.LoginUseCase
import com.example.zapas.features.auth.domain.usecase.LogoutUseCase
import com.example.zapas.features.auth.domain.usecase.RegisterUseCase
import com.example.zapas.features.auth.presentation.viewmodel.AuthViewModel
import com.example.zapas.features.auth.presentation.viewmodel.LoginState
import com.example.zapas.features.auth.presentation.viewmodel.RegisterState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private val loginUseCase = mockk<LoginUseCase>()
    private val registerUseCase = mockk<RegisterUseCase>()
    private val checkAuthStatusUseCase = mockk<CheckAuthStatusUseCase>()
    private val logoutUseCase = mockk<LogoutUseCase>()

    private lateinit var viewModel: AuthViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        viewModel = AuthViewModel(
            loginUseCase, registerUseCase, checkAuthStatusUseCase, logoutUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle`() = runTest {
        assertEquals(LoginState.Idle, viewModel.loginState.value)
        assertEquals(RegisterState.Idle, viewModel.registerState.value)
    }

    @Test
    fun `login success moves state from Idle to Loading to Success`() = runTest {
        coEvery { loginUseCase(any()) } returns Either.Right(Unit)

        viewModel.loginState.test {
            assertEquals(LoginState.Idle, awaitItem())

            viewModel.login("test_user", "password123")

            assertEquals(LoginState.Loading, awaitItem())
            assertEquals(LoginState.Success, awaitItem())

            expectNoEvents()
        }
    }

    @Test
    fun `login failure with 401 returns localized error message`() = runTest {
        coEvery { loginUseCase(any()) } returns Either.Left(AppError.ApiError(401, "Unauthorized"))

        viewModel.loginState.test {
            assertEquals(LoginState.Idle, awaitItem())

            viewModel.login("wrong_user", "wrong_pass")

            assertEquals(LoginState.Loading, awaitItem())

            val errorState = awaitItem()
            assertTrue(errorState is LoginState.Error)
            assertEquals("Неверный логин или пароль", (errorState as LoginState.Error).message)
        }
    }

    @Test
    fun `logout triggers logout use case`() = runTest {
        coEvery { logoutUseCase() } returns Unit

        viewModel.logout()
        advanceUntilIdle()

        coVerify(exactly = 1) { logoutUseCase() }
    }

    @Test
    fun `register success moves state from Idle to Loading to Success`() = runTest {
        coEvery { registerUseCase(any()) } returns Either.Right(Unit)

        viewModel.registerState.test {
            assertEquals(RegisterState.Idle, awaitItem())

            viewModel.register("new_user", "pass", "Name", "Bio")

            assertEquals(RegisterState.Loading, awaitItem())
            assertEquals(RegisterState.Success, awaitItem())
        }
    }
}
