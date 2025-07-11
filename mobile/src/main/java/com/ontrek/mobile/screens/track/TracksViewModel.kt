package com.ontrek.mobile.screens.track

import android.util.Log
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

    private val _msgToast = MutableStateFlow<String>("")
    val msgToast: StateFlow<String> = _msgToast

    fun loadTracks(token: String) {
        _isLoading.value = true
        getTracks(
            onSuccess = { tracks ->
                _tracks.value = tracks ?: emptyList()
                _isLoading.value = false
            },
            onError = { errorMsg ->
                _msgToast.value = errorMsg
                _isLoading.value = false
            },
            token = token
        )
    }

    fun deleteTrack(trackId: String, token: String) {
        Log.d("TrackViewModel", "Deleting track with ID: $trackId")
        _isLoading.value = true
        deleteTrack(
            id = trackId,
            onSuccess = { _ ->
                _tracks.value = _tracks.value.filter { it.id.toString() != trackId }
                _msgToast.value = "Track deleted successfully"
            },
            onError = { errorMsg ->
                _msgToast.value = errorMsg
                Log.e("TrackViewModel", "Error deleting track: $errorMsg")
            },
            token = token
        )
        _isLoading.value = false
    }

    fun addTrack(track: Track) {
        _tracks.value = _tracks.value + track
        _msgToast.value = "Track added successfully"
    }

    fun resetMsgToast() {
        _msgToast.value = ""
    }

}