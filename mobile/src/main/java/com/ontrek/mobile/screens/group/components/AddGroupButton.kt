package com.ontrek.mobile.screens.group.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue


@Composable
fun AddGroupButton(
    onCreateGroup: (description: String) -> Unit,
) {
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
                },
                onCreateGroup = { description ->
                    onCreateGroup(description)
                    showAddGroupDialog = false
                }
            )
        }
    }
}

