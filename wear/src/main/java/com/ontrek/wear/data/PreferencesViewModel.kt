package com.ontrek.wear.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ontrek.shared.api.TokenProvider
import com.ontrek.wear.StoreApplication
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PreferencesViewModel(
    private val preferencesStore: PreferencesStore
): ViewModel(), TokenProvider {

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as StoreApplication)
                PreferencesViewModel(application.preferencesStore)
            }
        }
    }

    val tokenState: StateFlow<String?> =
        preferencesStore.currentToken.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    val currentUserState: StateFlow<String?> =
        preferencesStore.currentUser.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    fun saveToken(userName: String) {
        viewModelScope.launch {
            preferencesStore.saveToken(userName)
        }
    }

    fun saveCurrentUser(userId: String) {
        viewModelScope.launch {
            preferencesStore.saveCurrentUser(userId)
        }
    }

    fun clearToken() {
        viewModelScope.launch {
            preferencesStore.clearToken()
        }
    }

    override fun getToken(): String? {
        return tokenState.value
    }
}