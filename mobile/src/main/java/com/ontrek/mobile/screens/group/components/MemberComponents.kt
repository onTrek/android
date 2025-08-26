package com.ontrek.mobile.screens.group.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.ontrek.mobile.screens.group.detail.GroupDetailsViewModel
import com.ontrek.mobile.utils.components.ProfileItem
import com.ontrek.shared.data.GroupMember
import com.ontrek.shared.data.UserMinimal

@Composable
fun MembersGroup(
    currentUserID: String,
    owner: String,
    membersState: List<GroupMember>,
    groupId: Int,
    viewModel: GroupDetailsViewModel,
) {
    var showAddMemberDialog by remember { mutableStateOf(false) }

    if (showAddMemberDialog && currentUserID == owner) {
        SearchMembersComponent(
            onDismiss = { showAddMemberDialog = false },
            onUserSelected = { user ->
                viewModel.addMember(user.id, groupId)
                showAddMemberDialog = false
            }
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
                    modifier = Modifier
                        .size(30.dp)
                        .padding(bottom = 4.dp)
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
                    val user = UserMinimal(
                        id = member.id,
                        username = member.username,
                        state = 0,
                    )
                    ProfileItem(
                        textDelete = "Remove Member",
                        user = user,
                        currentUserId = currentUserID,
                        groupOwner = owner,
                        color = Color(member.color.toColorInt()),
                        onClick = { viewModel.removeMember(groupId, member.id) },
                    )
                }
            }
        }
    }
}