package com.ontrek.wear.screens.track

import android.location.Location
import android.util.Log
import com.ontrek.shared.data.SimplePoint
import com.ontrek.shared.data.TrackPoint
import com.ontrek.shared.data.toSimplePoint
import com.ontrek.wear.utils.functions.getDistanceTo
import com.ontrek.wear.utils.functions.getNearestPoints
import com.ontrek.wear.utils.objects.NearPoint
import com.ontrek.wear.utils.objects.SectionDistances
import kotlin.math.abs
import kotlin.math.min

fun findNextTrackPoint(currentLocation: Location, trackPoints: List<TrackPoint>, onTrack: Boolean, actualPointIndex: Int?): TrackPoint {
    val threadSafePosition = currentLocation
    val probableNextPoint = if (actualPointIndex == null) {
        val nearestPoints = getNearestPoints(threadSafePosition, trackPoints)
        val nearestPoint = nearestPoints[0]
        if (nearestPoint.index > trackPoints.size - min(7, trackPoints.size)) nearestPoints.find { it.index < 5 } ?: nearestPoint else nearestPoint
    } else {
        extractNearestPoint(currentLocation, trackPoints, actualPointIndex)
    }
    var probableNextPointIndex = probableNextPoint.index
    var probableNextPointDistance = probableNextPoint.distanceToUser

    // --- Checks to skip entirely the next steps ---

    // Simplify the case where the user is at the first point
    if (probableNextPointIndex == 0) {
        return trackPoints[1]
    }
    if (probableNextPointIndex == trackPoints.size - 1) {
        // If we are at the last point, we can stop
        return trackPoints[trackPoints.size - 1]
    }

    // If the chosen point hits the threshold,
    // we provide the next point that does not hit the threshold
    while (probableNextPointDistance <= trackPointThreshold) {
        probableNextPointIndex++
        if (trackPoints.size <= probableNextPointIndex) {
            // If we are at the last point, we can stop
            return trackPoints[probableNextPointIndex - 1]
        }
        probableNextPointDistance = getDistanceTo(
            threadSafePosition.toSimplePoint(),
            trackPoints[probableNextPointIndex].toSimplePoint()
        )
        if (probableNextPointDistance > trackPointThreshold) {
            return trackPoints[probableNextPointIndex]
        }
    }

    // --- End of checks to skip ---

    // Finally, we need to understand which is the next one we need to "point to"
    // For variable naming, please refer to: https://github.com/onTrek/android/issues/40
    val W = probableNextPointIndex

    val otherPoints = calculateSectionDistances(
        trackPoints[W - 1].toSimplePoint(),
        threadSafePosition.toSimplePoint(),
        trackPoints[W + 1].toSimplePoint()
    )

    val A = probableNextPointDistance
    val B = otherPoints.firstToMe
    val C = otherPoints.lastToMe
    val X = trackPoints[W].distanceToPrevious
    val Y = trackPoints[W + 1].distanceToPrevious

    val offset1 = (A + B) - X
    val offset2 = (A + C) - Y

    if (offset1 < offset2) {
        // We are closer to the first point, so we can return it
        return trackPoints[W]
    }
    // We are closer to the second point, so we can return it
    return trackPoints[W + 1]
}

fun extractNearestPoint(position: Location, trackPoints: List<TrackPoint>, actualPointIndex: Int) : NearPoint {
    val threadSafePosition = position

    // Find the nearest track points to the current position
    val nearestPoints = getNearestPoints(threadSafePosition, trackPoints)
    val probableNextPoint = nearestPoints[0]

    Log.d("TRACK_SCREEN_VIEW_MODEL", "Nearest points: ${nearestPoints} - Distance: ${probableNextPoint.distanceToUser}")

    // If the nearest point is too far from the actual point index, we need to do a little bit of work
    // to avoid the case where a user might by in a track where there are two tracks close to each other
    // that go in different directions
    if (probableNextPoint.index <= actualPointIndex + 3 && probableNextPoint.index >= actualPointIndex - 1) return probableNextPoint

    // If the nearest points are close to each other, we can use the probable next point index
    // (probably the GPS fucked up at some point or lost the signal)...
    if (nearestPoints.maxBy { it.index }.index - nearestPoints.minBy { it.index }.index < 5) return probableNextPoint

    // But if they arent, we see if there is some point that is "close enough" to the actual point index
    val newProbableNextPoint = nearestPoints.find { it.index <= actualPointIndex + 3 && it.index >= actualPointIndex - 1 }

    // ...but if all the nearest points are too far from the actual point index,
    // we fallback the one that is "closest in the array" to the last point
    if (newProbableNextPoint == null) {
        Log.w(
            "TRACK_SCREEN_VIEW_MODEL",
            "No nearest point found close to the actual point index"
        )
        return nearestPoints.minBy { abs(it.index - actualPointIndex) }
    }

    return newProbableNextPoint
}


fun calculateSectionDistances(
    firstPoint: SimplePoint,
    location: SimplePoint,
    lastPoint: SimplePoint
): SectionDistances {
    val firstToMe = getDistanceTo(firstPoint, location)
    val lastToMe = getDistanceTo(lastPoint, location)

    return SectionDistances(firstToMe, lastToMe)
}

fun shouldUpdateDirection(newDirection: Double, oldDirection: Double?): Boolean {
    if (oldDirection == null) {
        return true
    }
    val diff = abs(newDirection - oldDirection)
    val wrappedDiff = diff.coerceAtMost(360 - diff)

    return wrappedDiff >= degreesThreshold
}