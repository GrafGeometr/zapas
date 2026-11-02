package com.example.zapas.features.members.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import arrow.core.Either
import com.example.zapas.core.domain.model.AppError
import com.example.zapas.core.navigation.MemberProfile
import com.example.zapas.features.members.domain.model.LocalMemberProfile
import com.example.zapas.features.members.domain.model.MemberPortion
import com.example.zapas.features.members.domain.usecase.GetDefaultPortionsUseCase
import com.example.zapas.features.members.domain.usecase.GetLocalMemberProfileUseCase
import com.example.zapas.features.members.domain.usecase.GetMemberPortionsUseCase
import com.example.zapas.features.members.domain.usecase.GetMemberUseCase
import com.example.zapas.features.members.domain.usecase.SetDefaultPortionsUseCase
import com.example.zapas.features.members.domain.usecase.SetMemberPortionUseCase
import com.example.zapas.features.members.domain.usecase.UpdateMemberProfileUseCase
import com.example.zapas.features.profile.domain.model.User
import com.example.zapas.features.profile.domain.usecase.GetProfileUseCase
import com.example.zapas.features.rooms.domain.model.Member
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import java.io.IOException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MemberProfileViewModelTest {
    private val getMember = mockk<GetMemberUseCase>()
    private val getDefaults = mockk<GetDefaultPortionsUseCase>()
    private val getPortions = mockk<GetMemberPortionsUseCase>()
    private val getUser = mockk<GetProfileUseCase>()
    private val getLocal = mockk<GetLocalMemberProfileUseCase>()
    private val setDefaults = mockk<SetDefaultPortionsUseCase>()
    private val setPortion = mockk<SetMemberPortionUseCase>()
    private val updateProfile = mockk<UpdateMemberProfileUseCase>()
    private val dispatcher = StandardTestDispatcher()
    private val member = Member("Bio", "2026-01-01", "Name", "member", "room", "user")
    private val local = LocalMemberProfile("member", "room", "user", "2026-01-01", null, null)
    private val record = MemberPortion("2026-01-10", 3)

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        mockkStatic("androidx.navigation.SavedStateHandleKt")
        coEvery { getMember(any()) } returns Either.Right(member)
        coEvery { getDefaults(any()) } returns Either.Right(1)
        coEvery { getPortions(any()) } returns Either.Right(listOf(record))
        coEvery { getUser() } returns Either.Right(User("user", "login", "Global", "Global bio", null, false, "2026-01-01"))
        coEvery { getLocal(any()) } returns Either.Right(local)
    }

    @After
    fun teardown() {
        unmockkStatic("androidx.navigation.SavedStateHandleKt")
        Dispatchers.resetMain()
    }

    private fun viewModel(): MemberProfileViewModel {
        val handle = SavedStateHandle(mapOf("periodYear" to 2026, "periodMonth" to 0))
        every { handle.toRoute<MemberProfile>() } returns MemberProfile("member")
        return MemberProfileViewModel(
            handle, getMember, getDefaults, getPortions, getUser, getLocal, setDefaults, setPortion, updateProfile
        )
    }

    @Test
    fun `zero defaults are saved once and existing records are unchanged`() = runTest {
        coEvery { setDefaults(any()) } returns Either.Right(0)
        val vm = viewModel()
        advanceUntilIdle()
        vm.changeDefaultPortions(-1)
        vm.changeDefaultPortions(-1)
        advanceUntilIdle()
        assertEquals(0L, vm.state.value.defaultPortions)
        assertEquals(listOf(record), vm.state.value.portions)
        assertFalse(vm.state.value.showProfileDialog)
        coVerify(exactly = 1) { setDefaults(SetDefaultPortionsUseCase.Params("member", 0)) }
    }

    @Test
    fun `foreign member cannot open editors or send mutations`() = runTest {
        coEvery { getMember(any()) } returns Either.Right(member.copy(userId = "other"))
        val vm = viewModel()
        advanceUntilIdle()
        vm.changePortion(record.date, 1)
        vm.changeDefaultPortions(1)
        vm.openProfileEditor()
        vm.saveProfile("Changed", "Changed")
        advanceUntilIdle()
        assertEquals(false, vm.state.value.isOwnProfile)
        assertFalse(vm.state.value.showProfileDialog)
        coVerify(exactly = 0) { setPortion(any()) }
        coVerify(exactly = 0) { setDefaults(any()) }
        coVerify(exactly = 0) { updateProfile(any()) }
    }

    @Test
    fun `failed saving keeps the existing portion`() = runTest {
        coEvery { setPortion(any()) } returns Either.Left(AppError.NetworkError(IOException()))
        val vm = viewModel()
        advanceUntilIdle()
        vm.changePortion(record.date, -1)
        advanceUntilIdle()
        assertEquals(listOf(record), vm.state.value.portions)
        assertNotNull(vm.state.value.error)
        assertFalse(vm.state.value.isSaving)
    }

    @Test
    fun `old month response cannot overwrite the selected month`() = runTest {
        val february = CompletableDeferred<Either<AppError, List<MemberPortion>>>()
        coEvery { getPortions(any()) } coAnswers {
            val params = firstArg<GetMemberPortionsUseCase.Params>()
            if (params.begin == "2026-02-01") february.await()
            else Either.Right(listOf(MemberPortion(params.begin, 1)))
        }
        val vm = viewModel()
        advanceUntilIdle()
        vm.changeMonth(1)
        runCurrent()
        vm.changeMonth(1)
        advanceUntilIdle()
        february.complete(Either.Right(listOf(MemberPortion("2026-02-01", 99))))
        advanceUntilIdle()
        assertEquals(listOf(MemberPortion("2026-03-01", 1)), vm.state.value.portions)
        coVerify { getPortions(GetMemberPortionsUseCase.Params("member", "2026-02-01", "2026-02-28")) }
        coVerify { getPortions(GetMemberPortionsUseCase.Params("member", "2026-03-01", "2026-03-31")) }
    }

    @Test
    fun `profile update changes the room profile in the card`() = runTest {
        coEvery { updateProfile(any()) } returns Either.Right(local.copy(displayName = "Room name", bio = "Room bio"))
        val vm = viewModel()
        advanceUntilIdle()
        vm.openProfileEditor()
        vm.saveProfile("Room name", "Room bio")
        advanceUntilIdle()
        assertEquals("Room name", vm.state.value.member?.displayName)
        assertEquals("Room bio", vm.state.value.member?.bio)
        assertFalse(vm.state.value.showProfileDialog)
    }

    @Test
    fun `minus at zero makes no request`() = runTest {
        coEvery { getDefaults(any()) } returns Either.Right(0)
        coEvery { getPortions(any()) } returns Either.Right(listOf(record.copy(portions = 0)))
        val vm = viewModel()
        advanceUntilIdle()
        vm.changeDefaultPortions(-1)
        vm.changePortion(record.date, -1)
        advanceUntilIdle()
        assertEquals(0L, vm.state.value.defaultPortions)
        assertEquals(0L, vm.state.value.portions.single().portions)
        coVerify(exactly = 0) { setPortion(any()) }
        coVerify(exactly = 0) { setDefaults(any()) }
    }

    @Test
    fun `plus and minus send consecutive counts for the selected date`() = runTest {
        coEvery { setPortion(any()) } returns Either.Right(Unit)
        val vm = viewModel()
        advanceUntilIdle()
        vm.changePortion(record.date, 1)
        advanceUntilIdle()
        assertEquals(4L, vm.state.value.portions.single().portions)
        vm.changePortion(record.date, -1)
        advanceUntilIdle()
        assertEquals(3L, vm.state.value.portions.single().portions)
        coVerify(exactly = 1) { setPortion(SetMemberPortionUseCase.Params("member", record.date, 4)) }
        coVerify(exactly = 1) { setPortion(SetMemberPortionUseCase.Params("member", record.date, 3)) }
    }

    @Test
    fun `overflow invalid step and missing date make no requests`() = runTest {
        coEvery { getDefaults(any()) } returns Either.Right(Long.MAX_VALUE)
        coEvery { getPortions(any()) } returns Either.Right(listOf(record.copy(portions = Long.MAX_VALUE)))
        val vm = viewModel()
        advanceUntilIdle()
        vm.changeDefaultPortions(1)
        vm.changePortion(record.date, 1)
        vm.changeDefaultPortions(2)
        vm.changePortion(record.date, 0)
        vm.changePortion("2026-01-31", -1)
        advanceUntilIdle()
        coVerify(exactly = 0) { setPortion(any()) }
        coVerify(exactly = 0) { setDefaults(any()) }
    }
}
