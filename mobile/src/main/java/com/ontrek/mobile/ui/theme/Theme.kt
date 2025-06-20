package com.ontrek.mobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.ontrek.shared.theme.DarkGray
import com.ontrek.shared.theme.DarkOlive
import com.ontrek.shared.theme.DeepGreen
import com.ontrek.shared.theme.LightGreen
import com.ontrek.shared.theme.OliveGreen

private val DarkColorScheme = darkColorScheme(
    primary = LightGreen,
    secondary = OliveGreen,
    background = DarkGray,
    surface = LightGreen,
    onPrimary = DeepGreen,
    onSecondary = LightGreen,
    onBackground = LightGreen,
    onSurface = DeepGreen,
    onSurfaceVariant = DarkGray,
    onError = LightGreen,
)

private val LightColorScheme = lightColorScheme(
    primary = DarkOlive,
    secondary = OliveGreen,
//    background = DeepGreen,
    surface = DarkOlive,
    onPrimary = DeepGreen,
    onSecondary = DarkOlive,
    onBackground = DarkOlive,
    onSurface = DeepGreen,
    onSurfaceVariant = DarkGray,
    onError = DarkOlive,
)


@Composable
fun OnTrekTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}