package com.ontrek.shared.data

data class TrackInfo (
    val id: Int,
    val title: String,
)

data class Hikes (
    val group_id: Int,
    val description: String,
    val created_at: String,
    val created_by: String,
    val member_number: Int,
    val track: TrackInfo
)