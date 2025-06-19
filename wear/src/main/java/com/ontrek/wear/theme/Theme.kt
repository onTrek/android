package com.ontrek.wear.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme
import com.ontrek.shared.DarkGray
import com.ontrek.shared.DarkOlive
import com.ontrek.shared.DeepGreen
import com.ontrek.shared.LightGreen
import com.ontrek.shared.MediumGreen
import com.ontrek.shared.OliveGreen

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