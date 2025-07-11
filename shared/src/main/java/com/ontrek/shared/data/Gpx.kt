package com.ontrek.shared.data

import android.location.Location

data class TrackPoint(
    val latitude: Double,
    val longitude: Double,
    val elevation: Double?,
    val distanceToPrevious: Double
)

fun TrackPoint.toSimplePoint(): SimplePoint {
    return SimplePoint(
        latitude = this.latitude,
        longitude = this.longitude,
        elevation = this.elevation
    )
}

data class SimplePoint(
    val latitude: Double,
    val longitude: Double,
    val elevation: Double?,
)

fun io.ticofab.androidgpxparser.parser.domain.TrackPoint.toSimplePoint(): SimplePoint {
    return SimplePoint(
        latitude = this.latitude,
        longitude = this.longitude,
        elevation = this.elevation
    )
}

fun Location.toSimplePoint(): SimplePoint {
    return SimplePoint(
        latitude = this.latitude,
        longitude = this.longitude,
        elevation = if (this.hasAltitude()) this.altitude else null
    )
}