package com.ontrek.mobile.utils.components.hikesComponents.groupDetailsComponent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
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
import com.ontrek.shared.data.GroupMember

@Composable
fun MembersGroup(
    creatorGroupMember: String = "",
    membersState: List<GroupMember>,
    groupId: Int,
    token: String,
    viewModel: com.ontrek.mobile.screens.hike.detail.GroupDetailsViewModel,
) {
    var showAddMemberDialog by remember { mutableStateOf(false) }

    if (showAddMemberDialog) {
        AddMemberDialog(
            groupId = groupId,
            onDismiss = { showAddMemberDialog = false },
            onMemberAdded = { viewModel.loadGroupDetails(groupId, token) },
            token = token
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
                        creator = creatorGroupMember,
                        member = member,
                        onRemoveClick = {
                            viewModel.removeMember(groupId, member.id, token)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MemberItem(
    creator: String,
    member: GroupMember,
    onRemoveClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Member",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = member.username,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        if (member.username == creator) {
            Text(
                text = "(Owner)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}