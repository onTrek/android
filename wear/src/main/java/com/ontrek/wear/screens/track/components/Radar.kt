package com.ontrek.wear.screens.track.components

import android.graphics.Paint
import android.graphics.Path
import android.location.Location
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Sos
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.ontrek.wear.utils.functions.PolarResult
import com.ontrek.wear.utils.functions.shouldUpdateDirection
import java.time.OffsetDateTime
import kotlin.math.*

val distances = listOf(
    0.33f to "50m",
    0.66f to "250m",
    0.98f to "1000+m"
)

data class MemberCluster(
    val members: List<MemberInfo>,
    val center: Offset
)

@Composable
fun FriendRadar(
    newDirection: Float,
    oldDirection: Float,
    userLocation: Location,
    members: List<MemberInfo>,
    modifier: Modifier = Modifier,
    maxDistanceMeters: Float = 1000f,
    radarColor: Color = Color.Gray.copy(alpha = 0.2f),
    onUserClick: (String) -> Unit = {}
) {
    var heading by rememberSaveable { mutableFloatStateOf(oldDirection) }
    LaunchedEffect(newDirection, oldDirection) {
        if (shouldUpdateDirection(newDirection.toDouble(), heading.toDouble())) {
            heading = newDirection
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val density = LocalDensity.current

        val centerX = constraints.maxWidth / 2f
        val centerY = constraints.maxHeight / 2f
        val maxRadiusPx = min(centerX, centerY) - with(density) { 6.dp.toPx() }

        var memberDrawData by remember {
            mutableStateOf<List<Triple<MemberInfo, Float, PolarResult>>>(emptyList())
        }
        var clusters by remember { mutableStateOf<List<MemberCluster>>(emptyList()) }

        LaunchedEffect(
            heading,
            userLocation.latitude, userLocation.longitude,
            members,
            maxRadiusPx, maxDistanceMeters
        ) {
            memberDrawData = members.map { member ->
                val (distance, bearingToMember) = computeDistanceAndBearing(
                    userLocation.latitude, userLocation.longitude,
                    member.latitude, member.longitude
                )

                val relativeBearing = (bearingToMember - heading + 360f) % 360f

                val polarResult = polarToCartesian(
                    centerX, centerY,
                    distance,
                    relativeBearing,
                    maxDistanceMeters,
                    maxRadiusPx
                )

                Triple(member, distance, polarResult)
            }

            clusters = clusterMembers(memberDrawData, minDistancePx = 16f)
        }

        // 5) Radar + etichette
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

        // 6) Disegno dei membri (rispetta i cluster)
        clusters.forEach { cluster ->
            val clusterSize = cluster.members.size

            val clusterInfo: List<Pair<String, Boolean>> =
                cluster.members.map { member -> member.user.id to member.help_request }

            if (clusterSize == 1) {
                val member = cluster.members.first()
                val (_, distance, polarResult) = memberDrawData.first { it.first == member }

                MemberMarker(
                    distance = distance,
                    polarResult = polarResult,
                    member = member,
                    density = density,
                    onUserClick = onUserClick
                )
            } else {
                val angleStepDeg = 360f / clusterSize
                cluster.members.forEachIndexed { index, member ->
                    val (_, distance, polarResult) = memberDrawData.first { it.first == member }
                    val angleRad = Math.toRadians((index * angleStepDeg).toDouble()).toFloat()

                    MemberMarker(
                        distance = distance,
                        polarResult = polarResult,
                        member = member,
                        density = density,
                        angleRad = angleRad,
                        center = cluster.center,
                        cluster = clusterInfo,
                        onUserClick = onUserClick
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

        val closeMembers = memberDrawData.filter { (_, _, otherPolar) ->
            val d = (polarResult.offset - otherPolar.offset).getDistance()
            d <= minDistancePx
        }

        val membersInCluster = closeMembers.map { it.first }
        visited.addAll(membersInCluster)

        // centroide
        val avgX = closeMembers.map { it.third.offset.x }.average().toFloat()
        val avgY = closeMembers.map { it.third.offset.y }.average().toFloat()
        val clusterCenter = Offset(avgX, avgY)

        clusters.add(MemberCluster(membersInCluster, clusterCenter))
    }

    return clusters
}

@Composable
fun MemberMarker( // rinominato per evitare ambiguità col data class
    distance: Float,
    polarResult: PolarResult,
    member: MemberInfo,
    density: Density,
    angleRad: Float = 0f,
    center: Offset = Offset(0f, 0f),
    onUserClick: (String) -> Unit = {},
    cluster: List<Pair<String, Boolean>> = emptyList()
) {
    val animatedX by animateFloatAsState(
        targetValue = polarResult.offset.x,
        animationSpec = tween(durationMillis = 200),
        label = "PolarAnimation"
    )

    val animatedY by animateFloatAsState(
        targetValue = polarResult.offset.y,
        animationSpec = tween(durationMillis = 200),
        label = "PolarAnimation"
    )

    val animatedOffset = Offset(animatedX, animatedY)

    // Cerchio membro
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
                center = animatedOffset,
                style = if (polarResult.isCapped) Stroke(width = 2f) else Fill
            )
        } else {
            val radiusAround = when {
                distance <= 50 -> 12f
                distance <= 250 -> 10f
                else -> 8f
            }
            val offsetX = cos(angleRad) * radiusAround
            val offsetY = sin(angleRad) * radiusAround

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
        System.currentTimeMillis() - OffsetDateTime.parse(member.time_stamp)
            .toInstant().toEpochMilli() > 90_000L -> Icons.Default.CloudOff
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
        Box(
            modifier = Modifier
                .size(iconSizeDp) // dimensione cerchio
                .offset {
                    IntOffset(
                        (animatedOffset.x - iconHalfPx).roundToInt(),
                        (animatedOffset.y - iconHalfPx).roundToInt()
                    )
                }
                .clip(CircleShape)
                .clickable(enabled = member.help_request) {
                    onUserClick(member.user.id) },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (polarResult.isCapped)
                    member.user.color.toColorInt().let { Color(it) }
                else MaterialTheme.colorScheme.surfaceContainer
            )
        }
    } else {
        val radiusAround = when {
            distance <= 50 -> 12f
            distance <= 250 -> 10f
            else -> 8f
        }
        val offsetX = cos(angleRad) * radiusAround
        val offsetY = sin(angleRad) * radiusAround
        val centerX = center.x + offsetX
        val centerY = center.y + offsetY

        Box(
            modifier = Modifier
                .size(iconSizeDp)
                .offset {
                    IntOffset(
                        (center.x - iconHalfPx).roundToInt(),
                        (center.y - iconHalfPx).roundToInt()
                    )
                }
                .clip(CircleShape)
                .clickable(enabled = cluster.any { it.second }) {
                    for ((id, value) in cluster) {
                        if (value) onUserClick(id)
                }
            },
        )

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
            tint = if (polarResult.isCapped)
                member.user.color.toColorInt().let { Color(it) }
            else MaterialTheme.colorScheme.surfaceContainer
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
            val paint = Paint().apply {
                this.color = color.toArgb()
                this.textSize = textSize
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            val path = Path().apply {
                addCircle(
                    size.width / 2,
                    size.height / 2,
                    radius,
                    Path.Direction.CW
                )
            }
            canvas.nativeCanvas.drawTextOnPath(text, path, 0f, 0f, paint)
        }
    }
}
