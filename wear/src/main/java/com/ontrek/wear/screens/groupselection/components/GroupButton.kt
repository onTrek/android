package com.ontrek.wear.screens.groupselection.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.outlined.DownloadForOffline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
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
import com.ontrek.wear.screens.groupselection.DownloadState
import com.ontrek.wear.screens.groupselection.GroupUI
import com.ontrek.wear.utils.components.Loading

@Composable
fun GroupButton(
    group: GroupUI,
    downloadState: DownloadState,
    downloadTrack: () -> Unit,
    deleteTrack: () -> Unit,
    navigateToTrack: (trackID: Int, trackName: String, sessionID: Int) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    var downloadedNow by remember { mutableStateOf(false) }
    val hasTrack = group.track.id != -1

    Button(
        onClick = {
            if (downloadState == DownloadState.Completed && hasTrack) {
                navigateToTrack(group.track.id, group.track.title, group.group_id)
            } else {
                showDialog = true
            }
        },
        onLongClick = {
            showDialog = true
        },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
    ) {
        Column(modifier = Modifier.padding(vertical = 6.dp)) {
            Text(
                text = group.description,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when {
                        !hasTrack -> Icons.Default.Block
                        downloadState == DownloadState.Completed -> Icons.Default.OfflinePin
                        else -> Icons.Outlined.DownloadForOffline
                    }, contentDescription = "Track", tint = when {
                        !hasTrack -> MaterialTheme.colorScheme.errorDim
                        downloadState == DownloadState.Completed -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outline
                    }, modifier = Modifier
                        .size(16.dp)
                        .padding(end = 4.dp)
                )
                Text(
                    text = if (hasTrack) group.track.title else "No track associated",
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = when {
                        !hasTrack -> MaterialTheme.colorScheme.errorDim
                        downloadState == DownloadState.Completed -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.outline
                    },
                )
            }
        }
    }

    if (hasTrack && (downloadState == DownloadState.NotStarted || downloadState == DownloadState.InProgress || downloadedNow)) {
        TrackDownloadDialog(
            groupTitle = group.description,
            trackTitle = group.track.title,
            downloadState = downloadState,
            visible = showDialog,
            onDismiss = { showDialog = false },
            onConfirm = {
                if (downloadState == DownloadState.NotStarted) {
                    downloadTrack()
                    downloadedNow = true
                } else {
                    navigateToTrack(group.track.id, group.track.title, group.group_id)
                }
            })
    } else if (hasTrack && downloadState == DownloadState.Completed) {
        TrackDeleteDialog(
            groupTitle = group.description,
            trackTitle = group.track.title,
            downloadState = downloadState,
            visible = showDialog,
            onDismiss = { showDialog = false },
            onConfirm = {
                deleteTrack()
                showDialog = false
            })
    } else {
        EmptyTrackDialog(
            groupTitle = group.description,
            visible = showDialog,
            onDismiss = { showDialog = false })
    }
}

@Composable
fun TrackDownloadDialog(
    groupTitle: String,
    trackTitle: String,
    downloadState: DownloadState,
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
                text = "You need to download the track '$trackTitle' before joining the group '$groupTitle'.",
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
                enabled = downloadState != DownloadState.InProgress,
                modifier = Modifier.padding(start = 4.dp),
            ) {
                when (downloadState) {
                    DownloadState.InProgress -> {
                        Loading()
                    }

                    is DownloadState.NotStarted -> {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download",
                        )
                    }

                    else -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Confirm",
                        )
                    }
                }
            }
        },
    )
}

@Composable
fun TrackDeleteDialog(
    groupTitle: String,
    trackTitle: String,
    downloadState: DownloadState,
    visible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        visible = visible,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Track?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
        },
        icon = {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete Track",
                tint = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete the track '$trackTitle' associated with the group '$groupTitle'?",
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
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                enabled = downloadState != DownloadState.InProgress,
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
    groupTitle: String, visible: Boolean, onDismiss: () -> Unit
) {
    AlertDialog(visible = visible, onDismissRequest = onDismiss, title = {
        Text(
            text = groupTitle,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }, icon = {
        Icon(
            imageVector = Icons.Filled.Groups,
            contentDescription = "Group",
            tint = MaterialTheme.colorScheme.primary
        )
    }, text = {
        Text(
            text = "No track associated with this group. Use the smartphone app to add it.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }, edgeButton = {
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
    })
}
