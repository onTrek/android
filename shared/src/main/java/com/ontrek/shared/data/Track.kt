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
    val id: Int,
    val filename: String,
    val stats: TrackStats,
    val title: String,
    val upload_date: String
)

data class GpxResponse(
    val gpx_files: List<Track>,
)