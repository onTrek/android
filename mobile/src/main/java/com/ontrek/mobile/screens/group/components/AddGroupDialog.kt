package com.ontrek.mobile.screens.group.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ontrek.shared.constant.MAX_DESCRIPTION_LENGTH
import com.ontrek.shared.data.Track

@Composable
fun AddGroupDialog(
    selectedTrack: Track,
    onCreateGroup: (description: String, trackId: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var description by remember { mutableStateOf("") }

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
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.Companion
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.Companion.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Icon(
                        imageVector = Icons.Default.Route,
                        contentDescription = "Track",
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.Companion.width(8.dp))

                    Text(
                        text = selectedTrack.title,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Companion.Ellipsis,
                        modifier = Modifier.Companion.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { if (it.length <= MAX_DESCRIPTION_LENGTH) description = it },
                    label = { Text("Description") },
                    modifier = Modifier.Companion.fillMaxWidth(),
                    minLines = 3,
                    supportingText = {
                        Text("${description.length}/${MAX_DESCRIPTION_LENGTH}")
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCreateGroup(description, selectedTrack.id)
                },
                enabled = description.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    )
}