package com.example.zapas.core.local.storage

interface KeyValueStorage {
    suspend fun saveString(key: String, value: String)
    suspend fun getString(key: String, defaultValue: String? = null): String?

    suspend fun saveInt(key: String, value: Int)
    suspend fun getInt(key: String, defaultValue: Int = 0): Int

    suspend fun saveBoolean(key: String, value: Boolean)
    suspend fun getBoolean(key: String, defaultValue: Boolean = false): Boolean

    suspend fun saveLong(key: String, value: Long)
    suspend fun getLong(key: String, defaultValue: Long = 0L): Long

    suspend fun saveFloat(key: String, value: Float)
    suspend fun getFloat(key: String, defaultValue: Float = 0f): Float

    suspend fun remove(key: String)
    suspend fun clear()

    suspend fun contains(key: String): Boolean
}
