package com.ontrek.wear.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyColumnDefaults
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.OutlinedButton
import androidx.wear.compose.material.Text
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.ScrollIndicator
import androidx.wear.compose.material3.ScrollIndicatorColors
import androidx.wear.tooling.preview.devices.WearDevices
import com.ontrek.shared.data.Track
import com.ontrek.wear.data.PreferencesViewModel
import com.ontrek.wear.screens.Screen
import com.ontrek.wear.theme.OnTrekTheme
import com.ontrek.wear.utils.components.Loading
import com.ontrek.wear.utils.samples.trackList

@Composable
fun TrackSelectionScreen(
    navController: NavHostController,
    trackListState: LiveData<List<Track>>,
    fetchTrackList: (String) -> (Unit),
    preferencesViewModel : PreferencesViewModel = viewModel(factory = PreferencesViewModel.Factory)
) {
    val trackList by trackListState.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(initial = false)
    val token by preferencesViewModel.tokenState.collectAsState()
    val listState = rememberScalingLazyListState()
    // this because token update is asynchronous, so it could happen that a token has been provided
    // but the viewModel has not yet fetched the data
    // L'ho aggiunto io sto commento non chatGPT come quelli di Gioele <3
    if (!token.isNullOrEmpty()) fetchTrackList(token ?: "")
    ScreenScaffold(
        scrollState = listState,
        scrollIndicator = {
            ScrollIndicator(
                state = listState,
                colors = ScrollIndicatorColors(
                    indicatorColor = MaterialTheme.colors.primary,
                    trackColor = MaterialTheme.colors.onSurfaceVariant
                ),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(start = 8.dp)
            )
        }
    ) {
        // TODO: add a loading, error and empty states

        if (!isLoading && trackList.isNullOrEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentHeight()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    modifier = Modifier.padding(bottom = 8.dp),
                    imageVector = Icons.Filled.Warning,
                    contentDescription = "No tracks available",
                    tint = MaterialTheme.colors.primary
                )
                Text(
                    modifier = Modifier.padding(bottom = 15.dp),
                    color = MaterialTheme.colors.primary,
                    style = MaterialTheme.typography.title3,
                    text = "No tracks available",
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Please upload them from your smartphone.",
                    color = MaterialTheme.colors.secondary,
                    style = MaterialTheme.typography.body2,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                flingBehavior = ScalingLazyColumnDefaults.snapFlingBehavior(state = listState),
            ) {
                item {
                    Text(
                        modifier = Modifier.padding(bottom = 15.dp),
                        color = MaterialTheme.colors.primary,
                        style = MaterialTheme.typography.title2,
                        text = "My tracks"
                    )
                }
                if (token.isNullOrEmpty() || isLoading) {
                    item {
                        Loading(modifier = Modifier.fillMaxSize())
                    }
                } else
                items(trackList ?: emptyList()) {
                    TrackButton(it.title, navController)
                }
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
            .fillMaxWidth(0.95f)
    ) {
        Text(
            text = trackName,
            style = MaterialTheme.typography.body1,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    OnTrekTheme {
        val navController = rememberNavController()
        val sampleTracks = MutableLiveData<List<Track>>().apply {
            value = trackList
        }
        TrackSelectionScreen(
            navController, sampleTracks,
            fetchTrackList = TODO(),
            preferencesViewModel = TODO()
        )
    }
}
