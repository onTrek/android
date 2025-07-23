package com.ontrek.mobile.screens.hike.hikesComponents

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ontrek.mobile.screens.hike.detail.GroupDetailsViewModel
import com.ontrek.mobile.utils.components.DeleteConfirmationDialog
import com.ontrek.mobile.utils.components.SearchUsersDialog
import com.ontrek.shared.data.GroupMember
import androidx.core.graphics.toColorInt
import com.ontrek.mobile.screens.friends.friendsComponents.Username
import com.ontrek.shared.data.UserMinimal

@Composable
fun MembersGroup(
    currentUserID: String,
    owner: String,
    membersState: List<GroupMember>,
    groupId: Int,
    token: String,
    viewModel: GroupDetailsViewModel,
) {
    var showAddMemberDialog by remember { mutableStateOf(false) }

    if (showAddMemberDialog && currentUserID == owner) {
        SearchUsersDialog(
            onDismiss = { showAddMemberDialog = false },
            onUserSelected = { user ->
                viewModel.addMember(user.id, groupId, token)
                showAddMemberDialog = false
            },
            token = token,
            title = "Add Member",
            onlyFriend = true,
       )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Members (${membersState.size})",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            if (currentUserID == owner) {
                IconButton(
                    onClick = { showAddMemberDialog = true },
                    modifier = Modifier.size(30.dp).padding(bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAddAlt1,
                        contentDescription = "add member",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = DividerDefaults.Thickness,
            color = DividerDefaults.color
        )

        if (membersState.isEmpty()) {
            Text(
                text = "No members in this group.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            Column {
                membersState.forEach { member ->
                    MemberItem(
                        currentUserID = currentUserID,
                        owner = owner,
                        member = member,
                        onRemoveClick = {
                            viewModel.removeMember(groupId, member.id, token)
                        },
                   )
                }
            }
        }
    }
}

@Composable
fun MemberItem(
    currentUserID: String,
    owner: String,
    member: GroupMember,
    onRemoveClick: () -> Unit
) {
    var showDialogRemove by remember { mutableStateOf(false) }

    if (showDialogRemove) {
        DeleteConfirmationDialog(
            onDismiss = { showDialogRemove = false },
            onConfirm = onRemoveClick,
            title = "Remove Member",
            text = "Are you sure you want to remove @${member.username} from the group?"
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Member",
            tint = Color(member.color.toColorInt()),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Username(
            username = member.username,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        if (member.id == owner) {
            Text(
                text = "(Owner)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        } else if (currentUserID == owner) {
            Box(
                modifier = Modifier
                    .clickable(
                        onClick = { showDialogRemove = true },
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove Member",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}