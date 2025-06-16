package com.ontrecksmartwatch.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.wear.tooling.preview.devices.WearDevices
import com.ontrecksmartwatch.screens.Screen
import com.ontrecksmartwatch.utils.data.Track

@Composable
fun TrackSelectionScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        ScrollableTracksList(navController)
    }
}


@Composable
fun ScrollableTracksList(navController: NavHostController) {
    val viewModel = viewModel<HomeViewModel>()
    val trackList by viewModel.trackListState.collectAsState()
    val listState = rememberScalingLazyListState()
    ScreenScaffold(
        scrollState = listState,
        scrollIndicator = {
            ScrollIndicator(state = listState)
        }
    ) {

        val state = rememberScalingLazyListState()
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = state,
            flingBehavior = ScalingLazyColumnDefaults.snapFlingBehavior(state = state),
        ) {
            item {
                Text(
                    modifier = Modifier.padding(bottom = 10.dp),
                    color = MaterialTheme.colors.primary,
                    text = "My tracks"
                )
            }
            items(trackList) {
                TrackButton(it.getTitle(), navController)
            }
        }
    }
}

@Composable
fun TrackButton(trackName: String, navController: NavHostController) {
    val viewModel = viewModel<HomeViewModel>()
    OutlinedButton(
        onClick = {
            navController.navigate(route = Screen.TrackScreen.route + "?text=${trackName}")
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(45.dp)
    ) {
        Text(trackName)
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    TrackSelectionScreen(rememberNavController())
}
