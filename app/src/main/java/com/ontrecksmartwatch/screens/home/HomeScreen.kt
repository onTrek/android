package com.ontrecksmartwatch.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
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
import com.ontrecksmartwatch.theme.OnTrekSmartwatchTheme

@Composable
fun WearApp() {
    OnTrekSmartwatchTheme {
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(8.dp)
        ) {
            UpperTitle()
            ScrollableTracksList()
        }
    }
}


@Composable
fun ScrollableTracksList() {
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
            flingBehavior = ScalingLazyColumnDefaults.snapFlingBehavior(state = state)
        ) {
            items(20) { index ->
                TrackButton(index.toString())
            }
        }
    }
}

fun openTrack() {
    // This function is intentionally left empty.
    // It can be used to add any additional functionality in the future.
}

@Composable
fun TrackButton(trackName: String) {
    OutlinedButton(
        onClick = { openTrack() },
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(trackName)
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp()
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
