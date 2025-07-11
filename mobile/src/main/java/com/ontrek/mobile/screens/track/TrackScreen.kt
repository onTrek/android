package com.ontrek.mobile.screens.track

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ontrek.mobile.utils.components.AddTrackDialog
import com.ontrek.mobile.utils.components.BottomNavBar
import com.ontrek.mobile.utils.components.TrackItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackScreen(navController: NavHostController, token: String) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val viewModel: TrackViewModel = viewModel()
    val context = LocalContext.current

    val tracks by viewModel.tracks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val msgToast by viewModel.msgToast.collectAsState()
    var showAddTrackDialog by remember { mutableStateOf(false) }

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
                        text = "My Tracks",
                    )
                },
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = { BottomNavBar(navController) },
    ) { innerPadding ->

        LaunchedEffect(Unit) {
            viewModel.loadTracks(token)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                tracks.isEmpty() -> {
                    Text(
                        text = "Nessuna traccia disponibile",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            items(tracks) { track ->
                                TrackItem(
                                    track = track,
                                    onDelete = {
                                        viewModel.deleteTrack(track.id.toString(), token)
                                    },
                                    onItemClick = {
                                        // navController.navigate("track/${track.id}")
                                        Toast.makeText(context, "Track clicked: ${track.title}", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }

                       FloatingActionButton(
                            onClick = {
                                showAddTrackDialog = true
                                      },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Aggiungi traccia"
                            )
                        }
                    }
                }
            }
        }

        // Mostra il dialog quando richiesto
        if (showAddTrackDialog) {
            AddTrackDialog(
                onDismissRequest = { showAddTrackDialog = false },
                onTrackAdded = {
                    showAddTrackDialog = false
                    viewModel.loadTracks(token)  // Ricarica le tracce
                    Toast.makeText(context, "Traccia aggiunta con successo", Toast.LENGTH_SHORT).show()
                },
                token = token
            )
        }
    }
}