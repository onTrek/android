package com.ontrek.mobile.screens.hike

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ontrek.mobile.utils.components.BottomNavBar
import com.ontrek.mobile.utils.components.EmptyComponent
import com.ontrek.mobile.utils.components.ErrorViewComponent
import com.ontrek.mobile.utils.components.TitleGeneric
import com.ontrek.mobile.utils.components.hikesComponents.AddGroup
import com.ontrek.mobile.utils.components.hikesComponents.IconTextComponent
import com.ontrek.shared.data.GroupDoc
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HikesScreen(navController: NavHostController, token: String) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val viewModel: HikesViewModel = viewModel()
    val context = LocalContext.current

    val listGroup by viewModel.listGroup.collectAsState()
    val msgToast by viewModel.msgToast.collectAsState("")
    val addDialog = remember { mutableStateOf(false) }
    val tracks by viewModel.tracks.collectAsState()
    val isCharged by viewModel.isCharged.collectAsState()

    LaunchedEffect(isCharged) {
        viewModel.loadGroups(token)
        viewModel.loadTracks(token)
    }

    LaunchedEffect(msgToast) {
        if (msgToast.isNotEmpty()) {
            Toast.makeText(context, msgToast, Toast.LENGTH_SHORT).show()
            viewModel.resetMsgToast()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("My Hikes Groups") },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = { BottomNavBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    addDialog.value = true
                },
                modifier = Modifier.padding(16.dp),
            ) {
                Icon(Icons.Default.GroupAdd, contentDescription = "Add Groups")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (listGroup) {
                is HikesViewModel.GroupsState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is HikesViewModel.GroupsState.Success -> {
                    val groups = (listGroup as HikesViewModel.GroupsState.Success).groups
                    if (groups.isEmpty()) {
                        EmptyComponent (
                            title = "No Groups Found",
                            description = "You haven't created any groups yet.",
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            items(groups) { group ->
                                GroupItem(
                                    group = group,
                                    onItemClick = {
                                        Log.d("HikesScreen", "Navigating to GroupDetailsScreen with group ID: ${group.group_id}")
                                        navController.navigate("GroupDetailsScreen/${group.group_id}")
                                    }
                                )
                            }
                        }
                    }
                }
                is HikesViewModel.GroupsState.Error -> {
                    ErrorViewComponent(
                        errorMsg = (listGroup as HikesViewModel.GroupsState.Error).message
                    )
                }
            }

            if (addDialog.value) {
                AddGroup(
                    onDismiss = { addDialog.value = false },
                    onCreateGroup = { description, trackId ->
                        viewModel.addGroup(
                            description = description,
                            trackId = trackId,
                            token = token
                        )
                        addDialog.value = false
                    },
                    isLoading = listGroup is HikesViewModel.GroupsState.Loading,
                    tracks = tracks
                )
            }
        }
    }
}

@Composable
fun GroupItem(group: GroupDoc, onItemClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onItemClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Hiking,
                contentDescription = "Icona Gruppo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(60.dp)
                    .padding(end = 16.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                TitleGeneric(
                    title = group.description,
                    modifier = Modifier.fillMaxWidth(0.8f),
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(4.dp))

                IconTextComponent(
                    text = group.file.filename,
                    icon = Icons.Default.Terrain,
                    styleText = MaterialTheme.typography.bodySmall,
                    modifierIcon = Modifier.size(20.dp).padding(end = 4.dp)
                )

                IconTextComponent(
                    text = "Created on: ${formatDate(group.created_at)}",
                    icon = Icons.Default.Update,
                    styleText = MaterialTheme.typography.bodySmall,
                    modifierIcon = Modifier.size(20.dp).padding(end = 4.dp)
                )

                IconTextComponent(
                    text = group.members_number.toString(),
                    icon = Icons.Default.Group,
                    styleText = MaterialTheme.typography.bodySmall,
                    modifierIcon = Modifier.size(20.dp).padding(end = 4.dp)
                )
            }
        }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val instant = Instant.parse(dateString)
        DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault()).format(instant)
    } catch (e: Exception) {
        dateString
    }
}