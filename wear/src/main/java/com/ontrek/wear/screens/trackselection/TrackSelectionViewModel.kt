package com.ontrek.wear.screens.trackselection

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ontrek.shared.api.gpx.downloadGpx
import com.ontrek.shared.api.track.getTracks
import com.ontrek.shared.data.Track
import com.ontrek.wear.data.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed class DownloadState {
    object NotStarted : DownloadState()
    object InProgress : DownloadState()
    object Completed : DownloadState()
}

data class TrackUI(
    val id: Int,
    val title: String,
    val filename: String = "$id.gpx",
    val uploadedAt: Long,
    val size: Long,  // size in Bytes
    var state: DownloadState,
) {
    val sizeInKB: Long
        get() = size / 1024

    val sizeInMB: Double
        get() = size / (1024.0 * 1024.0)

    fun getFormattedSize(): String {
        return when {
            sizeInMB >= 1 -> "$sizeInMB MB"
            sizeInKB >= 1 -> "$sizeInKB KB"
            else -> "$size Bytes"
        }
    }
}

class TrackSelectionViewModel(private val db: AppDatabase) : ViewModel() {

    private val _downloadedTrackListState = MutableStateFlow<List<TrackUI>>(listOf())
    val downloadedTrackListState: StateFlow<List<TrackUI>> = _downloadedTrackListState

    private val _availableTrackListState = MutableStateFlow<List<TrackUI>>(listOf())
    val availableTrackListState: StateFlow<List<TrackUI>> = _availableTrackListState

    private val _isLoadingTracks = MutableStateFlow<Boolean>(true)
    val isLoadingTracks: StateFlow<Boolean> = _isLoadingTracks

    private val _fetchError = MutableStateFlow<String?>(null)
    val fetchError: StateFlow<String?> = _fetchError

    private val _downloadError = MutableStateFlow<String?>(null)
    val downloadError: StateFlow<String?> = _downloadError

    private val _updateSuccess = MutableStateFlow<Boolean>(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess

    private val _downloadSuccess = MutableStateFlow<Boolean>(false)
    val downloadSuccess: StateFlow<Boolean> = _downloadSuccess

    private val _isLoadingDownloads = MutableStateFlow<Boolean>(true)
    val isLoadingDownloads: StateFlow<Boolean> = _isLoadingDownloads

    init {
        loadDownloadedTracks()
    }

    fun resetUpdateSuccess() {
        _updateSuccess.value = false
    }

    fun resetDownloadSuccess() {
        _downloadSuccess.value = false
    }

    private fun loadDownloadedTracks() {
        _isLoadingDownloads.value = true

        viewModelScope.launch {
            try {
                val tracks = db.trackDao().getAllTracks()
                _downloadedTrackListState.value = tracks.map { track ->
                    Log.d("TrackSelectionViewModel", "Loaded downloaded track: ${track.id} - ${track.title}")
                    TrackUI(
                        id = track.id,
                        title = track.title,
                        uploadedAt = track.uploadedAt,
                        size = track.size,
                        state = if (track.downloadedAt > 0) DownloadState.Completed else DownloadState.NotStarted
                    )
                }
                _isLoadingDownloads.value = false
            } catch (e: Exception) {
                Log.e("TrackSelectionViewModel", "Error loading downloaded tracks: ${e.message}")
                _fetchError.value = "Failed to load downloaded tracks"
            }
        }
    }

    fun fetchTrackList(token: String) {
        Log.d("WearOS", "Fetching data with token: $token")
        _isLoadingTracks.value = true


        viewModelScope.launch {
            getTracks(
                onSuccess = ::updateTracks,
                onError = ::setError,
                token = token,
            )
        }
    }

    fun updateTracks(data: List<Track>?) {
        Log.d("WearOS", "Data updated: $data")
        if (data != null) {
            _availableTrackListState.value = data.filter { track ->
                _downloadedTrackListState.value.none { it.id == track.id }  // Filter out already downloaded tracks
            }.map { track ->
                TrackUI(
                    id = track.id,
                    title = track.title,
                    uploadedAt = java.time.OffsetDateTime.parse(track.upload_date).toInstant()
                        .toEpochMilli(),
                    size = track.size,
                    state = DownloadState.NotStarted,
                )
            }  //.sorted()
            _fetchError.value = null
            _updateSuccess.value = true
        } else {
            Log.e("WearOS", "Data is null")
        }
        _isLoadingTracks.value = false
    }

//    private fun List<TrackButtonUI>.sorted(): List<TrackButtonUI> {
//        return this.sortedWith { a, b ->
//            when {
//                a.state is DownloadState.Completed && b.state !is DownloadState.Completed -> -1
//                a.state !is DownloadState.Completed && b.state is DownloadState.Completed -> 1
//                else -> when {
//                    a.downloadedAt == null && b.downloadedAt != null -> 1
//                    a.downloadedAt != null && b.downloadedAt == null -> -1
//                    a.downloadedAt != null && b.downloadedAt != null -> {
//                        if (a.downloadedAt!! > b.downloadedAt!!) -1 else 1
//                    }
//
//                    else -> {
//                        if (a.uploadedAt > b.uploadedAt) -1 else 1
//                    }
//                }
//            }
//        }
//    }

    fun setError(error: String?) {
        Log.e("WearOS", "Error occurred: $error")
        _fetchError.value = error
        _isLoadingTracks.value = false
    }

    private fun updateButtonState(index: Int, newState: DownloadState) {
        _availableTrackListState.value = _availableTrackListState.value.toMutableList().also {
            it[index] = it[index].copy(state = newState)
        }
    }

    fun downloadTrack(token: String, index: Int, trackID: Int, context: Context) {
        Log.d("DownloadTrack", "Downloading GPX")

        updateButtonState(index, DownloadState.InProgress)

        viewModelScope.launch {
            downloadGpx(
                token = token,
                gpxID = trackID,
                onError = {
                    Log.e("DownloadTrack", "Error occurred: $it")
                    _downloadError.value = "Failed to download track"
                    updateButtonState(index, DownloadState.NotStarted)
                },
                onSuccess = { fileContent ->
                    val downloaded = _availableTrackListState.value[index]

                    // Save the track to the database
                    viewModelScope.launch {
                        db.trackDao().insertTrack(
                            com.ontrek.wear.data.Track(
                                id = downloaded.id,
                                title = downloaded.title,
                                filename = downloaded.filename,
                                uploadedAt = downloaded.uploadedAt,
                                size = downloaded.size,
                                downloadedAt = System.currentTimeMillis()
                            )
                        )
                    }
                    saveFile(fileContent, downloaded.filename, context)
                    Log.d("DownloadTrack", "File downloaded successfully: $downloaded.filename")

                    // Update the downloaded track list state
                    updateButtonState(index, DownloadState.Completed)

                    // Add the downloaded track to the downloaded list
                    _downloadedTrackListState.value = listOf(
                        downloaded.copy(
                            state = DownloadState.Completed,
                            filename = downloaded.filename,
                        )
                    ) + _downloadedTrackListState.value

                    // remove the track from available list
                    _availableTrackListState.value = _availableTrackListState.value.toMutableList().also {
                        it.removeAt(index)
                    }

                    Log.d("DownloadTrack", "Track saved to database: ${downloaded.title}")
                    _downloadError.value = null
                    _downloadSuccess.value = true
                }
            )
        }
    }

    fun saveFile(fileContent: ByteArray, filename: String, context: Context) {
        context.openFileOutput(filename, Context.MODE_PRIVATE).use {
            it.write(fileContent)
        }
    }

    fun deleteTrack(index: Int, context: Context) {
        val trackToDelete = _downloadedTrackListState.value[index]
        Log.d("DeleteTrack", "Deleting track: ${trackToDelete.title}")

        viewModelScope.launch {
            try {
                // Delete from database
                db.trackDao().deleteTrackById(trackToDelete.id)

                // Delete the file from internal storage
                File(context.filesDir, trackToDelete.filename).delete()

                trackToDelete.state = DownloadState.NotStarted

                // Remove from the list
                _downloadedTrackListState.value = _downloadedTrackListState.value.toMutableList().also {
                    it.removeAt(index)
                }

                // Add back to available track list with NotStarted state
                _availableTrackListState.value = _availableTrackListState.value.toMutableList().also {
                    it.add(trackToDelete.copy(state = DownloadState.NotStarted))
                }

                // Reset the button state to NotStarted
                updateButtonState(_availableTrackListState.value.indexOf(trackToDelete), DownloadState.NotStarted)
            } catch (e: Exception) {
                Log.e("TrackSelectionViewModel", "Error deleting track: ${e.message}")
            }
        }
    }

    fun resetDownloadError() {
        _downloadError.value = null
    }

    class Factory(private val db: AppDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TrackSelectionViewModel::class.java)) {
                return TrackSelectionViewModel(db) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}