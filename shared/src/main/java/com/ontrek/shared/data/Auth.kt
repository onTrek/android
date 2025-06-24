package com.ontrek.shared.data

data class Login(
    val email: String,
    val password: String
)

data class TokenResponse(
    val token: String
)


data class Signup(
    val email: String,
    val username: String,
    val password: String,
)