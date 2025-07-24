package com.ontrek.mobile.screens.track

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ontrek.mobile.screens.Screen
import com.ontrek.mobile.utils.components.BottomNavBar
import com.ontrek.mobile.screens.track.components.AddTrackDialog
import com.ontrek.mobile.screens.track.components.TrackItem
import com.ontrek.mobile.utils.components.EmptyComponent
import com.ontrek.mobile.utils.components.ErrorViewComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackScreen(navController: NavHostController, token: String) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val viewModel: TrackViewModel = viewModel()
    val context = LocalContext.current

    val tracks by viewModel.tracksState.collectAsState()
    val msgToast by viewModel.msgToast.collectAsState()
    var showAddTrackDialog by remember { mutableStateOf(false) }
    var showFilePicker by remember { mutableStateOf(false) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val buffer = ByteArray(1024)
                    val bytesRead = inputStream.read(buffer)
                    if (bytesRead > 0) {
                        val content = String(buffer, 0, bytesRead)
                        if (content.contains("<?xml") && content.contains("<gpx")) {
                            selectedFileUri = uri
                            showAddTrackDialog = true
                        } else {
                            errorMessage = "Select a valid GPX file"
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        errorMessage = "Empty file selected or not a valid GPX file"
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                } ?: run {
                    errorMessage = "Impossible to read the file"
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                errorMessage = "Error to read the file: ${e.message}"
                Log.e("AddTrack", "Error reading file", e)
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(msgToast) {
        if (msgToast.isNotEmpty()) {
            Toast.makeText(context, msgToast, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(showFilePicker) {
        if (showFilePicker) {
            filePicker.launch("*/*")
            showFilePicker = false
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadTracks(token)
    }

    if (showAddTrackDialog) {
        AddTrackDialog(
            onDismissRequest = { showAddTrackDialog = false },
            onTrackAdded = {
                showAddTrackDialog = false
                viewModel.loadTracks(token)
                Toast.makeText(context, "Track added successfully", Toast.LENGTH_SHORT).show()
            },
            token = token,
            fileUri = selectedFileUri!!,
        )
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showFilePicker = true
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Track",
                )
            }
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = tracks is TrackViewModel.TracksState.Loading,
            onRefresh = {
                viewModel.loadTracks(token)
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val currentState = tracks) {
                is TrackViewModel.TracksState.Loading -> {
                    CircularProgressIndicator()
                }
                is TrackViewModel.TracksState.Success -> {
                    if (currentState.tracks.isEmpty()) {
                        EmptyComponent(
                            title = "No Tracks Found",
                            description = "You haven't added any tracks yet.",
                            icon = Icons.Default.Terrain
                        )
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        items(currentState.tracks) { track ->
                            TrackItem(
                                track = track,
                                onItemClick = {
                                    navController.navigate(Screen.TrackDetail.createRoute(track.id.toString()))
                                }
                            )
                        }
                    }
                }
                is TrackViewModel.TracksState.Error -> {
                    ErrorViewComponent(
                        errorMsg = currentState.message
                    )
                }
            }
        }
    }
}