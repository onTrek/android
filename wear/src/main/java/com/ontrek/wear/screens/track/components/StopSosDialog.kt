package com.ontrek.wear.screens.track.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.ontrek.wear.R

@Composable
fun StopSosDialog(
    username: String,
    end: Boolean = false,
    visible: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        visible = visible,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (end) "You reached $username" else "$username no longer needs help",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.person_check),
                contentDescription = "Group",
                tint = MaterialTheme.colorScheme.primary
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