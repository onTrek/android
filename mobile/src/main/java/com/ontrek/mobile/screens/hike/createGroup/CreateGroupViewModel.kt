package com.ontrek.mobile.screens.hike.createGroup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ontrek.shared.api.hikes.createGroup
import com.ontrek.shared.api.track.getTracks
import com.ontrek.shared.data.Track
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CreateGroupViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks

    private val _selectedTrackId = MutableStateFlow<Int?>(null)
    val selectedTrackId: StateFlow<Int?> = _selectedTrackId

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description

    private val _msgToast = MutableStateFlow("")
    val msgToast: StateFlow<String> = _msgToast

    fun loadTracks(token: String) {
        _isLoading.value = true
        viewModelScope.launch {
            getTracks(
                onSuccess = { tracksList ->
                    _tracks.value = tracksList ?: emptyList()
                    _isLoading.value = false
                },
                onError = { error ->
                    _msgToast.value = error
                    _isLoading.value = false
                },
                token = token
            )
        }
    }

    fun setDescription(description: String) {
        _description.value = description
    }

    fun setSelectedTrack(trackId: Int) {
        _selectedTrackId.value = trackId
    }

    fun createGroup(token: String, onSuccess: () -> Unit) {
        if (_description.value.isBlank()) {
            _msgToast.value = "Inserisci una descrizione"
            return
        }

        if (_selectedTrackId.value == null) {
            _msgToast.value = "Seleziona una traccia"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            delay(300)
        }
    }

    fun resetMsgToast() {
        _msgToast.value = ""
    }
}