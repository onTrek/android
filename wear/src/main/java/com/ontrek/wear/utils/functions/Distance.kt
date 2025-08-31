package com.ontrek.wear.utils.functions

import com.ontrek.shared.data.SimplePoint
import com.ontrek.shared.data.toSimplePoint
import com.ontrek.wear.utils.objects.NearPoint
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
    //TODO() Disabled for now
    //val elevationAvailable = point1.elevation != null && point2.elevation != null
    val elevationDiff = /*if (elevationAvailable) (point2.elevation!!) - (point1.elevation!!) else*/ 0.0

    // Total 3D distance using Pythagorean theorem
    val totalDistance = sqrt(surfaceDistance.pow(2.0) + elevationDiff.pow(2.0))

    return totalDistance
}

fun getNearestPoints(
    gpsLocation: SimplePoint,
    trackPoints: List<com.ontrek.shared.data.TrackPoint>
): List<NearPoint> {
    if (trackPoints.isEmpty()) throw IllegalArgumentException("Track points list cannot be empty")

    return getNearestPointsIndexes(gpsLocation, trackPoints).map { index ->
        val distance = getDistanceTo(
            gpsLocation,
            trackPoints[index].toSimplePoint()
        )
        NearPoint(index, distance)
    }
}

private fun getNearestPointsIndexes(
    gpsLocation: SimplePoint,
    trackPoints: List<com.ontrek.shared.data.TrackPoint>
): List<Int> {
    if (trackPoints.isEmpty()) throw IllegalArgumentException("Track points list cannot be empty")

    return trackPoints.indices.sortedBy { index ->
        getDistanceTo(
            gpsLocation,
            trackPoints[index].toSimplePoint()
        )
    }.take(5)
}
