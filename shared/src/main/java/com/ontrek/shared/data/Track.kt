package com.ontrek.shared.data

data class TrackStats(
    val km: Float,
    val duration: String,
    val ascent: Double,
    val descent: Double,
    val max_altitude: Int,
    val min_altitude: Int
)

data class Track(
    val owner: String,
    val id: Int,
    val filename: String,
    val stats: TrackStats,
    val title: String,
    val size: Long,
    val upload_date: String
)
