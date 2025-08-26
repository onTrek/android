package com.ontrek.wear.utils.functions

import android.location.Location
import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.sin

data class PolarResult(val offset: Offset, val isCapped: Boolean)

fun polarToCartesian(
    centerX: Float,
    centerY: Float,
    distanceMeters: Float,
    bearingDegrees: Float,
    maxDistanceMeters: Float,
    maxRadiusPx: Float
): PolarResult {
    val isCapped = distanceMeters > maxDistanceMeters
    val clampedDistance = distanceMeters.coerceIn(0f, maxDistanceMeters)

    val radius = when {
        clampedDistance <= 50f -> (clampedDistance / 50f) * (0.33f * maxRadiusPx)
        clampedDistance <= 250f -> (0.33f * maxRadiusPx) +
                ((clampedDistance - 50f) / 200f) * (0.33f * maxRadiusPx)
        else -> (0.66f * maxRadiusPx) +
                ((clampedDistance - 250f) / (maxDistanceMeters - 250f)) * (0.33f * maxRadiusPx)
    }.coerceAtMost(0.99f * maxRadiusPx)

    val angleRad = Math.toRadians(bearingDegrees.toDouble() - 90)
    val x = centerX + (radius * cos(angleRad)).toFloat()
    val y = centerY + (radius * sin(angleRad)).toFloat()

    return PolarResult(Offset(x, y), isCapped)
}

fun computeDistanceAndBearing(userLat: Double, userLon: Double, friendLat: Double, friendLon: Double): Pair<Float, Float> {
    val results = FloatArray(2)
    Location.distanceBetween(userLat, userLon, friendLat, friendLon, results)
    val distance = results[0]
    val bearing = results[1]
    return distance to bearing
}
