package com.ontrek.mobile.utils.components.hikesComponents.groupDetailsComponent

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ontrek.shared.data.GroupMember
import com.ontrek.shared.data.MemberInfo


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberDialog(
    groupId: Int,
    onDismiss: () -> Unit,
    onMemberAdded: () -> Unit,
    token: String,
) {
    var query by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf(listOf<GroupMember>()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Aggiungi membro", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        if (it.isNotBlank()) {
                            isLoading = true
                            error = null
                            // Esegui la ricerca utenti (solo amici)

                        } else {
                            searchResults = emptyList()
                        }
                    },
                    label = { Text("Cerca utente (amico)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                LazyColumn {
                    items(searchResults) { user ->
                        ListItem(
                            headlineContent = { Text(user.username) },
                            supportingContent = { Text(user.username) },
                            modifier = Modifier
                                .clickable {
                                }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close")
                }
            }
        }
    }
}