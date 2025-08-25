package com.ontrek.wear.screens.track.components

import android.location.Location
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Sos
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.wear.compose.material3.MaterialTheme
import com.ontrek.shared.data.MemberInfo
import com.ontrek.wear.utils.functions.computeDistanceAndBearing
import com.ontrek.wear.utils.functions.polarToCartesian
import kotlin.math.min
import com.ontrek.wear.utils.functions.PolarResult
import kotlin.collections.first
import kotlin.math.*
import kotlin.math.roundToInt

val distances = listOf(
    0.33f to "50m",
    0.64f to "250m",
    0.95f to "1000+m"
)

data class MemberCluster(
    val members: List<MemberInfo>,
    val center: Offset
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
        val memberDrawData =
            remember(members, userLocation, direction, maxRadiusPx, maxDistanceMeters) {
                members.map { member ->
                    val (distance, bearingToMember) = computeDistanceAndBearing(
                        userLocation.latitude, userLocation.longitude,
                        member.latitude, member.longitude
                    )

                    val relativeBearing =
                        (bearingToMember - direction + 360) % 360f // Convert to relative bearing

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

        val clusters = clusterMembers(memberDrawData, minDistancePx = 16f)

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

        // Membri
        clusters.forEach { cluster ->
            val clusterSize = cluster.members.size
            if (clusterSize == 1) {
                val member = cluster.members.first()
                val (_, distance, polarResult) = memberDrawData.first { it.first == member }

                MemberCluster(
                    distance = distance,
                    polarResult = polarResult,
                    member = member,
                    density = density
                )
            } else {
                val angleStep = (360f / clusterSize)

                cluster.members.forEachIndexed { index, member ->
                    val (_, distance, polarResult)  = memberDrawData.first { it.first == member }
                    MemberCluster(
                        distance = distance,
                        polarResult = polarResult,
                        member = member,
                        density = density,
                        angleRad = index * angleStep,
                        center = cluster.center
                    )
                }
            }
        }
    }
}

fun clusterMembers(
    memberDrawData: List<Triple<MemberInfo, Float, PolarResult>>,
    minDistancePx: Float
): List<MemberCluster> {
    val clusters = mutableListOf<MemberCluster>()
    val visited = mutableSetOf<MemberInfo>()

    memberDrawData.forEach { (member, _, polarResult) ->
        if (member in visited) return@forEach

        val closeMembers = memberDrawData.filter { (other, _, otherPolar) ->
            val distance = (polarResult.offset - otherPolar.offset).getDistance()
            distance <= minDistancePx
        }

        val membersInCluster = closeMembers.map { it.first }
        visited.addAll(membersInCluster)

        // calcola il centroide del cluster
        val avgX = closeMembers.map { it.third.offset.x }.average().toFloat()
        val avgY = closeMembers.map { it.third.offset.y }.average().toFloat()
        val clusterCenter = Offset(avgX, avgY)

        clusters.add(MemberCluster(membersInCluster, clusterCenter))
    }

    return clusters
}


@Composable
fun MemberCluster(
    distance: Float,
    polarResult: PolarResult,
    member: MemberInfo,
    density: Density,
    angleRad: Float = 0f,
    center: Offset = Offset(0f, 0f)
) {
    // Canvas: cerchio membro
    Canvas(modifier = Modifier.fillMaxSize()) {
        if (angleRad == 0f) {

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

        } else {

            val radiusAround = when {
                distance <= 50 -> 12f
                distance <= 250 -> 10f
                else -> 8f
            }

            val offsetX = (cos(angleRad) * radiusAround)
            val offsetY = (sin(angleRad) * radiusAround)

            val radiusDp = when {
                distance <= 50 -> 10.dp
                distance <= 250 -> 8.dp
                else -> 6.dp
            }
            val radiusPx = with(density) { radiusDp.toPx() }

            drawCircle(
                color = member.user.color.toColorInt().let { Color(it) },
                radius = radiusPx,
                center = center + Offset(offsetX, offsetY),
                style = if (polarResult.isCapped) Stroke(width = 2f) else Fill
            )
        }
    }

    val icon = when {
        member.help_request -> Icons.Default.Sos
        System.currentTimeMillis() - java.time.OffsetDateTime.parse(member.time_stamp)
            .toInstant().toEpochMilli() > 90000L -> Icons.Default.CloudOff
        member.going_to.isNotBlank() -> Icons.Default.PersonSearch
        else -> Icons.Default.Person
    }

    val iconSizeDp = when {
        distance <= 50 -> 14.dp
        distance <= 250 -> 12.dp
        else -> 10.dp
    }

    val iconHalfPx = with(density) { iconSizeDp.toPx() / 2f }

    if (angleRad == 0f) {
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
    } else {
        val radiusAround = when {
            distance <= 50 -> 12f
            distance <= 250 -> 10f
            else -> 8f
        }

        val offsetX = (cos(angleRad) * radiusAround)
        val offsetY = (sin(angleRad) * radiusAround)

        val centerX = center.x + offsetX
        val centerY = center.y + offsetY

        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(iconSizeDp)
                .offset {
                    IntOffset(
                        (centerX - iconHalfPx).roundToInt(),
                        (centerY - iconHalfPx).roundToInt()
                    )
                },
            tint = if (polarResult.isCapped) member.user.color.toColorInt().let { Color(it) } else MaterialTheme.colorScheme.surfaceContainer
        )
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
