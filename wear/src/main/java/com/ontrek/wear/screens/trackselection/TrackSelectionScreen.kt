package com.ontrek.wear.screens.trackselection

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.ontrek.shared.api.track.fetchData
import com.ontrek.wear.screens.trackselection.components.DownloadTrackButton
import com.ontrek.wear.screens.trackselection.components.TrackButton
import com.ontrek.wear.utils.components.ErrorScreen
import com.ontrek.wear.utils.components.Loading
import kotlinx.coroutines.flow.StateFlow
import java.io.File

@Composable
fun TrackSelectionScreen(
    navController: NavHostController = rememberNavController(),
    tokenState: StateFlow<String?>,
) {
    val trackSelectionViewModel = viewModel<TrackSelectionViewModel>()
    val trackList by trackSelectionViewModel.trackListState.collectAsStateWithLifecycle()
    val isLoading by trackSelectionViewModel.isLoading.collectAsStateWithLifecycle()
    val error by trackSelectionViewModel.error.collectAsStateWithLifecycle()
    val token by tokenState.collectAsStateWithLifecycle()
    val listState = rememberScalingLazyListState()

    val context = LocalContext.current

    LaunchedEffect(token) {
        if (!token.isNullOrEmpty()) trackSelectionViewModel.fetchTrackList(token!!, context)
    }

    // Scroll to top when track list updates
    LaunchedEffect(trackList) {
        if (trackList.isNotEmpty()) {
            listState.animateScrollToItem(0)
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


        if (isLoading) {
            Loading(modifier = Modifier.fillMaxSize())
        } else if (trackList.isEmpty() && error.isNullOrEmpty()) {
            EmptyList()
        } else ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            flingBehavior = ScalingLazyColumnDefaults.snapFlingBehavior(state = listState),
        ) {
            item {
                Text(
                    modifier = Modifier.padding(bottom = 15.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                    text = "My tracks"
                )
            }
            itemsIndexed(trackList) { index, track ->
                val file = File(context.filesDir, "${track.id}.gpx")
                if (file.exists()) {
                    // If the file exists, navigate to the track screen
                    TrackButton(
                        trackName = track.title,
                        trackID = track.id,
                        navController = navController,
                        index = index,
                        resetDownloadState = trackSelectionViewModel::resetDownloadState,
                        modifier = Modifier.fillMaxWidth(0.95f)
                    )
                } else {
                    DownloadTrackButton(
                        trackName = track.title,
                        trackID = track.id,
                        token = token ?: "",
                        state = track.state,
                        onDownloadClick = trackSelectionViewModel::downloadTrack,
                        unSetError = trackSelectionViewModel::resetDownloadState,
                        index = index,
                        modifier = Modifier.fillMaxWidth(0.95f),
                    )
                }
            }
            item {
                if (!error.isNullOrEmpty()) {
                    ErrorScreen(
                        "Error loading tracks",
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        token,
                        refresh = { token ->
                            trackSelectionViewModel.fetchTrackList(token, context)
                        }
                    )
                } else {
                    IconButton(
                        onClick = {
                            Log.d("TrackSelectionScreen", "Refresh tracks")
                            if (!token.isNullOrEmpty()) trackSelectionViewModel.fetchTrackList(token!!, context)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
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