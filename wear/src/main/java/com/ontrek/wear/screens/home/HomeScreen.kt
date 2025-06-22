package com.ontrek.wear.screens.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.ontrek.wear.data.TokenViewModel
import com.ontrek.wear.screens.Screen
import com.ontrek.wear.theme.OnTrekTheme

@Composable
fun TrackSelectionScreen(navController: NavHostController, modifier: Modifier) {
    ScrollableTracksList(navController, modifier)
}


@Composable
fun ScrollableTracksList(navController: NavHostController, modifier: Modifier, tokenViewModel : TokenViewModel = viewModel(factory = TokenViewModel.Factory)) {

    val viewModel: HomeViewModel = viewModel()

    val trackList by viewModel.trackListState.observeAsState()
    val preferencesStore by tokenViewModel.uiState.collectAsState()
    val listState = rememberScalingLazyListState()
    // this because token update is asynchronous, so it could happen that a token has been provided
    // but the viewModel has not yet fetched the data
    // L'ho aggiunto io sto commento non chatGPT come quelli di Gioele <3
    if (preferencesStore.token != "undefined") viewModel.fetchData(preferencesStore.token)
    ScreenScaffold(
        scrollState = listState,
        scrollIndicator = {
            ScrollIndicator(
                state = listState,
                colors = ScrollIndicatorColors(
                    indicatorColor = MaterialTheme.colors.primary,
                    trackColor = MaterialTheme.colors.onSurfaceVariant
                ),
                modifier = modifier
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
        TrackSelectionScreen(rememberNavController(), Modifier)
    }
}
