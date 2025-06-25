package com.ontrek.wear.screens.trackselection

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyColumnDefaults
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.OutlinedButton
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.ScrollIndicator
import androidx.wear.compose.material3.ScrollIndicatorColors
import androidx.wear.compose.material3.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.ontrek.shared.data.Track
import com.ontrek.wear.screens.Screen
import com.ontrek.wear.theme.OnTrekTheme
import com.ontrek.wear.utils.components.Loading
import com.ontrek.wear.utils.samples.sampleTrackList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun TrackSelectionScreen(
    navController: NavHostController = rememberNavController(),
    trackListState: StateFlow<List<Track>>,
    fetchTrackList: (String) -> (Unit) = { _ -> },
    loadingState: StateFlow<Boolean>,
    errorState: StateFlow<String?>,
    tokenState: StateFlow<String?>,
) {
    val trackList by trackListState.collectAsStateWithLifecycle()
    val isLoading by loadingState.collectAsStateWithLifecycle()
    val error by errorState.collectAsStateWithLifecycle()
    val token by tokenState.collectAsStateWithLifecycle()
    val listState = rememberScalingLazyListState()
    // this because token update is asynchronous, so it could happen that a token has been provided
    // but the viewModel has not yet fetched the data
    // L'ho aggiunto io sto commento non chatGPT come quelli di Gioele <3
    if (!token.isNullOrEmpty()) fetchTrackList(token!!)
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


        if (token.isNullOrEmpty() || isLoading) {
            Loading(modifier = Modifier.fillMaxSize())
            Log.d(
                "TrackSelectionScreen",
                "Loading tracks with token: $token, isLoading: $isLoading"
            )
        } else if (!error.isNullOrEmpty()) {
            ErrorFetch()
        } else if (trackList.isEmpty()) {
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
            items(trackList) {
                TrackButton(it.title, navController)
            }
        }
    }
}


@Composable
fun TrackButton(trackName: String, navController: NavHostController) {
    OutlinedButton(
        onClick = {
            navController.navigate(route = Screen.TrackScreen.route + "?text=${trackName}")
        },
        modifier = Modifier
            .fillMaxWidth(0.95f),
    ) {
        Icon(
            imageVector = Icons.Default.Download,
            contentDescription = "Download track",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = trackName,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Left,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun ErrorFetch() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Error,
            contentDescription = "Error loading tracks",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.titleSmall,
            text = "Error loading tracks",
            textAlign = TextAlign.Center
        )
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

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    OnTrekTheme {
        val empty = false
        val isLoading = false
        val token = "sample_token"
        val error = ""

        TrackSelectionScreen(
            trackListState = MutableStateFlow<List<Track>>(if (empty) emptyList() else sampleTrackList),
            loadingState = MutableStateFlow<Boolean>(isLoading),
            tokenState = MutableStateFlow<String?>(token),
            errorState = MutableStateFlow<String?>(error),
        )
    }
}
