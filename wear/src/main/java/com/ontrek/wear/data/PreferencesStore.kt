package com.ontrek.wear.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferencesStore(private val dataStore: DataStore<Preferences>) {
    private companion object {
        val TOKEN = stringPreferencesKey("token")
        val CURRENT_USER = stringPreferencesKey("currentUser")
        val FALL_DETECTED = booleanPreferencesKey("fallDetected")
    }

    val currentToken: Flow<String> =
        dataStore.data.map { preferences ->
            preferences[TOKEN] ?: ""
        }

    val currentUser: Flow<String> =
        dataStore.data.map { preferences ->
            preferences[CURRENT_USER] ?: ""
        }

    val fallDetected: Flow<Boolean> =
        dataStore.data.map { preferences ->
            preferences[FALL_DETECTED] ?: false
        }

    suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[TOKEN] = token
        }
    }

    suspend fun saveCurrentUser(userId: String) {
        dataStore.edit { preferences ->
            preferences[CURRENT_USER] = userId
        }
    }

    suspend fun clearToken() {
        dataStore.edit { preferences ->
            preferences.remove(TOKEN)
        }
    }

    suspend fun setFallDetected() {
        dataStore.edit { preferences ->
            preferences[FALL_DETECTED] = true
        }
    }

    suspend fun clearFallDetected() {
        dataStore.edit { preferences ->
            preferences[FALL_DETECTED] = false
        }
    }
}