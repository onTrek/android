package com.ontrek.wear.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme
import com.ontrek.shared.theme.backgroundDark
import com.ontrek.shared.theme.errorDark
import com.ontrek.shared.theme.onBackgroundDark
import com.ontrek.shared.theme.onErrorDark
import com.ontrek.shared.theme.onPrimaryDark
import com.ontrek.shared.theme.onSecondaryDark
import com.ontrek.shared.theme.onSurfaceDark
import com.ontrek.shared.theme.onSurfaceVariantDark
import com.ontrek.shared.theme.primaryDark
import com.ontrek.shared.theme.secondaryDark
import com.ontrek.shared.theme.surfaceDark

private val ColorPalette = Colors(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    error = errorDark,
    onError = onErrorDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    onSurfaceVariant = onSurfaceVariantDark,
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
        colors = ColorPalette,
        content = content
    )
}