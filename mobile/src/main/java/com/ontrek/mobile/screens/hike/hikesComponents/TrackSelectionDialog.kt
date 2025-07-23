package com.ontrek.mobile.screens.hike.hikesComponents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ontrek.shared.data.Track

@Composable
fun TrackSelectionDialog(
    tracks: List<Track>,
    oldTrack: Int = 0,
    onDismiss: () -> Unit,
    onTrackSelected: (Track) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select a new Track") },
        text = {
            if (tracks.isEmpty()) {
                Text("Not found tracks.")
            } else {
                LazyColumn(
                    modifier = Modifier.height(300.dp)
                ) {
                    items(tracks) { track ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    if (track.id != oldTrack) {
                                        onTrackSelected(track)
                                        onDismiss()
                                    }},
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terrain,
                                contentDescription = "Track",
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = track.title,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            if (oldTrack == track.id) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Current Track",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        HorizontalDivider(
                            Modifier,
                            DividerDefaults.Thickness,
                            DividerDefaults.color
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}