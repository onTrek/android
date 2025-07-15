package com.ontrek.shared.data

import java.sql.Timestamp

data class Friend (
    val id: String,
    val username: String,
)

data class FriendRequest(
    val id: String,
    val username: String,
    val timestamp: Timestamp
)