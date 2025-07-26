package com.ontrek.shared.data

data class Profile(
    val email: String,
    val username: String,
    val id: String,
)

data class UserMinimal(
    val id: String,
    val username: String,
    val state: Int = 0,
)