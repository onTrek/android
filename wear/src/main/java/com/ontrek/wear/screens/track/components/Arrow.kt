package com.ontrek.wear.screens.track.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import androidx.wear.compose.material3.MaterialTheme
import com.ontrek.wear.screens.track.notificationTrackDistanceThreshold

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
    distanceFromTrack: Double = 0.0,
    isOffTrack: Boolean = true
) {
    val correctColor = MaterialTheme.colorScheme.primaryContainer
    val middleColor = Color(0xFFFFA500) // Orange
    val wrongColor = MaterialTheme.colorScheme.errorContainer


    val arrowColor = if (isOffTrack) {
        val distanceFraction = distanceFromTrack.let {
            ((it - notificationTrackDistanceThreshold + 1 ) / (notificationTrackDistanceThreshold * 3))
                .toFloat()
                .coerceIn(0f, 1f)
        }
        lerp(middleColor, wrongColor, distanceFraction)
    } else correctColor

    // Memorizza l'ultimo valore di direzione per calcolare il percorso più breve
    val lastDirection = remember { mutableFloatStateOf(direction) }

    // Calcola la rotazione più breve
    val targetDirection = remember(direction) {
        val currentAngle = ((lastDirection.floatValue % 360) + 360) % 360
        val targetAngle = ((direction % 360) + 360) % 360

        // Calcola la differenza più breve
        var diff = targetAngle - currentAngle
        if (diff > 180f) diff -= 360f
        if (diff < -180f) diff += 360f

        val result = lastDirection.floatValue + diff
        lastDirection.floatValue = result
        result
    }

    val animatedDirection by animateFloatAsState(
        targetValue = targetDirection,
        animationSpec = tween(durationMillis = 200),
        label = "DirectionAnimation"
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2f

        // Rotazione negativa per allineare correttamente con la direzione Nord
        rotate(-animatedDirection) {
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