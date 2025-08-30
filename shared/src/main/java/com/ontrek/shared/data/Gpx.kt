package com.ontrek.shared.data

import android.location.Location

data class TrackPoint(
    val latitude: Double,
    val longitude: Double,
    val elevation: Double?,
    val distanceToPrevious: Double,
    val totalDistanceTraveled: Float,
    val index: Int,
)

data class NextTrackPoint(
    val nextProbablePoint: Int,
    val nextTrackPoint: TrackPoint,
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
    val elevation: Double? = null,
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