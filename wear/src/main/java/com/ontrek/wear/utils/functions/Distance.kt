package com.ontrek.wear.utils.functions

import android.location.Location
import com.ontrek.shared.data.SimplePoint
import io.ticofab.androidgpxparser.parser.domain.TrackPoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
fun getDistanceTo(point1: SimplePoint, point2: SimplePoint): Double {
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
    val elevationAvailable = point1.elevation != null && point2.elevation != null
    val elevationDiff = if (elevationAvailable) (point2.elevation!!) - (point1.elevation!!) else 0.0

    // Total 3D distance using Pythagorean theorem
    val totalDistance = sqrt(surfaceDistance.pow(2.0) + elevationDiff.pow(2.0))

    return totalDistance
}

fun getDistanceTo(point1: TrackPoint, point2: TrackPoint): Double {
    return getDistanceTo(
        SimplePoint(point1.latitude, point1.longitude, point1.elevation ?: 0.0),
        SimplePoint(point2.latitude, point2.longitude, point2.elevation ?: 0.0)
    )
}

fun distanceToTrack(
    gpsLocation: Location,
    trackPoints: List<com.ontrek.shared.data.TrackPoint>
): Double {
    if (trackPoints.isEmpty()) return Double.MAX_VALUE

    return trackPoints.minOfOrNull { point ->
        getDistanceTo(
            SimplePoint(gpsLocation.latitude, gpsLocation.longitude, if (gpsLocation.hasAltitude()) gpsLocation.altitude else null),
            SimplePoint(point.latitude, point.longitude, point.elevation)
        )
    } ?: Double.MAX_VALUE
}
