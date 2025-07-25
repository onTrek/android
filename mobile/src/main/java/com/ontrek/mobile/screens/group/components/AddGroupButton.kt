package com.ontrek.mobile.screens.group.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.ontrek.shared.data.Track


@Composable
fun AddGroupButton(
    onCreateGroup: (description: String) -> Unit,
) {
    var selectedTrack by remember { mutableStateOf<Track?>(null) }
    var showAddGroupDialog by remember { mutableStateOf(false) }


    FloatingActionButton(
        onClick = {
            showAddGroupDialog = true
        },
    ) {
        Icon(Icons.Default.GroupAdd, contentDescription = "Add Groups")

        if (showAddGroupDialog) {
            AddGroupDialog(
                onDismiss = {
                    showAddGroupDialog = false
                    selectedTrack = null
                },
                onCreateGroup = { description ->
                    onCreateGroup(description)
                    showAddGroupDialog = false
                }
            )
        }
    }
}

