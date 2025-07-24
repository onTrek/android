package com.ontrek.mobile.screens.group.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.ontrek.mobile.screens.group.GroupsViewModel
import com.ontrek.shared.data.Track


@Composable
fun AddGroupButton(
    tracks: GroupsViewModel.TrackState,
    loadTracks: () -> Unit,
    onCreateGroup: (description: String, trackId: Int) -> Unit,
) {
    var selectedTrack by remember { mutableStateOf<Track?>(null) }
    var showTrackSelection by remember { mutableStateOf(false) }
    var showAddGroupDialog by remember { mutableStateOf(false) }


    FloatingActionButton(
        onClick = {
            showTrackSelection = true
        },
    ) {
        Icon(Icons.Default.GroupAdd, contentDescription = "Add Groups")

        if (showTrackSelection) {
            TrackSelectionDialog(
                tracks = tracks,
                onDismiss = { showTrackSelection = false },
                onTrackSelected = { track ->
                    selectedTrack = track
                    showTrackSelection = false
                    showAddGroupDialog = true
                },
                oldTrack = selectedTrack?.id ?: 0,
                loadTracks = loadTracks
            )
        }

        if (showAddGroupDialog) {
            AddGroupDialog(
                onDismiss = {
                    showAddGroupDialog = false
                    selectedTrack = null
                },
                onCreateGroup = { description, trackId ->
                    onCreateGroup(description, trackId)
                    showAddGroupDialog = false
                },
                selectedTrack = selectedTrack!!, // It's guaranteed to be non-null here
            )
        }
    }
}

