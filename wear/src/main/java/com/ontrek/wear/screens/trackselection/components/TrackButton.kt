package com.ontrek.wear.screens.trackselection.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import java.io.File

@Composable
fun TrackButton(
    modifier: Modifier = Modifier,
    trackName: String,
    trackID: Int,
    index: Int,
    resetDownloadState: (Int) -> Unit,
    navController: NavHostController,
) {
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Button(
        onClick = { navController.navigate(route = Screen.TrackScreen.route + "?trackID=${trackID}") },
        onLongClick = { showDialog = true },
        modifier = modifier
    ) {
        Text(
            text = trackName,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Left,
            modifier = Modifier
                .weight(0.85f)
                .padding(8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        DeleteTrackDialog(
            trackName = trackName,
            showDialog = showDialog,
            onConfirm = {
                File(context.filesDir, "${trackID}.gpx").delete()
                resetDownloadState(index)

                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun DeleteTrackDialog(
    trackName: String, showDialog: Boolean, onConfirm: () -> Unit, onDismiss: () -> Unit
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
                text = "Are you sure you want to delete the track '$trackName'? You can re-download it later if needed.",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}
