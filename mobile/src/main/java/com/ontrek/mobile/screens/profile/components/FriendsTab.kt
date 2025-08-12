package com.ontrek.mobile.screens.profile.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ontrek.mobile.screens.profile.ProfileViewModel
import com.ontrek.mobile.screens.profile.ProfileViewModel.RequestsState.Companion.count
import com.ontrek.mobile.utils.components.EmptyComponent
import com.ontrek.mobile.utils.components.ErrorComponent
import com.ontrek.mobile.utils.components.ProfileItem
import com.ontrek.shared.data.UserMinimal

@Composable
fun FriendsTab(
    viewModel: ProfileViewModel,
) {
    val friendsState by viewModel.friendsState.collectAsState()
    val requestsState by viewModel.requestsState.collectAsState()
    val requestsCount = requestsState.count
    var showRequestsDialog by remember { mutableStateOf(false) }

    if (showRequestsDialog) {
        RequestsDialog(
            viewModel = viewModel,
            onDismiss = { showRequestsDialog = false }
        )
    }

    Column(
        modifier = Modifier.padding(vertical = 5.dp, horizontal = 5.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Friends",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = { showRequestsDialog = true }) {
                Icon(
                    imageVector = if (requestsCount > 0) {
                        Icons.Default.NotificationsActive
                    } else {
                        Icons.Default.NotificationsNone
                    },
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        HorizontalDivider(
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        when (friendsState) {
            is ProfileViewModel.FriendsState.Loading -> {
                CircularProgressIndicator()
            }

            is ProfileViewModel.FriendsState.Empty -> {
                EmptyComponent(
                    title = "No Friends",
                    description = "You have no friends yet. Start adding friends to see them here.",
                    icon = Icons.Default.PersonSearch,
                )
            }

            is ProfileViewModel.FriendsState.Error -> {
                val errorState = friendsState as ProfileViewModel.FriendsState.Error
                ErrorComponent(
                    errorMsg = errorState.message,
                )
            }

            is ProfileViewModel.FriendsState.Success -> {
                val friends = (friendsState as ProfileViewModel.FriendsState.Success).friends

                if (friends.isEmpty()) {
                    EmptyComponent(
                        title = "There are no friends yet",
                        description = "You can search for friends and add them.",
                        icon = Icons.Default.PersonSearch,
                    )
                } else {
                    Column {
                        friends.forEach { friend ->
                            ProfileItem(
                                user = UserMinimal(
                                    id = friend.id,
                                    username = friend.username,
                                ),
                                onClick = { viewModel.removeFriend(friend.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}