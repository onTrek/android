package com.ontrek.mobile.screens.friends.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ontrek.mobile.screens.friends.FriendsViewModel
import com.ontrek.mobile.screens.friends.components.Username
import com.ontrek.mobile.utils.components.EmptyComponent
import com.ontrek.mobile.utils.components.ErrorComponent

@Composable
fun RequestsSent(
    viewModel: FriendsViewModel,
) {
    val sentRequestsState by viewModel.sentFriendRequests.collectAsState()

    Box (
        modifier = Modifier.fillMaxSize()
    ) {
        when (sentRequestsState) {
            is FriendsViewModel.SentRequestsState.Loading -> {
                Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            }
            is FriendsViewModel.SentRequestsState.Error -> {
                ErrorComponent(
                    errorMsg = (sentRequestsState as FriendsViewModel.SentRequestsState.Error).message
                )
            }
            is FriendsViewModel.SentRequestsState.Success -> {
                val requests = (sentRequestsState as FriendsViewModel.SentRequestsState.Success).requests
                if (requests.isEmpty()) {
                    EmptyComponent(
                        title = "There are no sent friend requests",
                        description = "You can wait for friend requests or send your own.",
                        icon = Icons.Default.PersonSearch
                    )
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