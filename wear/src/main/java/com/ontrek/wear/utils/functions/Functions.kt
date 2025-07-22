package com.ontrek.wear.utils.functions

import io.ticofab.androidgpxparser.parser.domain.TrackPoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

fun getDistanceTo(point1: TrackPoint, point2: TrackPoint): Double {
    val earthRadiusKm = 6371.0

    val lat1Rad = Math.toRadians(point1.latitude)
    val lat2Rad = Math.toRadians(point2.latitude)
    val lon1Rad = Math.toRadians(point1.longitude)
    val lon2Rad = Math.toRadians(point2.longitude)

    val dLat = lat2Rad - lat1Rad
    val dLon = lon2Rad - lon1Rad

    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(lat1Rad) * cos(lat2Rad) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))

    val surfaceDistance = earthRadiusKm * c * 1000 // in meters

    // Elevation difference in meters
    val elevationDiff = (point2.elevation ?: 0.0) - (point1.elevation ?: 0.0)

    // Total 3D distance using Pythagorean theorem
    val totalDistance = sqrt(surfaceDistance.pow(2.0) + elevationDiff.pow(2.0))

    return totalDistance
}
