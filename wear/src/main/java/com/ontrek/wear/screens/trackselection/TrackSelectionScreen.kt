package com.ontrek.wear.screens.trackselection

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyColumnDefaults
import androidx.wear.compose.foundation.lazy.itemsIndexed
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.ScrollIndicator
import androidx.wear.compose.material3.ScrollIndicatorColors
import androidx.wear.compose.material3.Text
import com.ontrek.wear.data.DatabaseProvider
import com.ontrek.wear.screens.trackselection.components.TrackButton
import com.ontrek.wear.utils.components.Loading
import kotlinx.coroutines.flow.StateFlow

@Composable
fun TrackSelectionScreen(
    navController: NavHostController = rememberNavController(),
) {
    val trackSelectionViewModel = viewModel<TrackSelectionViewModel>(
        factory = TrackSelectionViewModel.Factory(DatabaseProvider.getDatabase(LocalContext.current.applicationContext))
    )
    val downloadedTracks by trackSelectionViewModel.downloadedTrackListState.collectAsStateWithLifecycle()
    val availableTracks by trackSelectionViewModel.availableTrackListState.collectAsStateWithLifecycle()
    val isLoadingTracks by trackSelectionViewModel.isLoadingTracks.collectAsStateWithLifecycle()
    val isLoadingDownloads by trackSelectionViewModel.isLoadingDownloads.collectAsStateWithLifecycle()
    val fetchError by trackSelectionViewModel.fetchError.collectAsStateWithLifecycle()
    val downloadError by trackSelectionViewModel.downloadError.collectAsStateWithLifecycle()
    val updateSuccess by trackSelectionViewModel.updateSuccess.collectAsStateWithLifecycle()
    val downloadSuccess by trackSelectionViewModel.downloadSuccess.collectAsStateWithLifecycle()
    var refresh by remember { mutableStateOf(false) }
    val listState = rememberScalingLazyListState()
    val charge by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(charge) {
        trackSelectionViewModel.fetchTrackList()
    }

    // Reset scroll position when download is successful
    LaunchedEffect(downloadSuccess) {
        if (downloadSuccess) {
            listState.scrollToItem(0)
            trackSelectionViewModel.resetDownloadSuccess()
        }
    }

    // Reset scroll position when update is successful
    LaunchedEffect(updateSuccess) {
        if (updateSuccess && refresh) {
            listState.animateScrollToItem(downloadedTracks.size + 2)
            trackSelectionViewModel.resetUpdateSuccess()
        } else if (updateSuccess) {
            trackSelectionViewModel.resetUpdateSuccess()
            refresh = true
        }
    }

    // show toast message if there is a download error
    LaunchedEffect(downloadError) {
        if (!downloadError.isNullOrEmpty()) {
            Toast.makeText(context, downloadError, Toast.LENGTH_LONG).show()
            trackSelectionViewModel.resetDownloadError()
        }
    }

    ScreenScaffold(
        scrollState = listState,
        scrollIndicator = {
            ScrollIndicator(
                state = listState,
                colors = ScrollIndicatorColors(
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(start = 8.dp)
            )
        }
    ) {


        if (isLoadingDownloads) {
            Loading(modifier = Modifier.fillMaxSize())
        } else if (downloadedTracks.isEmpty() && availableTracks.isEmpty() && !isLoadingTracks && fetchError.isNullOrEmpty()) {
            EmptyList()
        } else ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            flingBehavior = ScalingLazyColumnDefaults.snapFlingBehavior(state = listState),
        ) {
            if (downloadedTracks.isNotEmpty()) {
                item {
                    Text(
                        modifier = Modifier.padding(top = 15.dp, bottom = 8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium,
                        text = "Offline tracks"
                    )
                }
                itemsIndexed(downloadedTracks) { index, track ->
                    TrackButton(
                        track = track,
                        navController = navController,
                        index = index,
                        deleteTrack = trackSelectionViewModel::deleteTrack,
                        onDownloadClick = trackSelectionViewModel::downloadTrack,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            item {
                Text(
                    modifier = Modifier.padding(top = 15.dp, bottom = 8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                    text = "Available tracks"
                )
            }
            if (availableTracks.isEmpty() && !isLoadingTracks && fetchError.isNullOrEmpty()) {
                item {
                    Text(
                        text = "All your tracks are downloaded.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                itemsIndexed(availableTracks) { index, track ->
                    TrackButton(
                        track = track,
                        navController = navController,
                        index = index,
                        deleteTrack = trackSelectionViewModel::deleteTrack,
                        onDownloadClick = trackSelectionViewModel::downloadTrack,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            if (!fetchError.isNullOrEmpty() && !isLoadingTracks) {
                item {

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CloudOff,
                            contentDescription = "Error loading tracks.",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleSmall,
                            text = "Error loading tracks.",
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            item {
                if (isLoadingTracks) {
                    Loading(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    )
                } else {
                    IconButton(
                        onClick = {
                            Log.d("TrackSelectionScreen", "Refresh tracks")
                            trackSelectionViewModel.fetchTrackList()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Refresh tracks",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                }
            }
        }
    }
}

@Composable
fun EmptyList() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Warning,
            contentDescription = "No tracks available",
            tint = MaterialTheme.colorScheme.error
        )
        Text(
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.titleSmall,
            text = "No tracks available",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(8.dp)
        )
        Text(
            text = "Please upload them from your smartphone.",
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(8.dp)
        )
    }
}

//@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
//@Composable
//fun DefaultPreview() {
//    OnTrekTheme {
//        val empty = false
//        val isLoading = false
//        val token = "sample_token"
//        val error = ""
//
//        TrackSelectionScreen(
//            trackListState = MutableStateFlow<List<Track>>(if (empty) emptyList() else sampleTrackList),
//            loadingState = MutableStateFlow<Boolean>(isLoading),
//            tokenState = MutableStateFlow<String?>(token),
//            errorState = MutableStateFlow<String?>(error)
//        )
//    }
//}