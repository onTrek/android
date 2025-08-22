package com.ontrek.mobile.screens.profile.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ontrek.mobile.utils.components.DeleteConfirmationDialog

@Composable
fun MenuDialog(
    onDeleteProfile: () -> Unit,
    onLogoutClick: () -> Unit,
    onDismiss: () -> Unit
) {
    var showDeleteProfileDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showDeleteProfileDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                onDeleteProfile()
                showDeleteProfileDialog = false
                onDismiss()
            },
            onDismiss = { showDeleteProfileDialog = false },
            title = "Delete Profile",
            text = "Are you sure you want to delete your profile? This action cannot be undone."
        )
    }

    if (showLogoutDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                onLogoutClick()
                showLogoutDialog = false
                onDismiss()
            },
            textButton = "Logout",
            onDismiss = { showLogoutDialog = false },
            title = "Logout",
            text = "Are you sure you want to log out?"
        )
    }

    // Dialog principale con le opzioni
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            elevation = androidx.compose.material3.CardDefaults.cardElevation(
                defaultElevation = 6.dp
            )
        ) {
            Text(
                text = "Profile Options",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )

            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Logout option
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showLogoutDialog = true },
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            modifier = Modifier.padding(end = 16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Sign out",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                // Delete profile option
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showDeleteProfileDialog = true },
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Profile",
                            modifier = Modifier.padding(end = 16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Delete profile",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}