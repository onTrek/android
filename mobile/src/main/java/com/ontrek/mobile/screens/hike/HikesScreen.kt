package com.ontrek.mobile.screens.hike

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ontrek.mobile.utils.components.BottomNavBar
import com.ontrek.mobile.utils.components.EmptyComponent
import com.ontrek.mobile.utils.components.ErrorViewComponent
import com.ontrek.mobile.utils.components.hikesComponents.AddGroup
import com.ontrek.shared.data.GroupDoc

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
                title = { Text("Hikes") },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = { BottomNavBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    addDialog.value = true
                },
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Groups")
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text (
                    text = group.description,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = "Members Icon",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = group.members_number.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )

            Row (
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Terrain,
                    contentDescription = "Track Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = group.track.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}