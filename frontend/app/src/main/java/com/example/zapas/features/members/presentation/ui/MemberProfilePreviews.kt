package com.example.zapas.features.members.presentation.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.zapas.features.members.domain.model.LocalMemberProfile
import com.example.zapas.features.members.domain.model.MemberPortion
import com.example.zapas.features.members.presentation.viewmodel.MemberProfileState
import com.example.zapas.features.rooms.domain.model.Member
import com.example.zapas.ui.theme.ZapasTheme

internal object MemberPreviewSample {
    val state = MemberProfileState(
        member = Member("Описание в комнате", "2026-11-01T00:00:00Z", "Алексей", "member", "room", "user"),
        currentUserId = "user",
        localProfile = LocalMemberProfile("member", "room", "user", "2026-11-01T00:00:00Z", "Алексей", "Описание в комнате"),
        defaultPortions = 1,
        portions = listOf(MemberPortion("2026-11-01", 0), MemberPortion("2026-11-02", 2)),
        periodTitle = "ноябрь 2026",
        hasLoadedPortions = true,
        isLoading = false
    )
}

class MemberProfileStateProvider : PreviewParameterProvider<MemberProfileState> {
    override val values = with(MemberPreviewSample.state) {
        sequenceOf(
            MemberProfileState(periodTitle = periodTitle),
            this,
            copy(currentUserId = "other", localProfile = null),
            copy(currentUserId = null, localProfile = null),
            copy(isLoading = true),
            MemberProfileState(isLoading = false, error = "Участник не найден или недоступен"),
            MemberProfileState(isLoading = false, error = "Ошибка сети. Проверьте подключение."),
            copy(error = "Ошибка сети. Проверьте подключение."),
            copy(portions = emptyList()),
            copy(defaultPortions = 0, portions = listOf(MemberPortion("2026-11-01", 0))),
            copy(defaultPortions = null, portions = emptyList(), hasLoadedPortions = false, error = "Не удалось загрузить порции"),
            copy(localProfile = null, error = "Не удалось загрузить профиль в комнате"),
            copy(member = member?.copy(displayName = "", bio = ""))
        )
    }
}

@Preview(showBackground = true, name = "Карточка — состояния")
@Composable
private fun MemberProfileStatesPreview(
    @PreviewParameter(MemberProfileStateProvider::class) state: MemberProfileState
) {
    ZapasTheme {
        MemberProfileScreenContent(state = state, onBack = {}, onRefresh = {})
    }
}

@Preview(showBackground = true, name = "Карточка — тёмная тема", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, name = "Карточка — крупный текст", fontScale = 1.5f, widthDp = 320)
@Composable
private fun MemberProfileThemePreview() {
    ZapasTheme {
        MemberProfileScreenContent(state = MemberPreviewSample.state, onBack = {}, onRefresh = {})
    }
}
