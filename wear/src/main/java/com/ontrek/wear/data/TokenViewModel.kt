package com.ontrek.wear.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ontrek.wear.StoreApplication
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UiState (
    val token: String
)

class TokenViewModel(
    private val tokenStore: TokenStore
): ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as StoreApplication)
                TokenViewModel(application.tokenStore)
            }
        }
    }

    val uiState: StateFlow<UiState> =
        tokenStore.currentToken.map { token ->
            UiState(token)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState("undefined")
        )

    fun saveToken(userName: String) {
        viewModelScope.launch {
            tokenStore.saveToken(userName)
        }
    }

}