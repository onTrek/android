package com.ontrek.wear.screens.track.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.ontrek.wear.utils.functions.getReadableDistance


@Composable
fun DistantFromTrackDialog(
    showDialog: Boolean,
    onConfirm: () -> Unit,
    onClose: () -> Unit,
    metersAway: Int
) {
    AlertDialog(
        visible = showDialog,
        onDismissRequest = onConfirm,
        title = {
            Text(
                text = "You are far from the track",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Text(
                text = "You are ${getReadableDistance(metersAway.toDouble())} meters away from the track.\nDo you want to continue?",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
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
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(end = 4.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close",
                )
            }
        }
    )
}
