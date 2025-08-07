package com.ontrek.mobile.screens.group.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ontrek.mobile.utils.components.EmptyComponent
import com.ontrek.mobile.utils.components.ProfileItem
import com.ontrek.shared.api.search.searchUsers
import com.ontrek.shared.data.UserMinimal
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchMembersComponent(
    onDismiss: () -> Unit,
    onUserSelected: (user: UserMinimal) -> Unit,
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
                Text("Search Friends", style = MaterialTheme.typography.titleLarge)
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
                                        friendOnly = true,
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
                    label = { Text("Search Friends") },
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
                        ProfileItem(
                            user = user,
                            addMember = true,
                            onClick = {
                                onUserSelected(user)
                                onDismiss()
                            }
                        )
                    }
                    if (searchResults.isEmpty()) {
                        item {
                            EmptyComponent(
                                title = "No results found",
                                description = "Try searching with a different username.",
                                icon = Icons.Default.Search,
                                fillMaxSize = false
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
