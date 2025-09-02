package com.ontrek.mobile.screens.group.detail

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ontrek.mobile.screens.Screen
import com.ontrek.mobile.screens.group.components.MembersGroup
import com.ontrek.mobile.screens.group.components.TrackSelectionDialog
import com.ontrek.mobile.utils.components.BottomNavBar
import com.ontrek.mobile.utils.components.DeleteConfirmationDialog
import com.ontrek.mobile.utils.components.ErrorComponent
import com.ontrek.mobile.utils.components.InfoCardRow
import com.ontrek.mobile.utils.components.StartTrackButton
import com.ontrek.shared.data.TrackInfo
import com.ontrek.shared.utils.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreen(
    groupId: Int,
    currentUser: String,
    navController: NavHostController,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val viewModel: GroupDetailsViewModel = viewModel()
    val groupState by viewModel.groupState.collectAsState()
    val membersState by viewModel.membersState.collectAsState()
    val tracks by viewModel.tracks.collectAsState()
    val msgToast by viewModel.msgToast.collectAsState()

    val context = LocalContext.current

    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showTrackSelection by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadGroupDetails(groupId)
        viewModel.setNavController(navController)
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
                title = {
                    Text(
                        text = "Details",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                },
                actions = {
                    if (groupState is GroupDetailsViewModel.GroupState.Success) {
                        val groupState = groupState as GroupDetailsViewModel.GroupState.Success
                        if (groupState.groupInfo.track.id != -1) { // Se è associata una track
                            StartTrackButton(
                                trackName = groupState.groupInfo.track.title,
                                trackId = groupState.groupInfo.track.id,
                                sessionId = null,
                                sendStartHikeMessage = { _, _, _ -> } // TODO: implementare
                            )
                        }
                    }
                }
            )
        },
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (groupState) {
                is GroupDetailsViewModel.GroupState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is GroupDetailsViewModel.GroupState.Error -> {
                    ErrorComponent(
                        errorMsg = (groupState as GroupDetailsViewModel.GroupState.Error).message
                    )
                }

                is GroupDetailsViewModel.GroupState.Success -> {
                    val groupInfo =
                        (groupState as GroupDetailsViewModel.GroupState.Success).groupInfo

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                    ) {
                        // Dialoghi di conferma
                        if (showDeleteConfirmation) {
                            DeleteConfirmationDialog(
                                title = if (currentUser != groupInfo.created_by.id) {
                                    "Leave Group"
                                } else {
                                    "Delete Group"
                                },
                                textButton = "Confirm",
                                text = if (currentUser != groupInfo.created_by.id) {
                                    "Are you sure you want to leave this group? You will no longer have access to its content."
                                } else {
                                    "Are you sure you want to delete this group? This action cannot be undone."
                                },
                                onDismiss = { showDeleteConfirmation = false },
                                onConfirm = {
                                    if (currentUser != groupInfo.created_by.id) {
                                        viewModel.leaveGroup(
                                            groupId = groupId,
                                        )
                                    } else {
                                        viewModel.deleteGroup(
                                            groupId = groupId,
                                            onSuccess = {
                                                navController.navigateUp()
                                            }
                                        )
                                    }
                                }
                            )
                        }

                        if (showTrackSelection && currentUser == groupInfo.created_by.id) {
                            TrackSelectionDialog(
                                tracks = tracks,
                                loadTracks = { viewModel.loadTracks() },
                                onDismiss = { showTrackSelection = false },
                                onTrackSelected = { track ->
                                    viewModel.changeTrack(
                                        groupId, TrackInfo(
                                            id = track.id,
                                            title = track.title
                                        )
                                    )
                                    showTrackSelection = false
                                },
                                oldTrack = groupInfo.track.id
                            )
                        }

                        // Sezione informazioni generali
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = groupInfo.description,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    thickness = DividerDefaults.Thickness,
                                    color = DividerDefaults.color
                                )

                                InfoCardRow(
                                    icon = Icons.Default.Person,
                                    label = "Created by",
                                    value = "@${groupInfo.created_by.username}",
                                )

                                InfoCardRow(
                                    icon = Icons.Default.Update,
                                    label = "Created on",
                                    value = formatDate(groupInfo.created_at, time = true),
                                )
                            }

                            // Sezione track
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = 16.dp,
                                        end = 16.dp,
                                        bottom = 10.dp,
                                        top = 4.dp
                                    ),
                            ) {
                                Text(
                                    text = "Track",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    thickness = DividerDefaults.Thickness,
                                    color = DividerDefaults.color
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Route,
                                        contentDescription = "Associated Track",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )

                                    Text(
                                        text = groupInfo.track.title.ifEmpty {
                                            "No track associated"
                                        },
                                        color = if (groupInfo.track.title.isEmpty()) {
                                            MaterialTheme.colorScheme.outline
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 8.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    if (groupInfo.track.title.isEmpty()) {
                                        if (currentUser == groupInfo.created_by.id) {
                                            IconButton(
                                                onClick = { showTrackSelection = true }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Add Track",
                                                    tint = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                        }
                                    } else {
                                        if (currentUser == groupInfo.created_by.id) {
                                            IconButton(
                                                onClick = { showTrackSelection = true }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Change Track",
                                                    tint = MaterialTheme.colorScheme.secondary
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = {
                                                navController.navigate(
                                                    Screen.TrackDetail.createRoute(
                                                        groupInfo.track.id.toString()
                                                    )
                                                )
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = "Track Details",
                                                tint = MaterialTheme.colorScheme.tertiary
                                            )
                                        }
                                    }
                                }
                            }
                        }


                        // Sezione membri del gruppo
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 360.dp)
                                .padding(top = 12.dp, bottom = 12.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            MembersGroup(
                                currentUserID = currentUser,
                                owner = groupInfo.created_by.id,
                                membersState = membersState,
                                viewModel = viewModel,
                                groupId = groupId,
                            )
                        }

                        // Bottone elimina gruppo
                        TextButton(
                            onClick = { showDeleteConfirmation = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                        ) {
                            if (currentUser == groupInfo.created_by.id) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    tint = MaterialTheme.colorScheme.error,
                                    contentDescription = "Delete Group",
                                    modifier = Modifier.size(ButtonDefaults.IconSize)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    contentDescription = "Leave Group",
                                    modifier = Modifier.size(ButtonDefaults.IconSize)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            if (currentUser == groupInfo.created_by.id) {
                                Text(
                                    text = "Delete Group",
                                    color = MaterialTheme.colorScheme.error,
                                )
                            } else {
                                Text(
                                    text = "Leave Group",
                                    color = MaterialTheme.colorScheme.secondary,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}