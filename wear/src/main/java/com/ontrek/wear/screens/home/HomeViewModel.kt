package com.ontrek.wear.screens.home

import androidx.lifecycle.ViewModel
import com.ontrek.wear.utils.data.Track
import kotlinx.coroutines.flow.MutableStateFlow

class HomeViewModel: ViewModel() {
    val trackListState = MutableStateFlow<List<Track>>(
        listOf(Track("1", "Track 1"),
            Track("2", "Track 2"),
            Track("3", "Track 3"),
            Track("4", "Track 4"),
            Track("5", "Track 5"),
            Track("6", "Track 6"),
            Track("7", "Track 7"),
            Track("8", "Track 8"),
            Track("9", "Track 9"),
            Track("10", "Track 10"),
            Track("11", "Track 11"),
            Track("12", "Track 12"),

        )
    )

    fun addTrack(track: Track) {
        trackListState.value = trackListState.value + track
    }
}