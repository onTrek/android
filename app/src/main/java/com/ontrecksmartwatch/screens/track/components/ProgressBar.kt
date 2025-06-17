package com.ontrecksmartwatch.screens.track.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.tooling.preview.devices.WearDevices
import com.ontrecksmartwatch.theme.OnTrekSmartwatchTheme
import kotlin.math.min


@Composable
fun ProgressBar(
    progress: Float, // from 0f to 1f
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 3.dp,
    color: Color = MaterialTheme.colors.primary,
    backgroundColor: Color = MaterialTheme.colors.onSurfaceVariant
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(strokeWidth / 2) // Prevent clipping
    ) {
        val diameter = with(LocalDensity.current) {
            min(
                this@BoxWithConstraints.maxWidth.toPx(),
                this@BoxWithConstraints.maxHeight.toPx()
            )
        }
        val strokePx = with(LocalDensity.current) { strokeWidth.toPx() }

        Canvas(modifier = Modifier.fillMaxSize()) {
            // Background arc (full circle)
            drawArc(
                color = backgroundColor,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = Offset(
                    (size.width - diameter) / 2f,
                    (size.height - diameter) / 2f
                ),
                size = Size(diameter, diameter),
                style = Stroke(width = strokePx)
            )

            // Foreground progress arc
            drawArc(
                color = color,
                startAngle = 135f,
                sweepAngle = 270f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = Offset(
                    (size.width - diameter) / 2f,
                    (size.height - diameter) / 2f
                ),
                size = Size(diameter, diameter),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun PreviewProgressBar() {
    OnTrekSmartwatchTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ProgressBar(
                progress = 0.75f,
                strokeWidth = 8.dp,
            )
        }
    }
}
