package com.ontrek.mobile.utils.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchUsersDialog(
    onDismiss: () -> Unit,
    onUserSelected: (user: UserMinimal) -> Unit,
    token: String,
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
                                        token = token,
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
                    label = { Text("Search") },
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
                            headlineContent = { Text(user.username) },
                            leadingContent = {
                                Icon(Icons.Default.Person, contentDescription = "User")
                            },
                            modifier = Modifier.clickable {
                                onUserSelected(user)
                                onDismiss()
                            }
                        )
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
