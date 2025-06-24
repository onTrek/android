package com.ontrek.mobile.screens.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
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

    private val _uiState = MutableLiveData(AuthUiState())
    val uiState: LiveData<AuthUiState> = _uiState

    // Funzioni di aggiornamento dei campi
    fun updateEmail(value: String) {
        _uiState.value = _uiState.value?.copy(email = value)
    }

    fun updateUsername(value: String) {
        _uiState.value = _uiState.value?.copy(username = value)
    }

    fun updatePassword(value: String) {
        _uiState.value = _uiState.value?.copy(password = value)
    }

    fun updatePasswordRepeat(value: String) {
        _uiState.value = _uiState.value?.copy(passwordRepeat = value)
    }

    // Funzioni per toggle visibilità password
    fun togglePasswordVisibility() {
        _uiState.value?.let {
            _uiState.value = it.copy(passwordVisible = !it.passwordVisible)
        }
    }

    fun togglePasswordRepeatVisibility() {
        _uiState.value?.let {
            _uiState.value = it.copy(passwordRepeatVisible = !it.passwordRepeatVisible)
        }
    }

    // Cambio tra login e signup
    fun switchAuthMode() {
        _uiState.value?.let {
            _uiState.value = it.copy(authMode = if (it.authMode == AuthMode.LOGIN) AuthMode.SIGNUP else AuthMode.LOGIN)
        }
    }

    // Resetta messaggi di errore e successo
    fun clearMessages() {
        _uiState.value?.let {
            _uiState.value = it.copy(errorMessage = null, successMessage = null)
        }
    }

    // Funzione per il login
    fun login() {
        val email = _uiState.value?.email ?: ""
        val password = _uiState.value?.password ?: ""

        if (email.isEmpty() || password.isEmpty()) {
            _uiState.value = _uiState.value?.copy(errorMessage = "Email e password sono obbligatori")
            return
        }

        _uiState.value = _uiState.value?.copy(isLoading = true)

        viewModelScope.launch {
            try {
                // Simula una chiamata API
                delay(1000)
                _uiState.postValue(_uiState.value?.copy(
                    isLoading = false,
                    successMessage = "Login effettuato con successo!"
                ))
            } catch (e: Exception) {
                _uiState.postValue(_uiState.value?.copy(
                    isLoading = false,
                    errorMessage = "Errore durante il login: ${e.message}"
                ))
            }
        }
    }

    // Funzione per la registrazione
    fun signUp() {
        val currentState = _uiState.value ?: return
        val email = currentState.email
        val username = currentState.username
        val password = currentState.password
        val passwordRepeat = currentState.passwordRepeat

        if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            _uiState.value = currentState.copy(errorMessage = "Tutti i campi sono obbligatori")
            return
        }

        if (password != passwordRepeat) {
            _uiState.value = currentState.copy(errorMessage = "Le password non coincidono")
            return
        }

        _uiState.value = currentState.copy(isLoading = true)

        viewModelScope.launch {
            try {
                delay(1500)
                _uiState.postValue(currentState.copy(
                    isLoading = false,
                    successMessage = "Registrazione effettuata con successo!"
                ))
            } catch (e: Exception) {
                _uiState.postValue(currentState.copy(
                    isLoading = false,
                    errorMessage = "Errore durante la registrazione: ${e.message}"
                ))
            }
        }
    }
}