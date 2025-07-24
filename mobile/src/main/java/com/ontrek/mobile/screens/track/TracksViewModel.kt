package com.ontrek.mobile.screens.track

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ontrek.shared.api.track.getTracks
import com.ontrek.shared.data.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TrackViewModel : ViewModel() {
    private val _tracksState = MutableStateFlow<TracksState>(TracksState.Loading)
    val tracksState: StateFlow<TracksState> = _tracksState
    private val _msgToast = MutableStateFlow("")
    val msgToast: StateFlow<String> = _msgToast

    fun loadTracks(token: String) {
        _tracksState.value = TracksState.Loading
        viewModelScope.launch {
            getTracks(
                onSuccess = { tracks ->
                    if (tracks != null && tracks.isNotEmpty()) {
                        _tracksState.value = TracksState.Success(tracks)
                    } else {
                        _tracksState.value = TracksState.Success(emptyList())
                    }
                },
                onError = { errorMsg ->
                    _msgToast.value = errorMsg
                },
                token = token
            )
        }
    }

    fun clearMsgToast() {
        _msgToast.value = ""
    }

    sealed class TracksState {
        data class Success(val tracks: List<Track>) : TracksState()
        data class Error(val message: String) : TracksState()
        object Loading : TracksState()
    }
}