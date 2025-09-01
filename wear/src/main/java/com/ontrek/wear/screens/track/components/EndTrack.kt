package com.ontrek.wear.screens.track.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme

@Composable
fun EndTrack(
    visible: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    trackName: String
) {
    AlertDialog(
        visible = visible,
        icon = {
            Icon(
                imageVector = Icons.Filled.SportsScore,
                contentDescription = "Track Complete",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Completed!",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Text(
                text = "You completed '$trackName' track.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.padding(start = 4.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Confirm",
                )
            }
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
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Dismiss",
                )
            }
        },
        onDismissRequest = onDismiss,
    ) {}
}