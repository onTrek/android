package com.ontrek.mobile.screens.hike.groupDetails

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ontrek.mobile.screens.Screen
import com.ontrek.mobile.screens.hike.HikesViewModel
import com.ontrek.mobile.screens.track.detail.TrackDetailViewModel
import com.ontrek.mobile.utils.components.BottomNavBar
import com.ontrek.mobile.utils.components.DeleteConfirmationDialog
import com.ontrek.mobile.utils.components.ErrorViewComponent
import com.ontrek.mobile.utils.components.TitleGeneric
import com.ontrek.mobile.utils.components.trackComponents.TitleTrack
import com.ontrek.shared.data.MemberInfo
import com.ontrek.shared.data.Track
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
    val viewModel: GroupDetailsViewModel = viewModel()
    val groupState by viewModel.groupState.collectAsState()
    val membersState by viewModel.membersState.collectAsState()
    val tracks by viewModel.tracks.collectAsState()
    val msgToast by viewModel.msgToast.collectAsState()

    val context = LocalContext.current

    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showTrackSelection by remember { mutableStateOf(false) }
    var showAddMemberDialog by remember { mutableStateOf(false) }

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
        topBar = {
            TopAppBar(
                title = {
                    TitleGeneric(
                        title = when (groupState) {
                            is GroupDetailsViewModel.GroupState.Success -> (groupState as GroupDetailsViewModel.GroupState.Success).groupInfo.description
                            is GroupDetailsViewModel.GroupState.Loading -> "Caricamento..."
                            else -> "Group Details"
                        },
                        modifier = Modifier.fillMaxWidth(0.8f) // Occupa il 80% della larghezza
                    )
                },
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
                            .padding( horizontal = 16.dp, vertical = 8.dp )
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
                                .padding(vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "General Information",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                TitleGeneric(
                                    title = groupInfo.description,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Creato da: ${groupInfo.created_by.username}",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Data creazione: ${formatDate(groupInfo.created_at)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        // Sezione traccia
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
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
                                        text = "Traccia Associata",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Button(onClick = { showTrackSelection = true }) {
                                        Text("Cambia")
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                if (showTrackSelection) {
                                    TrackSelectionDialog(
                                        tracks = tracks,
                                        onDismiss = { showTrackSelection = false },
                                        onTrackSelected = { track ->
                                            viewModel.changeTrack(groupId, track.id, token)
                                            showTrackSelection = false
                                        }
                                    )
                                }

                                Button(
                                    onClick = { navController.navigate("track_detail/${groupId}") },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Visualizza Dettagli Traccia")
                                }
                            }
                        }

                        // Sezione membri
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
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
                                        text = "Membri del Gruppo",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Button(onClick = { showAddMemberDialog = true }) {
                                        Text("Aggiungi")
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                if (membersState.isEmpty()) {
                                    Text(
                                        text = "Nessun membro nel gruppo",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                } else {
                                    Column {
                                        membersState.forEach { member ->
                                            MemberItem(
                                                member = member,
                                                onRemoveClick = {
                                                    viewModel.removeMember(groupId, member.user.id, token)
                                                }
                                            )
                                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                                        }
                                    }
                                }
                            }
                        }

                        // Bottone elimina gruppo
                        Button(
                            onClick = { showDeleteConfirmation = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text(
                                text = "Elimina Gruppo",
                                color = MaterialTheme.colorScheme.onError
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MemberItem(
    member: MemberInfo,
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
            text = member.user.username,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onRemoveClick) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Rimuovi",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun TrackSelectionDialog(
    tracks: List<Track>,
    onDismiss: () -> Unit,
    onTrackSelected: (Track) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleziona una traccia") },
        text = {
            if (tracks.isEmpty()) {
                Text("Nessuna traccia disponibile")
            } else {
                LazyColumn(
                    modifier = Modifier.height(300.dp)
                ) {
                    items(tracks) { track ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable { onTrackSelected(track) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Route,
                                contentDescription = "Traccia",
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = track.title,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        HorizontalDivider(
                            Modifier,
                            DividerDefaults.Thickness,
                            DividerDefaults.color
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

private fun formatDate(dateString: String): String {
    return try {
        val instant = Instant.parse(dateString)
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault()).format(instant)
    } catch (e: Exception) {
        dateString
    }
}