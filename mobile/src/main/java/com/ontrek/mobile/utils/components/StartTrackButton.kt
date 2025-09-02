package com.ontrek.mobile.utils.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun StartTrackButton(
    trackName: String,
    trackId: Int,
    sessionId: Int? = null,
    sendStartHikeMessage: (trackId: Int, sessionId: Int?, trackName: String) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }

    IconButton(
        onClick = { showDialog = true }
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Start Track",
            tint = MaterialTheme.colorScheme.primary
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Start Hike") },
            text = { Text("Do you want to start this hike on your smartwatch?\nIt must be connected to this device.") },
            confirmButton = {
                Button(
                    onClick = {
                        sendStartHikeMessage(trackId, sessionId, trackName)
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Start", color = MaterialTheme.colorScheme.onPrimary)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDialog = false }) {
                    Text("Close", color = MaterialTheme.colorScheme.outline)
                }
            }
        )
    }
}