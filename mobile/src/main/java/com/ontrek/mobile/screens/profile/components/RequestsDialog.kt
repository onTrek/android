package com.ontrek.mobile.screens.profile.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ontrek.mobile.screens.profile.ProfileViewModel
import com.ontrek.mobile.utils.components.EmptyComponent

@Composable
fun RequestsDialog(
    viewModel: ProfileViewModel,
    onDismiss: () -> Unit
) {
    val requestsState by viewModel.requestsState.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Requests's friendship",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                HorizontalDivider(
                    thickness = DividerDefaults.Thickness,
                    color = DividerDefaults.color
                )

                when (requestsState) {

                    is ProfileViewModel.RequestsState.Empty -> {
                        EmptyComponent(
                            fillMaxSize = false,
                            title = "No requests",
                            description = "Don't have any requests",
                            icon = Icons.Default.AccountCircle,
                        )
                    }

                    is ProfileViewModel.RequestsState.Error -> {
                        val errorMsg =
                            (requestsState as ProfileViewModel.RequestsState.Error).message
                        Text(
                            text = errorMsg,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 32.dp)
                        )
                    }

                    is ProfileViewModel.RequestsState.Success -> {
                        val requests =
                            (requestsState as ProfileViewModel.RequestsState.Success).requests

                        LazyColumn(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .heightIn(max = 300.dp)
                        ) {
                            items(requests) { request ->
                                RequestItem(
                                    request = request,
                                    onAccept = { viewModel.acceptRequest(request) },
                                    onReject = { viewModel.rejectFriendRequest(request.id) }
                                )
                            }
                        }
                    }

                    ProfileViewModel.RequestsState.Loading -> {
                        CircularProgressIndicator()
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp)
                ) {
                    Text("Close")
                }
            }
        }
    }
}