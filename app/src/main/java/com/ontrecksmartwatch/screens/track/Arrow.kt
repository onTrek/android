package com.ontrecksmartwatch.screens.track

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

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
    // Aumentiamo la dimensione a 160.dp per una freccia più grande
    Canvas(modifier = modifier.size(160.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2.5f  // Aumentiamo anche la lunghezza proporzionalmente

        // Qui creo la freccia, poi il colore lo possiamo usare come parametro e cambiare a seconda delle necessità
        // Rotazione negativa per allineare correttamente con la direzione Nord
        rotate(-direction) {
            // Disegna il corpo della freccia (linea principale)
            drawLine(
                color = color,
                start = center,
                end = Offset(center.x, center.y - radius),
                strokeWidth = 12f  // Linea più spessa
            )

            // Disegna la punta della freccia
            val arrowHeadSize = radius * 0.35f
            drawLine(
                color = color,
                start = Offset(center.x, center.y - radius),
                end = Offset(center.x - arrowHeadSize, center.y - radius + arrowHeadSize),
                strokeWidth = 12f  // Linea più spessa
            )
            drawLine(
                color = color,
                start = Offset(center.x, center.y - radius),
                end = Offset(center.x + arrowHeadSize, center.y - radius + arrowHeadSize),
                strokeWidth = 12f  // Linea più spessa
            )
        }
    }
}