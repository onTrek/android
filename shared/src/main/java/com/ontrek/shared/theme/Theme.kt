package com.ontrek.shared.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Typography

private val CustomColorPalette = Colors(
    primary = LightGreen,
    primaryVariant = MediumGreen,
    secondary = OliveGreen,
    secondaryVariant = DarkOlive,
    background = DeepGreen,
    surface = LightGreen,
    onPrimary = DeepGreen,
    onSecondary = LightGreen,
    onBackground = LightGreen,
    onSurface = DeepGreen,
    onSurfaceVariant = DarkGray,
    onError = LightGreen,
)

@Composable
fun OnTrekTheme(
    typography: Typography = MaterialTheme.typography,
    content: @Composable () -> Unit
) {
    /**
     * Empty theme to customize for your app.
     * See: https://developer.android.com/jetpack/compose/designsystems/custom
     */
    MaterialTheme(
        colors = CustomColorPalette,
        typography = typography,
        content = content
    )
}