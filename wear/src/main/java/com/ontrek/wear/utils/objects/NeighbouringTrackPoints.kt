package com.ontrek.wear.utils.objects

data class NearPoint(
    val index: Int,
    val distanceToUser: Double,
)

data class SectionDistances(
    val firstToMe: Double,
    val lastToMe: Double,
)