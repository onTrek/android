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