package com.ontrek.wear.utils.functions

import android.location.Location
import com.ontrek.shared.data.SimplePoint
import com.ontrek.shared.data.toSimplePoint
import com.ontrek.wear.utils.objects.NearestPoint
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

fun getNearestPoint(
    gpsLocation: Location,
    trackPoints: List<com.ontrek.shared.data.TrackPoint>
): NearestPoint {
    if (trackPoints.isEmpty()) return NearestPoint(-1,Double.MAX_VALUE)

    val nearestPointIndex = getNearestPointIndex(gpsLocation, trackPoints)
    if (nearestPointIndex == -1) return NearestPoint(-1,Double.MAX_VALUE)
    val distance = getDistanceTo(
        gpsLocation.toSimplePoint(),
        trackPoints[nearestPointIndex].toSimplePoint()
    )
    return NearestPoint(nearestPointIndex, distance)
}

private fun getNearestPointIndex(
    gpsLocation: Location,
    trackPoints: List<com.ontrek.shared.data.TrackPoint>
): Int {
    if (trackPoints.isEmpty()) return -1

    return trackPoints.indices.minByOrNull { index ->
        getDistanceTo(
            SimplePoint(gpsLocation.latitude, gpsLocation.longitude, if (gpsLocation.hasAltitude()) gpsLocation.altitude else null),
            trackPoints[index].toSimplePoint()
        )
    } ?: -1
}
