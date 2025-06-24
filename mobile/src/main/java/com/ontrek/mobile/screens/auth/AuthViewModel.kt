package com.ontrek.mobile.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthMode {
    LOGIN, SIGNUP
}

data class AuthUiState(
    val email: String = "",
    val username: String = "",
    val password: String = "",
    val passwordRepeat: String = "",
    val passwordVisible: Boolean = false,
    val passwordRepeatVisible: Boolean = false,
    val isLoading: Boolean = false,
    val authMode: AuthMode = AuthMode.LOGIN,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    // Funzione per aggiornamenti diretti e flessibili
    fun updateState(update: (AuthUiState) -> AuthUiState) {
        _uiState.update(update)
    }

    // Funzioni di aggiornamento dei campi
    fun updateEmail(value: String) {
        _uiState.update { it.copy(email = value) }
    }

    fun updateUsername(value: String) {
        _uiState.update { it.copy(username = value) }
    }

    fun updatePassword(value: String) {
        _uiState.update { it.copy(password = value) }
    }

    fun updatePasswordRepeat(value: String) {
        _uiState.update { it.copy(passwordRepeat = value) }
    }

    // Funzioni per toggle visibilità password
    fun togglePasswordVisibility() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun togglePasswordRepeatVisibility() {
        _uiState.update { it.copy(passwordRepeatVisible = !it.passwordRepeatVisible) }
    }

    // Cambio tra login e signup
    fun switchAuthMode() {
        _uiState.update {
            it.copy(authMode = if (it.authMode == AuthMode.LOGIN) AuthMode.SIGNUP else AuthMode.LOGIN)
        }
    }

    // Resetta messaggi di errore e successo
    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    // Funzione per il login
    fun login() {
        val currentState = _uiState.value
        val email = currentState.email
        val password = currentState.password

        if (email.isEmpty() || password.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Email and password are required") }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                // Simula una chiamata API
                delay(1000)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Successfully logged in!",
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error during login: ${e.message}"
                    )
                }
            }
        }
    }

    // Funzione per la registrazione
    fun signUp() {
        val currentState = _uiState.value
        val email = currentState.email
        val username = currentState.username
        val password = currentState.password
        val passwordRepeat = currentState.passwordRepeat

        if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "All fields are required") }
            return
        }

        if (password != passwordRepeat) {
            _uiState.update { it.copy(errorMessage = "Passwords do not match") }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                delay(1500)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Successfully registered! Please log in.",
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Error during registration: ${e.message}"
                    )
                }
            }
        }
    }
}