package com.ontrek.mobile.utils.components

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CancelScheduleSend
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ontrek.mobile.screens.friends.components.Username
import com.ontrek.shared.data.UserMinimal

@Composable
fun ProfileItem(
    user: UserMinimal,
    isSearch: Boolean = false,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit = {},
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(
                horizontal = 1.dp,
                vertical = 4.dp,
            ),
        shape = RoundedCornerShape(50.dp),
    ) {
        if (showDeleteDialog) {
            DeleteConfirmationDialog(
                title = "Delete Friendship",
                text = "Are you sure you want to delete @${user.username} from this list?",
                onConfirm = {
                    onClick()
                    showDeleteDialog = false
                },
                onDismiss = { showDeleteDialog = false },
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 10.dp,
                    vertical = 8.dp,
                ),
        ) {
            ImageProfile(
                userID = user.id,
                color = color,
                modifier = Modifier.padding(end = 10.dp),
            )

            Username(
                user.username,
                modifier = Modifier.weight(1f),
            )

            if (isSearch) {
                when (user.state) {
                    0 -> {
                        IconButton(
                            onClick = {
                                Toast.makeText(
                                    context,
                                    "You are already sending a friendship request to @${user.username}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.CancelScheduleSend,
                                tint = MaterialTheme.colorScheme.outline,
                                contentDescription = "Sended Friendship Request",
                            )
                        }
                    }

                    -1 -> {
                        IconButton(
                            onClick = { onClick() },
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                tint = MaterialTheme.colorScheme.primary,
                                contentDescription = "Send Friendship Request",
                            )
                        }
                    }

                    1 -> {
                        IconButton(
                            onClick = {
                                Toast.makeText(
                                    context,
                                    "You are already friends with @${user.username}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.SupervisorAccount,
                                tint = MaterialTheme.colorScheme.primary,
                                contentDescription = "Delete Friendship",
                            )
                        }
                    }
                }
            } else {
                IconButton(
                    onClick = { showDeleteDialog = true },
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        tint = MaterialTheme.colorScheme.error,
                        contentDescription = "Delete Friendship",
                    )
                }
            }
        }
    }
}