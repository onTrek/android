package com.ontrek.mobile.screens.friends.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ontrek.mobile.screens.friends.FriendsViewModel
import com.ontrek.mobile.utils.components.Username
import com.ontrek.shared.data.Friend

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
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is FriendsViewModel.FriendsState.Error -> {
                val errorState = friendsState as FriendsViewModel.FriendsState.Error
                Text(
                    text = errorState.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is FriendsViewModel.FriendsState.Success -> {
                val friends = (friendsState as FriendsViewModel.FriendsState.Success).friends

                if (friends.isEmpty()) {
                    Text(
                        text = "There are no friends to display",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(15.dp)
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
    friend: Friend,
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
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile ${friend.id}",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(8.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(16.dp))

                Username(
                    username = friend.username,
                    modifier = Modifier.weight(1f)
                )
            }

            IconButton(onClick = onRemoveFriend) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Friend",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
