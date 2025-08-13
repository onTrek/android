package com.ontrek.wear.screens.track.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Chip
import com.ontrek.shared.data.MemberInfo

@Composable
fun SosFriendDialog(
    user: List<MemberInfo>,
    onDismiss: () -> Unit,
    onConfirm: (MemberInfo) -> Unit
) {
    if (user.isEmpty()) {
        onDismiss()
        return
    }

    if (user.size == 1) {
        // Solo una persona ha richiesto aiuto, mostro direttamente il nome
        val member = user[0]
        Dialog(
            onDismissRequest = onDismiss
        ) {
            Card(
                onClick = { /* non fare nulla al click sul card */ }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Help Request", style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Would you like to help @${member.user.username} who requested assistance?",
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = onDismiss
                        ) {
                            Text("No")
                        }
                        Button(
                            onClick = { onConfirm(member) }) {
                            Text("Yes")
                        }
                    }
                }
            }
        }
    } else {
        // Più persone hanno richiesto aiuto, mostro il numero di richieste
        Dialog(
            onDismissRequest = onDismiss
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Help Requests", style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${user.size} people need your help",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Select who you want to help:",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))

                ScalingLazyColumn {
                    items(user) { member ->
                        Chip(
                            onClick = { onConfirm(member) },
                            label = { Text(member.user.username) },
                            secondaryLabel = { Text("Tap to help") })
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onDismiss
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}