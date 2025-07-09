package com.ontrek.wear.screens.trackselection

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ontrek.shared.api.gpx.downloadGpx
import com.ontrek.shared.api.track.fetchData
import com.ontrek.shared.data.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class DownloadState {
    object NotStarted : DownloadState()
    object InProgress : DownloadState()
    object Completed : DownloadState()
    data class Error(val message: String) : DownloadState()
}

class TrackSelectionViewModel : ViewModel() {

    private val _data = MutableStateFlow<List<Track>>(listOf())
    val trackListState: StateFlow<List<Track>> = _data

    private val _downloadButtonStates = MutableStateFlow<List<DownloadState>>(listOf())
    val downloadButtonStates: StateFlow<List<DownloadState>> = _downloadButtonStates

    private val _isLoading = MutableStateFlow<Boolean>(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchTrackList(token: String) {
        Log.d("WearOS", "Fetching data with token: $token")
        _isLoading.value = true

        fetchData(
            onSuccess = ::updateData,
            onError = ::setError,
            token = token,
        )
    }

    fun updateData(data: List<Track>?) {
        Log.d("WearOS", "Data updated: $data")
        if (data != null) {
            _data.value = data
            _error.value = null
            _downloadButtonStates.value = List(data.size) { DownloadState.NotStarted }
        } else {
            Log.e("WearOS", "Data is null")
        }
        _isLoading.value = false
    }

    fun setError(error: String?) {
        Log.e("WearOS", "Error occurred: $error")
        _error.value = error
        _isLoading.value = false
    }

    private fun updateButtonState(index: Int, newState: DownloadState) {
        _downloadButtonStates.value = _downloadButtonStates.value.toMutableList().also {
            it[index] = newState
        }
    }

    fun downloadTrack(token: String, index: Int, trackID: Int, context: Context) {
        Log.d("DownloadTrack", "Dowloading GPX")

        updateButtonState(index, DownloadState.InProgress)

        viewModelScope.launch {
            downloadGpx(token = token, gpxID = trackID, onError = {
                Log.e("DownloadTrack", "Error occurred: $it")
                updateButtonState(index, DownloadState.Error(it.ifEmpty { "Unknown error" }))
            }, onSuccess = { fileContent, filename ->
                Log.d("DownloadTrack", "File downloaded successfully: $filename")
                saveFile(index, fileContent, filename, context)
            })
        }
    }

    fun saveFile(index: Int, fileContent: ByteArray, filename: String, context: Context) {
        context.openFileOutput(filename, Context.MODE_PRIVATE).use {
            it.write(fileContent)
        }
        updateButtonState(index, DownloadState.Completed)
    }

    fun unSetDownloadError(index: Int) {
        updateButtonState(index, DownloadState.NotStarted)
    }
}