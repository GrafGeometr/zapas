package com.example.zapas.data.local.storage


import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.content.edit

class SharedPreferencesStorage(
    private val context: Context
) : KeyValueStorage {

    // Используем lazy для создания SharedPreferences
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("app_storage", Context.MODE_PRIVATE)
    }

    // Все методы делаем suspend, но внутри используем Dispatchers.IO
    // для работы с диском в фоновом потоке

    override suspend fun saveString(key: String, value: String) {
        withContext(Dispatchers.IO) {
            prefs.edit { putString(key, value) }
        }
    }

    override suspend fun getString(key: String, defaultValue: String?): String? {
        return withContext(Dispatchers.IO) {
            prefs.getString(key, defaultValue)
        }
    }

    override suspend fun saveInt(key: String, value: Int) {
        withContext(Dispatchers.IO) {
            prefs.edit { putInt(key, value) }
        }
    }

    override suspend fun getInt(key: String, defaultValue: Int): Int {
        return withContext(Dispatchers.IO) {
            prefs.getInt(key, defaultValue)
        }
    }

    override suspend fun saveBoolean(key: String, value: Boolean) {
        withContext(Dispatchers.IO) {
            prefs.edit { putBoolean(key, value) }
        }
    }

    override suspend fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return withContext(Dispatchers.IO) {
            prefs.getBoolean(key, defaultValue)
        }
    }

    override suspend fun saveLong(key: String, value: Long) {
        withContext(Dispatchers.IO) {
            prefs.edit { putLong(key, value) }
        }
    }

    override suspend fun getLong(key: String, defaultValue: Long): Long {
        return withContext(Dispatchers.IO) {
            prefs.getLong(key, defaultValue)
        }
    }

    override suspend fun saveFloat(key: String, value: Float) {
        withContext(Dispatchers.IO) {
            prefs.edit { putFloat(key, value) }
        }
    }

    override suspend fun getFloat(key: String, defaultValue: Float): Float {
        return withContext(Dispatchers.IO) {
            prefs.getFloat(key, defaultValue)
        }
    }

    override suspend fun remove(key: String) {
        withContext(Dispatchers.IO) {
            prefs.edit { remove(key) }
        }
    }

    override suspend fun clear() {
        withContext(Dispatchers.IO) {
            prefs.edit { clear() }
        }
    }

    override suspend fun contains(key: String): Boolean {
        return withContext(Dispatchers.IO) {
            prefs.contains(key)
        }
    }
}