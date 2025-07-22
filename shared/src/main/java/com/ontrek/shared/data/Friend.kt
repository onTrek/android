package com.ontrek.shared.data

data class Friend (
    val id: String,
    val username: String,
)

data class FriendRequest(
    val id: String,
    val username: String,
    val date: String
)