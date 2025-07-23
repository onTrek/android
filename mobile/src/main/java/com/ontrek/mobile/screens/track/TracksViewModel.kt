package com.ontrek.mobile.screens.track

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ontrek.shared.api.track.getTracks
import com.ontrek.shared.data.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TrackViewModel : ViewModel() {
    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _msgToast = MutableStateFlow("")
    val msgToast: StateFlow<String> = _msgToast

    fun loadTracks() {
        _isLoading.value = true
        viewModelScope.launch {
            getTracks(
                onSuccess = { tracks ->
                    _tracks.value = tracks ?: emptyList()
                    _isLoading.value = false
                },
                onError = { errorMsg ->
                    _msgToast.value = errorMsg
                    _isLoading.value = false
                },
            )
        }

    }

    fun resetMsgToast() {
        _msgToast.value = ""
    }

}