package com.ontrek.mobile.screens.friends
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
@Composable
fun FriendsTab(
    viewModel: FriendsViewModel,
    token: String
) {
    val friendsState by viewModel.friendsState.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (friendsState) {
            is FriendsViewModel.FriendsState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
                )
            }
            is FriendsViewModel.FriendsState.Error -> {
                val errorState = friendsState as FriendsViewModel.FriendsState.Error
                Text(
                    text = errorState.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
                )
            }
            is FriendsViewModel.FriendsState.Success -> {
                val friends = (friendsState as FriendsViewModel.FriendsState.Success).friends

                if (friends.isEmpty()) {
                    Text(
                        text = "Non hai ancora amici",
                        modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(friends) { friend ->
                            FriendItem(
                                friend = friend,
                                onRemoveFriend = { viewModel.removeFriend(friend.id, token) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FriendItem(
    friend: FriendsViewModel.Friend,
    onRemoveFriend: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
               Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Person,
                    contentDescription = "Profilo di ${friend.name}",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(8.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = friend.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "@${friend.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onRemoveFriend) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                    contentDescription = "Elimina amico",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun SearchTab(
    viewModel: FriendsViewModel,
    token: String
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchState by viewModel.searchState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChange(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Cerca utenti") },
            leadingIcon = { Icon(androidx.compose.material.icons.Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxSize()) {
            when (searchState) {
                is FriendsViewModel.SearchState.Initial -> {
                    Text(
                        text = "Cerca utenti da aggiungere come amici",
                        modifier = Modifier.align(androidx.compose.ui.Alignment.Center),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is FriendsViewModel.SearchState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
                    )
                }
                is FriendsViewModel.SearchState.Empty -> {
                    Text(
                        text = "Nessun utente trovato",
                        modifier = Modifier.align(androidx.compose.ui.Alignment.Center),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is FriendsViewModel.SearchState.Error -> {
                    val errorState = searchState as FriendsViewModel.SearchState.Error
                    Text(
                        text = errorState.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
                    )
                }
                is FriendsViewModel.SearchState.Success -> {
                    val users = (searchState as FriendsViewModel.SearchState.Success).users

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(users) { user ->
                            UserItem(
                                user = user,
                                onSendRequest = {
                                    viewModel.sendFriendRequest(user.id, token) {
                                        // Feedback di successo con Snackbar o Toast
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserItem(
    user: FriendsViewModel.User,
    onSendRequest: () -> Unit
) {
    var requestSent by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Person,
                    contentDescription = "Profilo",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(8.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "@${user.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = {
                    if (!requestSent) {
                        onSendRequest()
                        requestSent = true
                    }
                },
                enabled = !requestSent
            ) {
                Text(if (requestSent) "Richiesta inviata" else "Aggiungi")
            }
        }
    }
}

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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    imageVector = androidx.compose.material.icons.Icons.Default.Person,
                    contentDescription = "Profilo",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(8.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = request.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "@${request.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatTimeAgo(request.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onReject,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Rifiuta")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(onClick = onAccept) {
                    Text("Accetta")
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