package com.example.zapas.features.members.presentation.ui

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.zapas.features.members.presentation.viewmodel.MemberProfileState
import com.example.zapas.ui.theme.ZapasTheme

data class MemberProfileDialogPreviewState(val state: MemberProfileState, val displayName: String, val bio: String)

class MemberProfileDialogPreviewProvider : PreviewParameterProvider<MemberProfileDialogPreviewState> {
    override val values = with(MemberPreviewSample.state.copy(showProfileDialog = true)) {
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
                showProfileDialog = true,
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
        MemberProfileDialog(MemberPreviewSample.state.copy(showProfileDialog = true), {}, { _, _ -> })
    }
}
