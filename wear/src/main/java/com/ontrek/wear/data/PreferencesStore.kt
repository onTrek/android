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
    }

    val currentToken: Flow<String> =
        dataStore.data.map { preferences ->
            preferences[TOKEN] ?: ""
        }

    suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[TOKEN] = token
        }
    }
}