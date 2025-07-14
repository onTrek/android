package com.ontrek.shared.data

import java.sql.Timestamp

data class Friend (
    val id: Int,
    val username: String,
)

data class FriendRequest(
    val id: Int,
    val username: String,
    val timestamp: Timestamp
)