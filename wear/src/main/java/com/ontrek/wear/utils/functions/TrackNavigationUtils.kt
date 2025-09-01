package com.ontrek.wear.utils.functions

import android.location.Location
import android.util.Log
import com.ontrek.shared.data.NextTrackPoint
import com.ontrek.shared.data.SimplePoint
import com.ontrek.shared.data.TrackPoint
import com.ontrek.shared.data.toSimplePoint
import com.ontrek.wear.screens.track.degreesThreshold
import com.ontrek.wear.screens.track.notificationTrackDistanceThreshold
import com.ontrek.wear.screens.track.trackPointThreshold
import com.ontrek.wear.utils.objects.NearPoint
import com.ontrek.wear.utils.objects.SectionDistances
import kotlin.math.abs
import kotlin.math.min

fun findNextTrackPoint(currentLocation: Location, trackPoints: List<TrackPoint>, probablePointIndex: Int?, hasBeenNearTheTrack: Boolean): NextTrackPoint {
    val threadSafePosition = currentLocation
    val probableNextPoint = if (probablePointIndex == null) {
        val nearestPoints = getNearestPoints(threadSafePosition.toSimplePoint(), trackPoints)
        val nearestPoint = nearestPoints[0]
        if (nearestPoint.index > trackPoints.size - min(7, trackPoints.size)) nearestPoints.find { it.index < 5 } ?: nearestPoint else nearestPoint
    } else {
        extractNearestPoint(currentLocation, trackPoints, probablePointIndex, hasBeenNearTheTrack)
    }

    Log.d("TrackScreenUtils", "Probable next point: ${probableNextPoint.index}, with distance ${probableNextPoint.distanceToUser}")

    var probableNextPointIndex = probableNextPoint.index
    var probableNextPointDistance = probableNextPoint.distanceToUser

    // --- Checks to skip entirely the next steps ---

    // Simplify the case where the user is at the first point
    if (probableNextPointIndex == 0) {
        return NextTrackPoint(1, trackPoints[1])
    }
    if (probableNextPointIndex == trackPoints.size - 1) {
        // If we are at the last point, we can stop
        return NextTrackPoint(probableNextPoint.index,trackPoints[trackPoints.size - 1])
    }

    // If the chosen point hits the threshold,
    // we provide the next point that does not hit the threshold
    while (probableNextPointDistance <= trackPointThreshold) {
        probableNextPointIndex++
        if (trackPoints.size <= probableNextPointIndex) {
            // If we are at the last point, we can stop
            return NextTrackPoint(probableNextPoint.index,trackPoints[probableNextPointIndex - 1])
        }
        probableNextPointDistance = getDistanceTo(
            threadSafePosition.toSimplePoint(),
            trackPoints[probableNextPointIndex].toSimplePoint()
        )
        if (probableNextPointDistance > trackPointThreshold) {
            return NextTrackPoint(probableNextPoint.index,trackPoints[probableNextPointIndex])
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

    if (offset1 > offset2 && offset2 < notificationTrackDistanceThreshold) {
        // We are closer to the second point and we are onTrack even with this new point, so we can return it
        return NextTrackPoint(probableNextPoint.index,trackPoints[W+1])
    }
    // We are closer to the first point, or the first point is a best candidate
    return NextTrackPoint(probableNextPoint.index,trackPoints[W])
}

fun extractNearestPoint(position: Location, trackPoints: List<TrackPoint>, probablePointIndex: Int, hasBeenNearTheTrack: Boolean) : NearPoint {
    val threadSafePosition = position

    //So if we have an actualPointIndex of 2, the array becomes [2, 3, 4, 5, ..., 0, 1]
    val trackPointsLooper = trackPoints.subList(probablePointIndex, trackPoints.size) + trackPoints.subList(0, probablePointIndex)

    var bestPointDistanceFromTrack = computeDistanceFromTrack(threadSafePosition, trackPoints, probablePointIndex)
    var pointIndex = probablePointIndex
    var pointUnderThresholdFound = false

    for (point in trackPointsLooper) {
        if (point.index == 0 || point.index == probablePointIndex) {
            continue // Skip the first and last points
        }
        if (!hasBeenNearTheTrack &&
            (point.index > trackPoints.size / 1.5) &&
            getDistanceTo(trackPoints[0].toSimplePoint(), trackPoints[point.index].toSimplePoint()) < 50) {
            continue //Skip the point if we are too far from the start and the point is close to the start
        }
        val distanceOfPointInExam = computeDistanceFromTrack(threadSafePosition, trackPoints, point.index)
        if (distanceOfPointInExam <= bestPointDistanceFromTrack) {
            bestPointDistanceFromTrack = distanceOfPointInExam
            pointIndex = point.index
        } else if (pointUnderThresholdFound) {
            break
        }
        if (bestPointDistanceFromTrack <= notificationTrackDistanceThreshold) {
            pointUnderThresholdFound = true
        }
        //If we found a point under the threshold, we don't need to search in the starting points, unless we are at startup
        if (pointUnderThresholdFound && point.index == trackPoints.size - 1) break
    }

    return NearPoint(
        index = pointIndex,
        distanceToUser = getDistanceTo(
            threadSafePosition.toSimplePoint(), trackPoints[pointIndex].toSimplePoint()
        )
    )
}

fun computeDistanceFromTrack(currentLocation: Location, trackPoints: List<TrackPoint>, analysisPointIndex: Int): Double {
    val nextPoint = trackPoints[analysisPointIndex]
    val previousPoint = trackPoints[analysisPointIndex - 1]

    val previousPointDistance = getDistanceTo(
        currentLocation.toSimplePoint(), previousPoint.toSimplePoint()
    )
    val nextPointDistance = getDistanceTo(
        currentLocation.toSimplePoint(),
        nextPoint.toSimplePoint()
    )

    return previousPointDistance + nextPointDistance - getDistanceTo(previousPoint.toSimplePoint(), nextPoint.toSimplePoint())
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