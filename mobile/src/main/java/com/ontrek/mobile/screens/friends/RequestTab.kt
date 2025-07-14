package com.ontrek.mobile.screens.friends
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max

@Composable
fun RequestsTab(
    viewModel: FriendsViewModel,
    token: String
) {
    val requestsState by viewModel.requestsState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (requestsState) {
            is FriendsViewModel.RequestsState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
                )
            }
            is FriendsViewModel.RequestsState.Error -> {
                val errorState = requestsState as FriendsViewModel.RequestsState.Error
                Text(
                    text = errorState.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
                )
            }
            is FriendsViewModel.RequestsState.Success -> {
                val requests = (requestsState as FriendsViewModel.RequestsState.Success).requests

                if (requests.isEmpty()) {
                    Text(
                        text = "Non hai richieste di amicizia in sospeso",
                        modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(15.dp)
                    ) {
                        items(requests) { request ->
                            RequestItem(
                                request = request,
                                onAccept = { viewModel.acceptFriendRequest(request.id, token) },
                                onReject = { viewModel.rejectFriendRequest(request.id, token) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RequestItem(
    request: FriendsViewModel.FriendRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profilo",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "@${request.username}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatTimeAgo(request.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Rifiuta",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Button(onClick = onAccept) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Accetta",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// Funzione per formattare il tempo passato
fun formatTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Adesso"
        diff < 3_600_000 -> "${diff / 60_000} minuti fa"
        diff < 86_400_000 -> "${diff / 3_600_000} ore fa"
        else -> "${diff / 86_400_000} giorni fa"
    }
}