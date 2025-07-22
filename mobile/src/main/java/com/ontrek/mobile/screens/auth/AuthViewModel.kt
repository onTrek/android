package com.ontrek.mobile.screens.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import com.ontrek.shared.api.auth.login
import com.ontrek.shared.api.auth.signup
import com.ontrek.shared.data.Login

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
    fun loginFunc(saveToken: (String) -> Unit) {
        val currentState = _uiState.value
        val email = currentState.email
        val password = currentState.password

        if (email.isEmpty() || password.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Email and password are required") }
            return
        }

        // Controllo se l'email è valida
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(errorMessage = "Invalid email format") }
            return
        }

        login(
            loginBody = Login(email, password),
            onSuccess = { response ->
                val token = response?.token ?: ""
                if (token.isNotEmpty()) {
                    saveToken(token)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Login successful!",
                        )
                        // Reset email and password fields after successful login
                        .copy(email = "", password = "")
                    }
                } else {
                    _uiState.update { it.copy(
                        isLoading = false,
                        errorMessage = "Login failed: No token received"
                    ) }
                }
            },
            onError = { error ->
                val msg = when (error) {
                    "401" -> "Login failed: Invalid credentials"
                    "403" -> "Login failed: Access forbidden"
                    "404" -> "Login failed: User not found"
                    "500" -> "Login failed: Server error"
                    "400" -> "Login failed: Email or password is incorrect"
                    else -> "Login failed: $error"
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = msg
                    )
                }
            }
        )
    }

    // Funzione per la registrazione
    fun signUpFunc() {
        val currentState = _uiState.value
        val email = currentState.email
        val username = currentState.username
        val password = currentState.password
        val passwordRepeat = currentState.passwordRepeat

        if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "All fields are required") }
            return
        }

        // Controllo se l'email è valida
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update { it.copy(errorMessage = "Invalid email format") }
            return
        }

        if (password != passwordRepeat) {
            _uiState.update { it.copy(errorMessage = "Passwords do not match") }
            return
        }

        signup(
            signupBody = com.ontrek.shared.data.Signup(email, username, password),
            onSuccess = { response ->
                _uiState.update {
                    it.copy(
                        username = "",
                        email = email,
                        password = "",
                        passwordRepeat = "",
                        passwordVisible = false,
                        passwordRepeatVisible = false,
                        authMode = AuthMode.LOGIN,
                        successMessage = "Registration successful! Welcome, $username!",
                    )
                }
            },
            onError = { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Registration failed: $error"
                    )
                }
            }
        )
    }
}