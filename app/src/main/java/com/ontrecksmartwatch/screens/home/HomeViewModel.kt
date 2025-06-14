package com.ontrecksmartwatch.screens.home

import androidx.lifecycle.ViewModel
import com.ontrecksmartwatch.utils.data.Track
import kotlinx.coroutines.flow.MutableStateFlow

class HomeViewModel: ViewModel() {
    val trackListState = MutableStateFlow<List<Track>>(
        listOf(Track("1", "Track 1"),
            Track("2", "Track 2"),
            Track("3", "Track 3"),
            Track("4", "Track 4"),
            Track("5", "Track 5")
        )
    )

    fun addTrack(track: Track) {
        trackListState.value = trackListState.value + track
    }
}