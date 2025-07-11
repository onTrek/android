package com.ontrek.wear.utils.objects

data class NearestPoint(
    val index: Int,
    val distance: Double,
)

data class SectionDistances(
    val firstToMe: Double,
    val lastToMe: Double,
)