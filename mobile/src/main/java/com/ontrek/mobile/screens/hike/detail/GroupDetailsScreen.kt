package com.ontrek.mobile.screens.hike.detail

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.ontrek.mobile.screens.Screen
import com.ontrek.mobile.utils.components.BottomNavBar
import com.ontrek.mobile.utils.components.DeleteConfirmationDialog
import com.ontrek.mobile.utils.components.ErrorViewComponent
import com.ontrek.mobile.utils.components.InfoCardRow
import com.ontrek.mobile.utils.components.hikesComponents.TrackSelectionDialog
import com.ontrek.mobile.utils.components.hikesComponents.groupDetailsComponent.MembersGroup
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailsScreen(
    groupId: Int,
    navController: NavHostController,
    token: String
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

    LaunchedEffect(groupId) {
        viewModel.loadGroupDetails(groupId, token)
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
                   ErrorViewComponent(
                        errorMsg = (groupState as GroupDetailsViewModel.GroupState.Error).message
                   )
                }
                is GroupDetailsViewModel.GroupState.Success -> {
                    val groupInfo = (groupState as GroupDetailsViewModel.GroupState.Success).groupInfo

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                    ) {
                        // Dialoghi di conferma
                        if (showDeleteConfirmation) {
                            DeleteConfirmationDialog(
                                title = "Delete Group",
                                onDismiss = { showDeleteConfirmation = false },
                                onConfirm = {
                                    viewModel.deleteGroup(
                                        groupId = groupId,
                                        token = token,
                                        onSuccess = {
                                            navController.navigateUp()
                                        }
                                    )
                                }
                            )
                        }

                        // Sezione informazioni generali
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            shape = MaterialTheme.shapes.medium,
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
                                    value = formatDate(groupInfo.created_at),
                                )
                            }

                            // Sezione track
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
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
                                        imageVector = Icons.Default.Terrain,
                                        contentDescription = "Associated Track",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )

                                    Text(
                                        text = if (groupInfo.track != null) {
                                            groupInfo.track.title
                                        } else {
                                            "No track associated"
                                        },
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(start = 8.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    IconButton(
                                        onClick = { showTrackSelection = true }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Change Track",
                                            tint = MaterialTheme.colorScheme.secondary
                                        )
                                    }

                                    if (showTrackSelection) {
                                        TrackSelectionDialog(
                                            tracks = tracks,
                                            onDismiss = { showTrackSelection = false },
                                            onTrackSelected = { track ->
                                                viewModel.changeTrack(groupId, track.id, token)
                                                showTrackSelection = false
                                            },
                                            oldTrack = groupInfo.track.id
                                        )
                                    }

                                    IconButton(
                                        onClick = { navController.navigate(Screen.TrackDetail.createRoute(groupInfo.track.id.toString()))}
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


                        // Sezione membri del gruppo
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 360.dp)
                                .padding(top = 12.dp, bottom = 12.dp),
                            shape = MaterialTheme.shapes.medium,
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            MembersGroup(
                                membersState = membersState,
                                token = token,
                                creatorGroupMember = groupInfo.created_by.username,
                                viewModel = viewModel,
                                groupId = groupId,
                            )
                        }

                        // Bottone elimina gruppo
                        Button(
                            onClick = { showDeleteConfirmation = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )

                            Text(
                                text = "Delete Group",
                                color = MaterialTheme.colorScheme.onError
                            )
                        }
                    }
                }
            }
        }
    }
}
private fun formatDate(dateString: String): String {
    return try {
        val instant = Instant.parse(dateString)
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault()).format(instant)
    } catch (e: Exception) {
        dateString
    }
}