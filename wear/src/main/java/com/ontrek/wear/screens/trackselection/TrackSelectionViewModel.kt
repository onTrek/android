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
import java.io.File
import kotlin.collections.sortedWith

sealed class DownloadState {
    object NotStarted : DownloadState()
    object InProgress : DownloadState()
    object Completed : DownloadState()
    data class Error(val message: String) : DownloadState()
}

data class TrackButtonUI (
    val id: Int,
    val title: String,
    val filename: String = "$id.gpx",
    val uploadedAt: Long,
    val size: Double,  // TODO: Change to Long if size is always in bytes
    var downloadedAt: Long? = null,
    var state: DownloadState,
)

class TrackSelectionViewModel : ViewModel() {

    private val _trackListState = MutableStateFlow<List<TrackButtonUI>>(listOf())
    val trackListState: StateFlow<List<TrackButtonUI>> = _trackListState

    private val _isLoading = MutableStateFlow<Boolean>(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchTrackList(token: String, context: Context) {
        Log.d("WearOS", "Fetching data with token: $token")
        _isLoading.value = true

        fetchData(
            onSuccess = { data ->
                updateTracks(data, context)
            },
            onError = ::setError,
            token = token,
        )
    }

    fun updateTracks(data: List<Track>?, context: Context) {
        Log.d("WearOS", "Data updated: $data")
        if (data != null) {
            _trackListState.value = data.map { track ->
                val file = File(context.filesDir, "${track.id}.gpx")
                TrackButtonUI(
                    id = track.id,
                    title = track.title,
                    uploadedAt = java.time.OffsetDateTime.parse(track.upload_date).toInstant().toEpochMilli(),
                    size = track.size,
                    state = if (file.exists()) DownloadState.Completed else DownloadState.NotStarted,
                )
            }.sorted()
            _error.value = null
        } else {
            Log.e("WearOS", "Data is null")
        }
        _isLoading.value = false
    }

    private fun List<TrackButtonUI>.sorted(): List<TrackButtonUI> {
        return this.sortedWith { a, b ->
            when {
                a.state is DownloadState.Completed && b.state !is DownloadState.Completed -> -1
                a.state !is DownloadState.Completed && b.state is DownloadState.Completed -> 1
                else -> when {
                    a.downloadedAt == null && b.downloadedAt != null -> 1
                    a.downloadedAt != null && b.downloadedAt == null -> -1
                    a.downloadedAt != null && b.downloadedAt != null -> {
                        if (a.downloadedAt!! > b.downloadedAt!!) -1 else 1
                    }
                    else -> {
                        if (a.uploadedAt > b.uploadedAt) -1 else 1
                    }
                }
            }
        }
    }

    fun setError(error: String?) {
        Log.e("WearOS", "Error occurred: $error")
        _error.value = error
        _isLoading.value = false
    }

    private fun updateButtonState(index: Int, newState: DownloadState) {
        _trackListState.value = _trackListState.value.toMutableList().also {
            it[index].state = newState
        }
    }

    fun downloadTrack(token: String, index: Int, trackID: Int, context: Context) {
        Log.d("DownloadTrack", "Downloading GPX")

        updateButtonState(index, DownloadState.InProgress)

        viewModelScope.launch {
            downloadGpx(token = token, gpxID = trackID, onError = {
                Log.e("DownloadTrack", "Error occurred: $it")
                updateButtonState(index, DownloadState.Error(it.ifEmpty { "Unknown error" }))
            }, onSuccess = { fileContent, filename ->
                Log.d("DownloadTrack", "File downloaded successfully: $filename")
                saveFile(fileContent, filename, context)
                _trackListState.value[index].downloadedAt = System.currentTimeMillis()
                updateButtonState(index, DownloadState.Completed)
                _trackListState.value = _trackListState.value.sorted() // Re-sort the list after download
            })
        }
    }

    fun saveFile(fileContent: ByteArray, filename: String, context: Context) {
        context.openFileOutput(filename, Context.MODE_PRIVATE).use {
            it.write(fileContent)
        }
    }

    fun resetDownloadState(index: Int) {
        updateButtonState(index, DownloadState.NotStarted)
    }
}