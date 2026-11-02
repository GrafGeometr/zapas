package com.example.zapas.features.members.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import com.example.zapas.features.profile.domain.usecase.GetProfileUseCase
import com.example.zapas.features.rooms.domain.model.Member
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

enum class MemberDialog { PROFILE, DEFAULT_PORTIONS, PORTION }

data class MemberProfileState(
    val member: Member? = null,
    val currentUserId: String? = null,
    val localProfile: LocalMemberProfile? = null,
    val defaultPortions: Long? = null,
    val portions: List<MemberPortion> = emptyList(),
    val periodTitle: String = "",
    val hasLoadedPortions: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val dialog: MemberDialog? = null,
    val selectedPortion: MemberPortion? = null
) {
    val isOwnProfile: Boolean?
        get() = currentUserId?.let { userId -> member?.userId?.let { it == userId } }
    val canEdit: Boolean
        get() = isOwnProfile == true && !isLoading && !isSaving
}

@HiltViewModel
class MemberProfileViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getMemberUseCase: GetMemberUseCase,
    private val getDefaultPortionsUseCase: GetDefaultPortionsUseCase,
    private val getMemberPortionsUseCase: GetMemberPortionsUseCase,
    private val getProfileUseCase: GetProfileUseCase,
    private val getLocalMemberProfileUseCase: GetLocalMemberProfileUseCase,
    private val setDefaultPortionsUseCase: SetDefaultPortionsUseCase,
    private val setMemberPortionUseCase: SetMemberPortionUseCase,
    private val updateMemberProfileUseCase: UpdateMemberProfileUseCase
) : ViewModel() {
    private val memberId = savedStateHandle.toRoute<MemberProfile>().memberId
    private val period = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        savedStateHandle.get<Int>("periodYear")?.let { set(Calendar.YEAR, it) }
        savedStateHandle.get<Int>("periodMonth")?.let { set(Calendar.MONTH, it) }
    }
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
    private val titleFormat = SimpleDateFormat("LLLL yyyy", Locale("ru"))
    private val _state = MutableStateFlow(MemberProfileState(periodTitle = titleFormat.format(period.time)))
    val state = _state.asStateFlow()
    private var loadJob: Job? = null

    init {
        load()
    }

    fun changeMonth(offset: Int) {
        if (_state.value.isSaving || _state.value.dialog != null || offset !in listOf(-1, 1)) return
        val next = (period.clone() as Calendar).apply { add(Calendar.MONTH, offset) }
        if (next.get(Calendar.ERA) != 1 || next.get(Calendar.YEAR) !in 1..9999) return
        period.time = next.time
        savedStateHandle["periodYear"] = period.get(Calendar.YEAR)
        savedStateHandle["periodMonth"] = period.get(Calendar.MONTH)
        _state.update {
            it.copy(periodTitle = titleFormat.format(period.time), portions = emptyList(), hasLoadedPortions = false)
        }
        load()
    }

    fun load() {
        if (_state.value.isSaving || _state.value.dialog != null) return
        loadJob?.cancel()
        val begin = dateFormat.format(period.time)
        val end = dateFormat.format((period.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        }.time)
        _state.update { it.copy(isLoading = true, error = null) }
        loadJob = viewModelScope.launch {
            val memberRequest = async { getMemberUseCase(memberId) }
            val profileRequest = async { getProfileUseCase() }
            val defaultPortionsRequest = async { getDefaultPortionsUseCase(memberId) }
            val portionsRequest = async {
                getMemberPortionsUseCase(GetMemberPortionsUseCase.Params(memberId, begin, end))
            }
            val member = memberRequest.await()
            val profile = profileRequest.await()
            val defaultPortions = defaultPortionsRequest.await()
            val portions = portionsRequest.await()
            val ownProfile = member.getOrNull()?.userId?.let { it == profile.getOrNull()?.id } == true
            val localProfile = if (ownProfile) getLocalMemberProfileUseCase(memberId) else null
            val error = listOfNotNull(member, profile, defaultPortions, portions, localProfile)
                .firstNotNullOfOrNull { result -> result.fold({ formatError(it) }, { null }) }
            val inaccessible = member.fold({ it is AppError.ApiError && it.httpStatus in listOf(403, 404) }, { false })
            coroutineContext.ensureActive()
            _state.update {
                it.copy(
                    member = if (inaccessible) null else member.getOrNull() ?: it.member,
                    currentUserId = profile.getOrNull()?.id,
                    localProfile = if (ownProfile) localProfile?.getOrNull() else null,
                    defaultPortions = if (inaccessible) null else defaultPortions.getOrNull() ?: it.defaultPortions,
                    portions = if (inaccessible) emptyList() else portions.getOrNull()?.sortedBy { record -> record.date } ?: it.portions,
                    hasLoadedPortions = !inaccessible && (portions is Either.Right || it.hasLoadedPortions),
                    isLoading = false,
                    error = error
                )
            }
        }
    }

    fun openDialog(dialog: MemberDialog, portion: MemberPortion? = null) {
        val state = _state.value
        if (!state.canEdit) return
        if (dialog == MemberDialog.PROFILE && state.localProfile == null) return
        if (dialog == MemberDialog.DEFAULT_PORTIONS && state.defaultPortions == null) return
        if (dialog == MemberDialog.PORTION && (portion == null || portion !in state.portions)) return
        _state.update { it.copy(dialog = dialog, selectedPortion = portion, error = null) }
    }

    fun dismissDialog() {
        if (_state.value.isSaving) return
        _state.update { it.copy(dialog = null, selectedPortion = null, error = null) }
    }

    fun savePortions(input: String) {
        val state = _state.value
        if (!state.canEdit) return
        val count = parsePortions(input)
        if (count == null) {
            _state.update { it.copy(error = "Введите целое неотрицательное число допустимого размера") }
            return
        }
        when (state.dialog) {
            MemberDialog.DEFAULT_PORTIONS -> save(
                request = { setDefaultPortionsUseCase(SetDefaultPortionsUseCase.Params(memberId, count)) },
                onSuccess = { current, result -> current.copy(defaultPortions = result) }
            )
            MemberDialog.PORTION -> {
                val record = state.selectedPortion ?: return
                save(
                    request = { setMemberPortionUseCase(SetMemberPortionUseCase.Params(memberId, record.date, count)) },
                    onSuccess = { current, _ -> current.copy(
                        portions = current.portions.map { if (it.date == record.date) it.copy(portions = count) else it }
                    ) }
                )
            }
            else -> return
        }
    }

    fun saveProfile(displayName: String, bio: String) {
        if (_state.value.dialog != MemberDialog.PROFILE || !_state.value.canEdit) return
        if (displayName.isBlank()) {
            _state.update { it.copy(error = "Введите имя участника") }
            return
        }
        save(
            request = { updateMemberProfileUseCase(UpdateMemberProfileUseCase.Params(memberId, displayName.trim(), bio)) },
            onSuccess = { current, result -> current.copy(
                localProfile = result,
                member = current.member?.copy(
                    displayName = result.displayName ?: current.member.displayName,
                    bio = result.bio ?: current.member.bio
                )
            ) }
        )
    }

    private fun <T> save(
        request: suspend () -> Either<AppError, T>,
        onSuccess: (MemberProfileState, T) -> MemberProfileState
    ) {
        if (!_state.value.canEdit) return
        _state.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            request().fold(
                { error -> _state.update { it.copy(isSaving = false, error = formatError(error)) } },
                { result -> _state.update {
                    onSuccess(it, result).copy(isSaving = false, dialog = null, selectedPortion = null, error = null)
                } }
            )
        }
    }

    private fun formatError(error: AppError): String = when (error) {
        is AppError.ApiError -> when (error.httpStatus) {
            403 -> "Нет доступа к участнику или этому действию"
            404 -> "Участник не найден или недоступен"
            else -> error.msg
        }
        is AppError.NetworkError -> "Ошибка сети. Проверьте подключение."
        is AppError.UnknownError -> error.msg ?: "Неизвестная ошибка"
    }
}

internal fun parsePortions(value: String): Long? = value.trim().takeIf {
    it.isNotEmpty() && it.all { char -> char in '0'..'9' }
}?.toLongOrNull()
