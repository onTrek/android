package com.ontrek.wear.screens.track.components

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material3.AlertDialog

@Composable
fun FallDialog(
    openDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            // Title can be added here if needed
        },
        text = {
            // Text content can be added here if needed
            CircularProgressIndicator(1f)
        },
        confirmButton = {
            // Confirm button can be added here if needed
        },
        dismissButton = {
            // Dismiss button can be added here if needed
        },
        visible = openDialog
    )
}