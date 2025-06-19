package com.ontreck.wear.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

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
fun OnTrekSmartwatchTheme(
    content: @Composable () -> Unit
) {
    /**
     * Empty theme to customize for your app.
     * See: https://developer.android.com/jetpack/compose/designsystems/custom
     */
    MaterialTheme(
        colors = CustomColorPalette,
        content = content
    )
}