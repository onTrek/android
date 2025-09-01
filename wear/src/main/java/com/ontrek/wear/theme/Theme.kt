package com.ontrek.wear.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import com.ontrek.shared.theme.backgroundDark
import com.ontrek.shared.theme.errorContainerDark
import com.ontrek.shared.theme.errorDark
import com.ontrek.shared.theme.onBackgroundDark
import com.ontrek.shared.theme.onErrorContainerDark
import com.ontrek.shared.theme.onErrorDark
import com.ontrek.shared.theme.onPrimaryContainerDark
import com.ontrek.shared.theme.onPrimaryDark
import com.ontrek.shared.theme.onSecondaryContainerDark
import com.ontrek.shared.theme.onSecondaryDark
import com.ontrek.shared.theme.onSurfaceDark
import com.ontrek.shared.theme.onSurfaceVariantDark
import com.ontrek.shared.theme.onTertiaryContainerDark
import com.ontrek.shared.theme.onTertiaryDark
import com.ontrek.shared.theme.outlineDark
import com.ontrek.shared.theme.outlineVariantDark
import com.ontrek.shared.theme.primaryDark
import com.ontrek.shared.theme.secondaryContainerDark
import com.ontrek.shared.theme.secondaryDark
import com.ontrek.shared.theme.surfaceContainerDark
import com.ontrek.shared.theme.surfaceContainerHighDark
import com.ontrek.shared.theme.surfaceContainerLowDark
import com.ontrek.shared.theme.tertiaryContainerDark
import com.ontrek.shared.theme.tertiaryDark

private val ColorPalette = ColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = Color(0xFF1E922F),
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    onSurface = onSurfaceDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
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
        colorScheme = ColorPalette,
        content = content
    )
}