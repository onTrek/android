package com.ontrek.wear.screens.track.components

import android.location.Location
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.unit.dp
import com.ontrek.shared.data.MemberInfo
import com.ontrek.wear.utils.functions.computeDistanceAndBearing
import com.ontrek.wear.utils.functions.polarToCartesian
import kotlin.math.min
import androidx.core.graphics.toColorInt
import com.ontrek.wear.utils.functions.PolarResult

val distances = listOf(
    0.33f to "10m",
    0.66f to "50m",
    0.99f to "100+m"
)

@Composable
fun FriendRadar(
    direction: Float,
    userLocation: Location,
    members: List<MemberInfo>,
    modifier: Modifier = Modifier,
    maxDistanceMeters: Float = 100f
) {
    val memberPositions = remember { mutableStateMapOf<String, PolarResult>() }

    BoxWithConstraints(modifier = modifier.fillMaxSize().padding(12.dp)) {
        val centerX = constraints.maxWidth / 2f
        val centerY = constraints.maxHeight / 2f
        val maxRadiusPx = min(centerX, centerY) - 12f

        // Radar + etichette
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Cerchi concentrici
            distances.forEach { (distance, _) ->
                val radiusPx = distance * maxRadiusPx

                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color.Gray.copy(alpha = 0.2f),
                        radius = radiusPx,
                        center = Offset(centerX, centerY),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                    )
                }
            }

            // Etichette curve (una sola volta per etichetta)
            distances.forEach { (distance, label) ->
                val radiusPx = distance * maxRadiusPx

                CurvedTextOnCircle(
                    text = label,
                    radius = radiusPx + 2f,
                    color = Color.Gray.copy(alpha = 0.5f),
                    textSize = 12f
                )
            }
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            members.forEach { member ->
                val (distance, bearingToMember) = computeDistanceAndBearing(
                    userLocation.latitude, userLocation.longitude,
                    member.latitude, member.longitude
                )


                val relativeBearing = (bearingToMember - direction + 360) % 360 - 180
                val polarResult = polarToCartesian(
                    centerX, centerY,
                    distance,
                    relativeBearing,
                    maxDistanceMeters,
                    maxRadiusPx
                )

                Log.d("FriendRadar", "Member: ${member.user.username}, Distance: $distance, Bearing: $bearingToMember, Relative Bearing: $relativeBearing")

                val offset = polarResult.offset
                val isCapped = polarResult.isCapped

                memberPositions[member.user.id] = polarResult

                drawCircle(
                    color = member.user.color.toColorInt().let { Color(it) },
                    radius = 12f,
                    center = offset,
                    style = if (isCapped) Stroke(width = 2f) else Fill
                )

            }
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