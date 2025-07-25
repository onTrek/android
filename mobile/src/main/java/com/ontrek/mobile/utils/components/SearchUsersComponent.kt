package com.ontrek.mobile.utils.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ontrek.shared.api.search.searchUsers
import com.ontrek.shared.data.UserMinimal
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchUsersDialog(
    onDismiss: () -> Unit,
    onUserSelected: (user: UserMinimal) -> Unit,
    onlyFriend: Boolean = false,
    title: String = "Search Users",
) {
    var query by remember { mutableStateOf("") }
    var searchResults: List<UserMinimal> by remember { mutableStateOf(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        if (it.isNotBlank()) {
                            coroutineScope.launch {
                                isLoading = true
                                error = null
                                try {
                                    searchUsers(
                                        query = it,
                                        friendOnly = onlyFriend,
                                        onSuccess = { results ->
                                            searchResults = results ?: emptyList()
                                        },
                                        onError = { errorMessage ->
                                            error = errorMessage
                                        }
                                    )
                                } catch (e: Exception) {
                                    error = e.message ?: "Error occurred while searching"
                                } finally {
                                    isLoading = false
                                }
                            }
                        } else {
                            searchResults = emptyList()
                        }
                    },
                    label = { Text( if (onlyFriend) "Search Friends" else "Search Users" ) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    items(searchResults) { user ->
                        ListItem(
                            headlineContent = { Text(
                                "@${user.username}",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            ) },
                            leadingContent = {
                                Icon(Icons.Default.PersonAddAlt1, contentDescription = "User")
                            },
                            trailingContent = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add utente",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            modifier = Modifier.clickable {
                                onUserSelected(user)
                                onDismiss()
                            }
                        )
                    }
                    if (searchResults.isEmpty()) {
                        item {
                            Text(
                                text = "No results found",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
