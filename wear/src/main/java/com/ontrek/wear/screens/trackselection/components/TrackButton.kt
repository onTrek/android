package com.ontrek.wear.screens.trackselection.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.outlined.DownloadForOffline
import androidx.compose.material.icons.outlined.Route
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.ontrek.wear.screens.Screen
import com.ontrek.wear.screens.trackselection.DownloadState
import com.ontrek.wear.screens.trackselection.TrackUI
import com.ontrek.wear.utils.components.Loading

@Composable
fun TrackButton(
    modifier: Modifier = Modifier,
    track: TrackUI,
    index: Int,
    navController: NavHostController,
    onDownloadClick: (Int, Int, Context) -> Unit,
    deleteTrack: (Int, Context) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Button(
        onClick = {
            if (track.state is DownloadState.NotStarted) {
                onDownloadClick(index, track.id, context)
            } else if (track.state is DownloadState.Completed) {
                navController.navigate(route = Screen.TrackScreen.route + "?trackID=${track.id}&trackName=${track.title}")
            }
        },
        onLongClick = {
            if (track.state is DownloadState.Completed) {
                showDialog = true
            } else {
                Toast.makeText(
                    context,
                    "Track size: ${track.getFormattedSize()}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        enabled = track.state is DownloadState.Completed || track.state is DownloadState.NotStarted,
        modifier = modifier,
    ) {
        when (track.state) {
            is DownloadState.InProgress -> {
                Loading(Modifier.fillMaxWidth())
            }

            is DownloadState.NotStarted -> {
                Icon(
                    imageVector = Icons.Outlined.DownloadForOffline,
                    contentDescription = "Download track",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(0.15f)
                )
                Text(
                    text = track.title,
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Left,
                    modifier = Modifier
                        .weight(0.9f)
                        .padding(8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            is DownloadState.Completed -> {
                Icon(
                    imageVector = Icons.Default.OfflinePin,
                    contentDescription = "Download track",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(0.15f)
                )
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Left,
                    modifier = Modifier
                        .weight(0.9f)
                        .padding(8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
    DeleteTrackDialog(
        track = track,
        showDialog = showDialog,
        onConfirm = {
            deleteTrack(index, context)
            showDialog = false
        },
        onDismiss = { showDialog = false }
    )
}

@Composable
fun DeleteTrackDialog(
    track: TrackUI,
    showDialog: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(visible = showDialog, onDismissRequest = onDismiss, icon = {
        Icon(
            imageVector = Icons.Outlined.Route,
            contentDescription = "Delete Track",
            tint = MaterialTheme.colorScheme.error,
        )
    }, title = {
        Text(
            text = "Delete Track?",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.titleMedium,
        )
    }, confirmButton = {
        Button(
            onClick = onConfirm,
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
    }, dismissButton = {
        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.padding(end = 4.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Confirm",
            )
        }
    }) {
        item {
            Text(
                text = track.title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(8.dp),
            )
        }
        item {
            Text(
                text = "${track.getFormattedSize()} of storage will be freed. You can download it later if needed.",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}
