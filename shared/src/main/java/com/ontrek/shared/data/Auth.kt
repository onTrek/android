package com.ontrek.shared.data

data class Login(
    val email: String,
    val password: String
)

data class LoginResponse (
    val token: String,
    val id: String
)


data class Signup(
    val email: String,
    val username: String,
    val password: String,
)

enum class AuthMode {
    LOGIN, SIGNUP
}


data class AuthUIData(
    val email: String = "",
    val username: String = "",
    val password: String = "",
    val passwordRepeat: String = "",
    val authMode: AuthMode = AuthMode.LOGIN,
    val isLoading: Boolean = false,
)