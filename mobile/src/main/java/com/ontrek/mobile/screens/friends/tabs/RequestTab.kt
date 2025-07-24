package com.ontrek.mobile.screens.friends.tabs
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ontrek.mobile.screens.friends.FriendsViewModel
import com.ontrek.mobile.utils.components.DeleteConfirmationDialog
import com.ontrek.mobile.screens.friends.components.Username
import com.ontrek.mobile.utils.components.EmptyComponent
import com.ontrek.mobile.utils.components.ErrorViewComponent
import com.ontrek.shared.data.FriendRequest
import com.ontrek.shared.utils.formatTimeAgo

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
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is FriendsViewModel.RequestsState.Error -> {
                val errorState = requestsState as FriendsViewModel.RequestsState.Error
                ErrorViewComponent(
                    errorMsg = errorState.message
                )
            }
            is FriendsViewModel.RequestsState.Success -> {
                val requests = (requestsState as FriendsViewModel.RequestsState.Success).requests

                if (requests.isEmpty()) {
                    EmptyComponent(
                        title = "There are no friend requests",
                        description = "You can wait for friend requests or send your own.",
                        icon = Icons.Default.PersonSearch,
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
                                onAccept = { viewModel.acceptRequest(request, token) },
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
    request: FriendRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
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
                    Username(
                        username = request.username,
                    )
                    Text(
                        text = formatTimeAgo(request.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = { showDeleteConfirmation = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.outline),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (showDeleteConfirmation) {
                        DeleteConfirmationDialog(
                            onDismiss = { showDeleteConfirmation = false },
                            onConfirm = {
                                onReject()
                                showDeleteConfirmation = false
                            },
                            title = "Remove Request",
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Button(onClick = onAccept) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Acept",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}