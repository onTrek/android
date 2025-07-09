package com.ontrek.wear.utils.objects

import com.ontrek.shared.data.TrackPoint

data class NeighbouringTrackPoints(
    val nearestPoint: TrackPoint,
    val nextPoint: TrackPoint?,
)

data class NearestPoint(
    val index: Int,
    val distance: Double,
)