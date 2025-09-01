package com.ontrek.wear.screens.track.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text


@Composable
fun OffTrackDialog(
    showDialog: Boolean,
    onConfirm: () -> Unit,
    onSnooze: () -> Unit
) {
    AlertDialog(
        visible = showDialog,
        onDismissRequest = onConfirm,
        icon = {
            Icon(
                imageVector = Icons.Outlined.Warning,
                contentDescription = "Off Track",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        },
        title = {
            Text(
                text = "You are getting off track!",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
                onClick = onSnooze,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(end = 4.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Snooze,
                    contentDescription = "Snooze",
                )
            }
        }
    )
}
