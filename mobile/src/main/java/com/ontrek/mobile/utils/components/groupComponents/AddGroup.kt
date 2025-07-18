package com.ontrek.mobile.utils.components.groupComponents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ontrek.shared.data.Track

@Composable
fun AddGroup(
    tracks: List<Track>,
    isLoading: Boolean = false,
    onCreateGroup: (description: String, trackId: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var description by remember { mutableStateOf("") }
    var selectedTrack by remember { mutableStateOf<Track?>(null) }
    var showTrackSelection by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Create new group",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { if (it.length <= 100) description = it },
                    label = { Text("Descrizione") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    supportingText = {
                        Text("${description.length}/100 caratteri")
                    }
                )

                // Selezione traccia
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTrackSelection = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Route,
                            contentDescription = "Track",
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = selectedTrack?.title ?: "Select a track",
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedTrack?.let { track ->
                        onCreateGroup(description, track.id)
                    }
                },
                enabled = description.isNotBlank() && selectedTrack != null && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Create")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Close")
            }
        }
    )

    if (showTrackSelection) {
        TrackSelectionDialog(
            tracks = tracks,
            onDismiss = { showTrackSelection = false },
            onTrackSelected = { track ->
                selectedTrack = track
                showTrackSelection = false
            }
        )
    }
}