package com.ontrek.mobile.screens.track

import androidx.lifecycle.ViewModel
import com.ontrek.shared.api.track.getTracks
import com.ontrek.shared.api.track.deleteTrack
import com.ontrek.shared.data.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TrackViewModel : ViewModel() {
    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _msgToast = MutableStateFlow<String>("")
    val msgToast: StateFlow<String> = _msgToast

    fun loadTracks(token: String) {
        _isLoading.value = true
        _error.value = null

        getTracks(
            onSuccess = { tracks ->
                _tracks.value = tracks ?: emptyList()
                _isLoading.value = false
            },
            onError = { errorMsg ->
                _error.value = errorMsg
                _isLoading.value = false
            },
            token = token
        )
    }

    fun deleteTrack(trackId: String, token: String) {
        deleteTrack(
            id = trackId,
            onSuccess = { _ ->
                _tracks.value = _tracks.value.filter { it.id.toString() != trackId }
            },
            onError = { errorMsg ->
                _error.value = errorMsg
            },
            token = token
        )
    }

    fun resetMsgToast() {
        _msgToast.value = ""
    }

}