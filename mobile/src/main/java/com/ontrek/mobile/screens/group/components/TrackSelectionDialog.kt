package com.ontrek.mobile.screens.group.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ontrek.mobile.screens.group.GroupsViewModel
import com.ontrek.mobile.utils.components.EmptyComponent
import com.ontrek.mobile.utils.components.ErrorComponent
import com.ontrek.shared.data.Track

@Composable
fun TrackSelectionDialog(
    tracks: GroupsViewModel.TrackState,
    loadTracks: () -> Unit,
    oldTrack: Int?,
    onDismiss: () -> Unit,
    onTrackSelected: (Track) -> Unit
) {
    var selectedTrack by remember { mutableStateOf<Track?>(null) }

    LaunchedEffect(Unit) {
        loadTracks()
    }

    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Select a track",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp)
                )

                HorizontalDivider(
                    Modifier,
                    DividerDefaults.Thickness,
                    DividerDefaults.color
                )

                if (tracks is GroupsViewModel.TrackState.Loading) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                } else if (tracks is GroupsViewModel.TrackState.Error) {
                    ErrorComponent(tracks.message)
                } else {
                    val tracks = (tracks as GroupsViewModel.TrackState.Success).tracks
                    if (tracks.isEmpty()) {
                        EmptyComponent(
                            fillMaxSize = false,
                            icon = Icons.Default.Route,
                            title = "No tracks available",
                            description = "Please add a track to continue."
                        )
                    } else {
                        LazyColumn(
                            Modifier
                                .selectableGroup()
                                .heightIn(max = 265.dp)
                        ) {
                            items(tracks) { track ->
                                val isSelected = if (selectedTrack == null) {
                                    track.id == oldTrack
                                } else {
                                    track == selectedTrack
                                }
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .selectable(
                                            selected = (isSelected),
                                            onClick = {
                                                selectedTrack =
                                                    if (track != selectedTrack) track else null
                                            },
                                            role = Role.RadioButton
                                        )
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = (isSelected),
                                        onClick = null // null recommended for accessibility with screenreaders
                                    )
                                    Text(
                                        text = track.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(start = 16.dp)
                                    )
                                }
                            }
                        }
                    }

                }

                HorizontalDivider(
                    Modifier,
                    DividerDefaults.Thickness,
                    DividerDefaults.color
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .padding(8.dp)
                    ) {
                        Text("Cancel")
                    }

                    TextButton(
                        enabled = selectedTrack != null,
                        onClick = { onTrackSelected(selectedTrack!!) },
                        modifier = Modifier
                            .padding(8.dp)
                    ) {
                        Text("Select")
                    }
                }
            }
        }
    }
}