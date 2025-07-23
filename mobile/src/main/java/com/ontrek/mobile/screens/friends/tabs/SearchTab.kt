package com.ontrek.mobile.screens.friends.tabs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.ontrek.mobile.screens.friends.FriendsViewModel
import com.ontrek.mobile.screens.friends.friendsComponents.Username

@Composable
fun SearchTab(
    viewModel: FriendsViewModel,
    token: String
) {
    val searchState by viewModel.searchState.collectAsState()
    val sentRequestsState by viewModel.sentFriendRequests.collectAsState()
    var query by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    // Carica le richieste inviate
    LaunchedEffect(Unit) {
        viewModel.loadSentFriendRequests(token)
    }

    Column(
        modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
        ) {
        // Barra di ricerca
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    isSearching = it.isNotEmpty()
                    viewModel.onSearchQueryChange(it, token)
                },
                label = { Text("Search Users") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            )

            // Dropdown dei risultati di ricerca
            if (isSearching && searchState is FriendsViewModel.SearchState.Success) {
                val users = (searchState as FriendsViewModel.SearchState.Success).users
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp)
                        .zIndex(1f),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(users) { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.sendFriendRequest(user, token)
                                        query = ""
                                        isSearching = false
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Username(
                                    username = user.username,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send Friend Request",
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (users.indexOf(user) < users.lastIndex) {
                                HorizontalDivider(
                                    Modifier.padding(horizontal = 16.dp),
                                    DividerDefaults.Thickness,
                                    DividerDefaults.color
                                )
                            }
                        }
                    }
                }
            }

            // Indicatore di caricamento
            if (searchState is FriendsViewModel.SearchState.Loading && isSearching) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Intestazione sezione richieste inviate
        Text(
            "Sent Friend Requests",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Lista delle richieste inviate
        Box (
            modifier = Modifier.fillMaxSize(),
        ) {
            when (sentRequestsState) {
                is FriendsViewModel.SentRequestsState.Loading -> {
                    Box(Modifier.fillMaxSize()) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                }
                is FriendsViewModel.SentRequestsState.Error -> {
                    Text(
                        (sentRequestsState as FriendsViewModel.SentRequestsState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                is FriendsViewModel.SentRequestsState.Success -> {
                    val requests = (sentRequestsState as FriendsViewModel.SentRequestsState.Success).requests
                    if (requests.isEmpty()) {
                        Box(Modifier.fillMaxSize()) {
                            Text(
                                "There are no sent friend requests",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(requests) { request ->
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Username(
                                            username = request.username,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}