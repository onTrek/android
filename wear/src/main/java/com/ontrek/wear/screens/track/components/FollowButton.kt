package com.ontrek.wear.screens.track.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.ontrek.wear.utils.functions.getContrastingTextColor

@Composable
fun FollowButton(
    username: String,
    userColor: Color,
    sweepAngle: Float = 0f,
    stopFollow: () -> Unit,
) {

    var showDialog by remember { mutableStateOf(false) }

    val widthFactor = remember(sweepAngle) {
        val normalizedAngle = (sweepAngle / 360f).coerceIn(0f, 1f)
        0.4f + (normalizedAngle * 0.7f)
    }


    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        val initialHeight = 0.14f

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxHeight(initialHeight)
                .fillMaxWidth(widthFactor)
                .clip(
                    RoundedCornerShape(
                        topStart = 100.dp, topEnd = 100.dp, bottomStart = 0.dp, bottomEnd = 0.dp
                    )
                )
                .background(userColor)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.75f)
                    .padding(top = 2.dp)
            ) {
                Text(
                    text = username,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontStyle = FontStyle.Italic,
                    style = MaterialTheme.typography.bodyExtraSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    color = getContrastingTextColor(userColor),
                )
                IconButton(
                    onClick = { showDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = getContrastingTextColor(userColor),
                    )
                }
            }
        }
    }

    AlertDialog(
        visible = showDialog,
        onDismissRequest = { showDialog = false },
        icon = {
            Icon(
                imageVector = Icons.Outlined.Warning,
                contentDescription = "Off Track",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 30.dp)
            )
        },
        title = {
            Text(
                text = "Do you want to stop following $username?",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    stopFollow()
                    showDialog = false
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                modifier = Modifier.padding(start = 4.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Confirm",
                )
            }
        },
        dismissButton = {
            Button(
                onClick = { showDialog = false },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(end = 4.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Snooze",
                )
            }
        }
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun FollowButtonPreview() {
    FollowButton(
        username = "Gioele Maria Zoccoli",
        userColor = Color(0xFF6200EE),
        sweepAngle = 60f,
        stopFollow = {}
    )
}
