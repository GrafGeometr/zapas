package com.example.zapas.features.profile.presentation.ui

data class ProfileUiActions(
    val onBack: () -> Unit = {},
    val onLogout: () -> Unit = {},
    val onToggleEdit: () -> Unit = {},
    val onSave: (name: String, bio: String) -> Unit = { _, _ -> },
    val onShowPasswordDialog: () -> Unit = {},
    val onDismissPasswordDialog: () -> Unit = {},
    val onChangePassword: (old: String, new: String) -> Unit = { _, _ -> },
    val onShowDeleteDialog: () -> Unit = {},
    val onDismissDeleteDialog: () -> Unit = {},
    val onDeleteConfirm: () -> Unit = {}
)
