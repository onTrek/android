package com.ontrecksmartwatch.screens.track

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate

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
    color: Color = Color.Red
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2f

        // Rotazione negativa per allineare correttamente con la direzione Nord
        rotate(-direction) {
            // Disegna il corpo della freccia (linea principale)
            drawLine(
                color = color,
                start = Offset(center.x, center.y + radius),
                end = Offset(center.x, center.y - radius),
                strokeWidth = 20f,  // Linea più spessa
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            // Disegna la punta della freccia
            val arrowHeadSize = radius * 0.5f
            drawLine(
                color = color,
                start = Offset(center.x, center.y - radius),
                end = Offset(center.x - arrowHeadSize, center.y - radius + arrowHeadSize),
                strokeWidth = 20f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            drawLine(
                color = color,
                start = Offset(center.x, center.y - radius),
                end = Offset(center.x + arrowHeadSize, center.y - radius + arrowHeadSize),
                strokeWidth = 20f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}