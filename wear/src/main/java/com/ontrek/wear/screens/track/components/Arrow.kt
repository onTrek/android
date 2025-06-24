package com.ontrek.wear.screens.track.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.wear.compose.material3.MaterialTheme

/**
 * Componente che disegna una freccia direzionale.
 *
 * @param modifier Modificatore per personalizzare l'aspetto
 * @param direction Angolo di rotazione in gradi (0 = Nord, 90 = Est, ecc)
 * @param color Colore della freccia
 */
@Composable
fun Arrow(
    modifier: Modifier = Modifier,
    direction: Float,
    color: Color? = null
) {
    val correctColor = MaterialTheme.colorScheme.primaryContainer
    val wrongColor = MaterialTheme.colorScheme.errorContainer
    val normalizedDirection = if (direction > 180f) 360f - direction else direction
    val fraction = normalizedDirection / 180f
    val arrowColor = color ?: lerp(correctColor, wrongColor, fraction)

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2f

        // Rotazione negativa per allineare correttamente con la direzione Nord
        rotate(-direction) {
            // Disegna il corpo della freccia (linea principale)
            drawLine(
                color = arrowColor,
                start = Offset(center.x, center.y + radius),
                end = Offset(center.x, center.y - radius),
                strokeWidth = 20f,  // Linea più spessa
                cap = StrokeCap.Round
            )

            // Disegna la punta della freccia
            val arrowHeadSize = radius * 0.5f
            drawLine(
                color = arrowColor,
                start = Offset(center.x, center.y - radius),
                end = Offset(center.x - arrowHeadSize, center.y - radius + arrowHeadSize),
                strokeWidth = 20f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = arrowColor,
                start = Offset(center.x, center.y - radius),
                end = Offset(center.x + arrowHeadSize, center.y - radius + arrowHeadSize),
                strokeWidth = 20f,
                cap = StrokeCap.Round
            )
        }
    }
}