package com.ontrek.shared.data

data class TrackPoint(
    val latitude: Double,
    val longitude: Double,
    val elevation: Double?,
    val distance: Double
)

data class SimplePoint(
    val latitude: Double,
    val longitude: Double,
    val elevation: Double?,
)