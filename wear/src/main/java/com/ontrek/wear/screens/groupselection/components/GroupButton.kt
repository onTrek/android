package com.ontrek.wear.screens.groupselection.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.ontrek.wear.screens.groupselection.GroupUI

@Composable
fun GroupButton(
    group: GroupUI,
    navigateToTrack: (trackID: Int, trackName: String, sessionID: Int) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    val hasTrack = group.track.id != -1

    Button(
        onClick = {
            showDialog = true
        },
        modifier = Modifier
            .fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
    ) {
        Text(
            text = group.description,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }

    if (hasTrack) {
        GroupDialog(
            groupTitle = group.description,
            trackTitle = group.track.title,
            visible = showDialog,
            onDismiss = { showDialog = false },
            onConfirm = {
                // TODO: Implement the check if the track is already downloaded and download it if not
                navigateToTrack(group.track.id, group.track.title, group.group_id)
            }
        )
    } else {
        EmptyTrackDialog(
            groupTitle = group.description,
            visible = showDialog,
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun GroupDialog(
    groupTitle: String,
    trackTitle: String,
    visible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        visible = visible,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = groupTitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        },
        icon = {
            Icon(
                imageVector = Icons.Filled.Groups,
                contentDescription = "Group",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Text(
                text = "You are about to join the group '$groupTitle' for the hike '$trackTitle'.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(end = 4.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.padding(start = 4.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Confirm",
                )
            }
        },
    )
}

@Composable
fun EmptyTrackDialog(
    groupTitle: String,
    visible: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        visible = visible,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = groupTitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        },
        icon = {
            Icon(
                imageVector = Icons.Filled.Groups,
                contentDescription = "Group",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Text(
                text = "No track associated with this group. Use the smartphone app to add it.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        edgeButton = {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.padding(vertical = 8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    )
}
