package com.example.zapas.features.members.presentation.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.zapas.features.members.presentation.viewmodel.MemberDialog
import com.example.zapas.features.members.presentation.viewmodel.MemberProfileState
import com.example.zapas.ui.theme.ZapasTheme

data class MemberPortionsPreviewState(val state: MemberProfileState, val value: String)

class MemberPortionsPreviewProvider : PreviewParameterProvider<MemberPortionsPreviewState> {
    override val values = listOf(MemberDialog.DEFAULT_PORTIONS, MemberDialog.PORTION).flatMap { dialog ->
        val state = MemberPreviewSample.state.copy(
            dialog = dialog,
            selectedPortion = if (dialog == MemberDialog.PORTION) MemberPreviewSample.state.portions.first() else null
        )
        listOf(
            MemberPortionsPreviewState(state, "2"),
            MemberPortionsPreviewState(state, "0"),
            MemberPortionsPreviewState(state.copy(isSaving = true), "2"),
            MemberPortionsPreviewState(state.copy(error = "Ошибка сети. Проверьте подключение."), "2"),
            MemberPortionsPreviewState(state, ""),
            MemberPortionsPreviewState(state, "-1"),
            MemberPortionsPreviewState(state, "1.5"),
            MemberPortionsPreviewState(state, "9223372036854775808")
        )
    }.asSequence()
}

@Preview(showBackground = true, name = "Порции — состояния диалогов")
@Composable
private fun MemberPortionsStatesPreview(
    @PreviewParameter(MemberPortionsPreviewProvider::class) preview: MemberPortionsPreviewState
) {
    ZapasTheme {
        MemberPortionsDialogContent(preview.state, preview.value, {}, {}, {})
    }
}

data class MemberProfileDialogPreviewState(val state: MemberProfileState, val displayName: String, val bio: String)

class MemberProfileDialogPreviewProvider : PreviewParameterProvider<MemberProfileDialogPreviewState> {
    override val values = with(MemberPreviewSample.state.copy(dialog = MemberDialog.PROFILE)) {
        sequenceOf(
            MemberProfileDialogPreviewState(this, "Алексей", "Описание в комнате"),
            MemberProfileDialogPreviewState(copy(isSaving = true), "Алексей", "Описание в комнате"),
            MemberProfileDialogPreviewState(copy(error = "Ошибка сети. Проверьте подключение."), "Новое имя", "Новое описание"),
            MemberProfileDialogPreviewState(this, "", "Описание в комнате"),
            MemberProfileDialogPreviewState(this, "   ", "Описание в комнате"),
            MemberProfileDialogPreviewState(this, "Алексей", ""),
            MemberProfileDialogPreviewState(this, "Длинное имя участника комнаты", "Длинное описание. ".repeat(30))
        )
    }
}

@Preview(showBackground = true, name = "Профиль — состояния диалога")
@Composable
private fun MemberProfileDialogStatesPreview(
    @PreviewParameter(MemberProfileDialogPreviewProvider::class) preview: MemberProfileDialogPreviewState
) {
    ZapasTheme {
        MemberProfileDialogContent(preview.state, preview.displayName, preview.bio, {}, {}, {}, {})
    }
}

@Preview(showBackground = true, name = "Профиль — наследование глобальных значений")
@Composable
private fun MemberInheritedProfilePreview() {
    ZapasTheme {
        MemberProfileDialog(
            state = MemberPreviewSample.state.copy(
                dialog = MemberDialog.PROFILE,
                localProfile = MemberPreviewSample.state.localProfile?.copy(displayName = null, bio = null)
            ),
            onDismiss = {},
            onSave = { _, _ -> }
        )
    }
}

@Preview(showBackground = true, name = "Профиль — тёмная тема", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, name = "Профиль — крупный текст", fontScale = 1.5f, widthDp = 320)
@Composable
private fun MemberProfileDialogThemePreview() {
    ZapasTheme {
        MemberProfileDialog(MemberPreviewSample.state.copy(dialog = MemberDialog.PROFILE), {}, { _, _ -> })
    }
}

@Preview(showBackground = true, name = "Порции по умолчанию — тёмная тема", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, name = "Порции по умолчанию — крупный текст", fontScale = 1.5f, widthDp = 320)
@Composable
private fun MemberDefaultPortionsThemePreview() {
    ZapasTheme {
        MemberPortionsDialog(MemberPreviewSample.state.copy(dialog = MemberDialog.DEFAULT_PORTIONS), {}, {})
    }
}

@Preview(showBackground = true, name = "Порции на дату — тёмная тема", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true, name = "Порции на дату — крупный текст", fontScale = 1.5f, widthDp = 320)
@Composable
private fun MemberDailyPortionsThemePreview() {
    ZapasTheme {
        MemberPortionsDialog(MemberPreviewSample.state.copy(
            dialog = MemberDialog.PORTION, selectedPortion = MemberPreviewSample.state.portions.first()
        ), {}, {})
    }
}
