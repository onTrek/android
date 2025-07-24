package com.ontrek.mobile.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import com.ontrek.shared.api.auth.login
import com.ontrek.shared.api.auth.signup
import com.ontrek.shared.data.AuthMode
import com.ontrek.shared.data.AuthUIData
import com.ontrek.shared.data.Login
import com.ontrek.shared.data.Signup
import kotlinx.coroutines.launch


class AuthViewModel : ViewModel() {

    private val _authState = MutableStateFlow(AuthUIData())
    val authState: StateFlow<AuthUIData> = _authState

    private val _msgToast = MutableStateFlow("")
    val msgToast: StateFlow<String> = _msgToast

    fun updateState(
        email: String = _authState.value.email,
        username: String = _authState.value.username,
        password: String = _authState.value.password,
        passwordRepeat: String = _authState.value.passwordRepeat,
        authMode: AuthMode = _authState.value.authMode
    ) {
        _authState.update {
            it.copy(
                email = email,
                username = username,
                password = password,
                passwordRepeat = passwordRepeat,
                authMode = authMode
            )
        }
    }


    // Funzione per il login
    fun loginFunc(saveToken: (String) -> Unit, saveCurrentUser: (String) -> Unit) {
        val currentState = authState.value
        val email = currentState.email
        val password = currentState.password

        if (email.isEmpty() || password.isEmpty()) {
            _msgToast.value = "Email or password cannot be empty"
            return
        }

        // Controllo se l'email è valida
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _msgToast.value = "Invalid email format"
            return
        }

        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true) }
            login(
                loginBody = Login(email.trim(), password),
                onSuccess = { response ->
                    val token = response?.token ?: ""
                    if (token.isNotEmpty()) {
                        saveToken(token)
                        saveCurrentUser(response?.id ?: "")
                        _authState.update { AuthUIData() }
                    } else {
                        _msgToast.value = "Login failed, please try again"
                    }
                },
                onError = { error ->
                    _msgToast.value = "Login failed: $error"
                }
            )
            _authState.update { it.copy(isLoading = false) }
        }
    }

    // Funzione per la registrazione
    fun signUpFunc() {
        val currentState = authState.value
        val email = currentState.email
        val username = currentState.username
        val password = currentState.password
        val passwordRepeat = currentState.passwordRepeat

        if (email.trim().isEmpty() || username.trim().isEmpty() || password.isEmpty()) {
            _msgToast.value = "Email, username, and password cannot be empty"
            return
        }

        // Controllo se l'email è valida
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _msgToast.value = "Invalid email format"
            return
        }

        if (password != passwordRepeat) {
            _msgToast.value = "Passwords do not match"
            return
        }

        if (password.length < 6) {
            _msgToast.value = "Password must be at least 6 characters long"
            return
        }

        if (username.length < 3) {
            _msgToast.value = "Username must be at least 3 characters long"
            return
        }

        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true) }
            signup(
                signupBody = Signup(email.trim(), username.trim(), password),
                onSuccess = { response ->
                    _msgToast.value = "Signup successful! Please log in."
                    _authState.update { it.copy(authMode = AuthMode.LOGIN, username = "", password = "", passwordRepeat = "")}
                },
                onError = { error ->
                    _msgToast.value = "Signup failed: $error"
                }
            )
            _authState.update { it.copy(isLoading = false) }
        }
    }

    fun clearMsgToast() {
        _msgToast.value = ""
    }

    sealed class AuthState {
        object Loading : AuthState()
        data class Success(val auth: AuthUIData) : AuthState()
        data class Error(val message: String) : AuthState()
    }
}