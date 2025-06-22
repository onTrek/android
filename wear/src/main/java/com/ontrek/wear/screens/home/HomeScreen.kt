package com.ontrek.wear.screens.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyColumnDefaults
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.OutlinedButton
import androidx.wear.compose.material.Text
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.ScrollIndicator
import androidx.wear.compose.material3.ScrollIndicatorColors
import androidx.wear.tooling.preview.devices.WearDevices
import com.ontrek.wear.screens.Screen
import com.ontrek.wear.theme.OnTrekTheme

@Composable
fun TrackSelectionScreen(navController: NavHostController, trackListState: LiveData<List<Track>>) {
    val trackList by trackListState.observeAsState()
    val listState = rememberScalingLazyListState()
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

        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            flingBehavior = ScalingLazyColumnDefaults.snapFlingBehavior(state = listState),
        ) {
            item {
                Text(
                    modifier = Modifier.padding(bottom = 15.dp),
                    color = MaterialTheme.colors.primary,
                    text = "My tracks"
                )
            }
            items(trackList ?: emptyList()) {
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
            .fillMaxWidth(0.95f)
    ) {
        Text(trackName)
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    OnTrekTheme {
        val navController = rememberNavController()
        val sampleTracks = MutableLiveData<List<Track>>().apply {
            value = listOf(
                Track(
                    id = 1,
                    filename = "track1.gpx",
                    stats = TrackStats(
                        km = 5.2f,
                        duration = "00:45:00",
                        ascent = 120.0,
                        descent = 110.0,
                        max_altitude = 450,
                        min_altitude = 320
                    ),
                    title = "Morning Hike",
                    upload_date = "2024-06-01"
                ),
                Track(
                    id = 3,
                    filename = "track3.gpx",
                    stats = TrackStats(
                        km = 8.7f,
                        duration = "01:20:00",
                        ascent = 200.0,
                        descent = 195.0,
                        max_altitude = 600,
                        min_altitude = 400
                    ),
                    title = "Mount Everest Expedition",
                    upload_date = "2024-06-02"
                ),
                Track(
                    id = 2,
                    filename = "track2.gpx",
                    stats = TrackStats(
                        km = 8.7f,
                        duration = "01:20:00",
                        ascent = 200.0,
                        descent = 195.0,
                        max_altitude = 600,
                        min_altitude = 400
                    ),
                    title = "Evening Trail",
                    upload_date = "2024-06-02"
                )
            )
        }
        TrackSelectionScreen(navController, sampleTracks)
    }
}
