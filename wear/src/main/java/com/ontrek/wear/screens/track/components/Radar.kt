package com.ontrek.wear.screens.track.components

import android.location.Location
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DoubleArrow
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Sos
import androidx.compose.material3.Icon
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.ontrek.shared.data.MemberInfo
import com.ontrek.wear.utils.functions.computeDistanceAndBearing
import com.ontrek.wear.utils.functions.polarToCartesian
import kotlin.math.min
import androidx.core.graphics.toColorInt
import androidx.wear.compose.material3.MaterialTheme
import kotlin.math.roundToInt

val distances = listOf(
    0.33f to "50m",
    0.66f to "250m",
    0.96f to "1000+m"
)

@Composable
fun FriendRadar(
    direction: Float,
    userLocation: Location,
    members: List<MemberInfo>,
    modifier: Modifier = Modifier,
    maxDistanceMeters: Float = 1000f,
    radarColor: Color = Color.Gray.copy(alpha = 0.2f),
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val centerX = constraints.maxWidth / 2f
        val centerY = constraints.maxHeight / 2f
        val maxRadiusPx = min(centerX, centerY) - 12f
        val density = LocalDensity.current

        // Calcola le posizioni una volta sola e salva anche la distanza
        val memberDrawData = remember(members, userLocation, direction, maxRadiusPx, maxDistanceMeters) {
            members.map { member ->
                val (distance, bearingToMember) = computeDistanceAndBearing(
                    userLocation.latitude, userLocation.longitude,
                    member.latitude, member.longitude
                )

                val relativeBearing = (bearingToMember - direction + 360) % 360f // Convert to relative bearing

                val polarResult = polarToCartesian(
                    centerX, centerY,
                    distance,
                    relativeBearing,
                    maxDistanceMeters,
                    maxRadiusPx
                )

                Triple(member, distance, polarResult)
            }
        }

        // Radar + etichette
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            distances.forEach { (distance, _) ->
                val radiusPx = distance * maxRadiusPx
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = radarColor,
                        radius = radiusPx,
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 2f)
                    )
                }
            }

            distances.forEach { (distance, label) ->
                val radiusPx = distance * maxRadiusPx
                CurvedTextOnCircle(
                    text = label,
                    radius = radiusPx + 2f,
                    color = radarColor,
                    textSize = 12f
                )
            }
        }

        // Canvas: cerchi membri con dimensione variabile
        Canvas(modifier = Modifier.fillMaxSize()) {
            memberDrawData.forEach { (member, distance, polarResult) ->
                val radiusDp = when {
                    distance <= 50 -> 10.dp
                    distance <= 250 -> 8.dp
                    else -> 6.dp
                }
                val radiusPx = with(density) { radiusDp.toPx() }

                drawCircle(
                    color = member.user.color.toColorInt().let { Color(it) },
                    radius = radiusPx,
                    center = polarResult.offset,
                    style = if (polarResult.isCapped) Stroke(width = 2f) else Fill
                )
            }
        }

        // Icone membri con dimensione variabile
        memberDrawData.forEach { (member, distance, polarResult) ->
            val icon = when {
                member.help_request -> Icons.Default.Sos
                member.going_to.isNotBlank() -> Icons.Default.PersonSearch
                else -> Icons.Default.Person
            }

            val iconSizeDp = when {
                distance <= 50 -> 14.dp
                distance <= 250 -> 12.dp
                else -> 10.dp
            }

            val iconHalfPx = with(density) { iconSizeDp.toPx() / 2f }

            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(iconSizeDp)
                    .offset {
                        IntOffset(
                            (polarResult.offset.x - iconHalfPx).roundToInt(),
                            (polarResult.offset.y - iconHalfPx).roundToInt()
                        )
                    },
                tint = if (polarResult.isCapped) member.user.color.toColorInt().let { Color(it) } else MaterialTheme.colorScheme.surfaceContainer
            )
        }
    }

}



@Composable
fun CurvedTextOnCircle(
    text: String,
    radius: Float,
    color: Color = Color.Gray,
    textSize: Float = 28f
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawIntoCanvas { canvas ->
            val paint = android.graphics.Paint().apply {
                this.color = color.toArgb()
                this.textSize = textSize
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.CENTER
            }

            val path = android.graphics.Path().apply {
                addCircle(
                    size.width / 2,
                    size.height / 2,
                    radius,
                    android.graphics.Path.Direction.CW
                )
            }

            canvas.nativeCanvas.drawTextOnPath(text, path, 0f, 0f, paint)
        }
    }
}