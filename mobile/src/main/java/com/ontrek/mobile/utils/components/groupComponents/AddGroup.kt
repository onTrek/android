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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Crea Nuovo Gruppo",
                style = MaterialTheme.typography.titleLarge
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descrizione del gruppo") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
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
                        contentDescription = "Traccia",
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = selectedTrack?.title ?: "Seleziona una traccia",
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Bottoni azioni
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text("Annulla")
                }

                Spacer(modifier = Modifier.width(8.dp))

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
                        Text("Crea")
                    }
                }
            }

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
    }
}