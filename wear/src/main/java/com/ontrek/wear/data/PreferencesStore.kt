package com.ontrek.wear.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PreferencesStore(private val dataStore: DataStore<Preferences>) {
    private companion object {
        val TOKEN = stringPreferencesKey("token")
        val CURRENT_USER = stringPreferencesKey("currentUser")
    }

    val currentToken: Flow<String> =
        dataStore.data.map { preferences ->
            preferences[TOKEN] ?: ""
        }

    val currentUser: Flow<String> =
        dataStore.data.map { preferences ->
            preferences[CURRENT_USER] ?: ""
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
}