package com.example.zapas.data.local


import com.example.zapas.data.local.storage.KeyValueStorage

class AppSettingsManager(
    private val storage: KeyValueStorage
) {

    companion object {
        private const val KEY_THEME = "app_theme"
        private const val KEY_LANGUAGE = "app_language"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_SELECTED_ROOM_ID = "selected_room_id"
        private const val KEY_LAST_SYNC_TIME = "last_sync_time"
    }

    // Настройки темы
    suspend fun saveTheme(theme: String) {
        storage.saveString(KEY_THEME, theme)
    }

    suspend fun getTheme(): String {
        return storage.getString(KEY_THEME, "system") ?: "system"
    }

    // Настройки языка
    suspend fun saveLanguage(language: String) {
        storage.saveString(KEY_LANGUAGE, language)
    }

    suspend fun getLanguage(): String {
        return storage.getString(KEY_LANGUAGE, "ru") ?: "ru"
    }

    // Уведомления
    suspend fun saveNotificationsEnabled(enabled: Boolean) {
        storage.saveBoolean(KEY_NOTIFICATIONS_ENABLED, enabled)
    }

    suspend fun getNotificationsEnabled(): Boolean {
        return storage.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    // Первый запуск
    suspend fun setFirstLaunchCompleted() {
        storage.saveBoolean(KEY_FIRST_LAUNCH, false)
    }

    suspend fun isFirstLaunch(): Boolean {
        return storage.getBoolean(KEY_FIRST_LAUNCH, true)
    }

    // Работа с комнатами
    suspend fun saveSelectedRoomId(roomId: String?) {
        if (roomId == null) {
            storage.remove(KEY_SELECTED_ROOM_ID)
        } else {
            storage.saveString(KEY_SELECTED_ROOM_ID, roomId)
        }
    }

    suspend fun getSelectedRoomId(): String? {
        return storage.getString(KEY_SELECTED_ROOM_ID)
    }

    // Время последней синхронизации
    suspend fun updateLastSyncTime() {
        storage.saveLong(KEY_LAST_SYNC_TIME, System.currentTimeMillis())
    }

    suspend fun getLastSyncTime(): Long {
        return storage.getLong(KEY_LAST_SYNC_TIME, 0L)
    }

    suspend fun shouldSync(): Boolean {
        val lastSync = getLastSyncTime()
        if (lastSync == 0L) return true

        // Синхронизируем раз в 5 минут
        return System.currentTimeMillis() - lastSync > 5 * 60 * 1000
    }
}