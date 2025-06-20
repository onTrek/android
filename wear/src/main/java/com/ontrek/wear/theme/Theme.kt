package com.ontrek.wear.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme
import com.ontrek.shared.theme.DarkGray
import com.ontrek.shared.theme.DarkOlive
import com.ontrek.shared.theme.DeepGreen
import com.ontrek.shared.theme.LightGreen
import com.ontrek.shared.theme.MediumGreen
import com.ontrek.shared.theme.OliveGreen

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