package com.ontrek.mobile.screens.hike.createGroup

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ontrek.mobile.utils.components.BottomNavBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupScreen(
    navController: NavHostController,
    token: String
) {
    val viewModel: CreateGroupViewModel = viewModel()
    val context = LocalContext.current

    val isLoading by viewModel.isLoading.collectAsState()
    val tracks by viewModel.tracks.collectAsState()
    val selectedTrackId by viewModel.selectedTrackId.collectAsState()
    val description by viewModel.description.collectAsState()
    val msgToast by viewModel.msgToast.collectAsState()

    var showTrackSelection by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
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
                title = { Text("Crea Nuovo Gruppo") },
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
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Campo descrizione
                    OutlinedTextField(
                        value = description,
                        onValueChange = { viewModel.setDescription(it) },
                        label = { Text("Descrizione del Gruppo") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    // Selezione traccia
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTrackSelection = true },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Route,
                                contentDescription = "Traccia",
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = selectedTrackId?.let { id ->
                                    tracks.find { it.id == id }?.title ?: "Seleziona una traccia"
                                } ?: "Seleziona una traccia",
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Mostra dialog di selezione traccia
                    if (showTrackSelection) {
                        TrackSelectionDialog(
                            tracks = tracks,
                            onDismiss = { showTrackSelection = false },
                            onTrackSelected = { track ->
                                viewModel.setSelectedTrack(track.id)
                                showTrackSelection = false
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Bottone crea
                    Button(
                        onClick = {
                            viewModel.createGroup(token) {
                                navController.navigateUp()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = description.isNotBlank() && selectedTrackId != null
                    ) {
                        Text("Crea Gruppo")
                    }
                }
            }
        }
    }
}

@Composable
fun TrackSelectionDialog(
    tracks: List<com.ontrek.shared.data.Track>,
    onDismiss: () -> Unit,
    onTrackSelected: (com.ontrek.shared.data.Track) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleziona una traccia") },
        text = {
            if (tracks.isEmpty()) {
                Text("Nessuna traccia disponibile")
            } else {
                Column(
                    modifier = Modifier
                        .height(300.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    tracks.forEach { track ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTrackSelected(track) }
                                .padding(vertical = 8.dp),
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
                        Divider()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}