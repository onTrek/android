package com.ontrecksmartwatch.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyColumnDefaults
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.OutlinedButton
import androidx.wear.compose.material.Text
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.ScrollIndicator
import androidx.wear.tooling.preview.devices.WearDevices
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ontrecksmartwatch.utils.data.Track
import androidx.wear.compose.foundation.lazy.items

@Composable
fun HomeScreen() {
    Column (
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
    ) {
        UpperTitle()
        ScrollableTracksList()
    }
}


@Composable
fun ScrollableTracksList() {
    val viewModel = viewModel<HomeViewModel>()
    val trackList = viewModel.trackListState.collectAsState()
    val listState = rememberScalingLazyListState()
    ScreenScaffold(
        scrollState = listState,
        scrollIndicator = {
            ScrollIndicator(state = listState)
        }
    ) {

        val state = rememberScalingLazyListState()
        ScalingLazyColumn(
            modifier = Modifier.fillMaxWidth(),
            state = state,
            flingBehavior = ScalingLazyColumnDefaults.snapFlingBehavior(state = state),

        ) {
            items(trackList.value) {
                TrackButton(it.getTitle())
            }
            item { TrackButton("Add one") }
        }
    }
}

@Composable
fun TrackButton(trackName: String) {
    val viewModel = viewModel<HomeViewModel>()
    OutlinedButton(
        onClick = { viewModel.addTrack(Track("6","Aggiunto")) },
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(trackName)
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    HomeScreen()
}

@Composable
fun UpperTitle() {
    Text(
        modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = "My tracks"
    )
}
