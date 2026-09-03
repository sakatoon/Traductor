package com.sakatoon.traductor.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private val DOWNLOAD_ONLY_ON_WIFI = booleanPreferencesKey("download_only_on_wifi")
    private val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
    private val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")

    val downloadOnlyOnWifi: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DOWNLOAD_ONLY_ON_WIFI] ?: true
        }

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_FIRST_LAUNCH] ?: true
        }

    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_DARK_MODE] ?: true // Default to dark mode as requested
        }

    suspend fun setDownloadOnlyOnWifi(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DOWNLOAD_ONLY_ON_WIFI] = enabled
        }
    }

    suspend fun setFirstLaunchCompleted() {
        context.dataStore.edit { preferences ->
            preferences[IS_FIRST_LAUNCH] = false
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_MODE] = enabled
        }
    }
}
