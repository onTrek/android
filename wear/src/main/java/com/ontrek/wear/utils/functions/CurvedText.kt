package com.ontrek.wear.utils.functions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp


fun calculateFontSize(text: String): TextUnit {
    val maxFontSize = 12.sp
    val minFontSize = 7.sp

    return when {
        text.length <= 10 -> maxFontSize
        text.length >= 20 -> minFontSize
        else -> {
            val ratio = (text.length - 10) / 15f
            val fontSize = maxFontSize.value - ((maxFontSize.value - minFontSize.value) * ratio)
            fontSize.sp
        }
    }
}

fun getContrastingTextColor(backgroundColor: Color): Color {
    // Calculate perceived brightness using the formula:
    // (R * 0.299 + G * 0.587 + B * 0.114)
    // This formula considers human perception of different color components
    val brightness = (backgroundColor.red * 0.299f +
            backgroundColor.green * 0.587f +
            backgroundColor.blue * 0.114f)

    // Use white text for dark backgrounds, black text for light backgrounds
    // The threshold 0.5f works well for most colors
    return if (brightness < 0.5f) {
        Color.White
    } else {
        Color.Black
    }
}

fun getReadableDistance(meters: Double): String {
    return if (meters >= 1000) {
        "%.1f km".format(meters / 1000.0)
    } else {
        "${meters.toInt()} m"
    }
}