package com.ontrek.mobile.screens.group

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ontrek.mobile.screens.Screen
import com.ontrek.mobile.screens.group.components.AddGroupButton
import com.ontrek.mobile.utils.components.BottomNavBar
import com.ontrek.mobile.utils.components.EmptyComponent
import com.ontrek.mobile.utils.components.ErrorViewComponent
import com.ontrek.shared.data.GroupDoc

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(navController: NavHostController, token: String) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val viewModel: GroupsViewModel = viewModel()
    val context = LocalContext.current

    val listGroupState by viewModel.listGroup.collectAsStateWithLifecycle()
    val msgToast by viewModel.msgToast.collectAsStateWithLifecycle("")
    val tracks by viewModel.tracks.collectAsStateWithLifecycle()
    val isCharged by viewModel.isCharged.collectAsStateWithLifecycle()

    var groups by remember { mutableStateOf(listOf<GroupDoc>()) }

    LaunchedEffect(isCharged) {
        viewModel.loadGroups(token)
    }

    if (msgToast.isNotEmpty()) {
        LaunchedEffect(msgToast) {
            Toast.makeText(context, msgToast, Toast.LENGTH_SHORT).show()
            viewModel.clearMsgToast()
        }
    }

    LaunchedEffect(listGroupState) {
        if (listGroupState is GroupsViewModel.GroupsState.Success) {
            groups = (listGroupState as GroupsViewModel.GroupsState.Success).groups
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Hiking Groups") },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = { BottomNavBar(navController) },
        floatingActionButton = {
            AddGroupButton(
                tracks = tracks,
                loadTracks = { viewModel.loadTracks(token) },
                onCreateGroup = { description, trackId ->
                    viewModel.addGroup(
                        description = description,
                        trackId = trackId,
                        token = token,
                        navController = navController
                    )
                }
            )
        }
    ) { innerPadding ->

        PullToRefreshBox(
            isRefreshing = listGroupState is GroupsViewModel.GroupsState.Loading,
            onRefresh = {
                viewModel.loadGroups(token)
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (listGroupState) {
                is GroupsViewModel.GroupsState.Loading,
                is GroupsViewModel.GroupsState.Success -> {
                    if (groups.isEmpty()) {
                        EmptyComponent(
                            icon = Icons.Default.Groups,
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
                                        navController.navigate(
                                            Screen.GroupDetails.createRoute(group.group_id)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                is GroupsViewModel.GroupsState.Error -> {
                    ErrorViewComponent(
                        errorMsg = (listGroupState as GroupsViewModel.GroupsState.Error).message
                    )
                }
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
                Text(
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Route,
                    contentDescription = "Track Icon",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = group.track.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}