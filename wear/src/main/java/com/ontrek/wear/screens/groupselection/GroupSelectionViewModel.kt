package com.ontrek.wear.screens.groupselection

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ontrek.shared.api.gpx.downloadGpx
import com.ontrek.shared.api.groups.getGroups
import com.ontrek.shared.api.track.getTrack
import com.ontrek.shared.data.GroupDoc
import com.ontrek.shared.data.Track
import com.ontrek.shared.data.TrackInfo
import com.ontrek.wear.data.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class GroupUI(
    val group_id: Int,
    val description: String,
    val created_at: String,
    val created_by: String,
    val member_number: Int,
    val track: TrackInfo
)

sealed class DownloadState {
    object NotStarted : DownloadState()
    object InProgress : DownloadState()
    object Completed : DownloadState()
    class Error(val message: String) : DownloadState()
}

class GroupSelectionViewModel(private val db: AppDatabase) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _fetchError = MutableStateFlow<String?>(null)
    val fetchError: StateFlow<String?> = _fetchError

    private val _groupsListState = MutableStateFlow<List<GroupUI>>(listOf())
    val groupListState: StateFlow<List<GroupUI>> = _groupsListState

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.NotStarted)
    val downloadState: StateFlow<DownloadState> = _downloadState


    fun fetchGroupsList() {
        Log.d("WearOS", "Fetching data")
        _isLoading.value = true


        viewModelScope.launch {
            getGroups(
                onSuccess = ::updateGroups,
                onError = ::setError
            )
        }
    }

    fun updateGroups(data: List<GroupDoc>?) {
        Log.d("WearOS", "Data updated: $data")
        if (data != null) {
            _groupsListState.value = data.map { group ->
                GroupUI(
                    group_id = group.group_id,
                    description = group.description,
                    created_at = group.created_at,
                    created_by = group.created_by,
                    member_number = group.members_number,
                    track = TrackInfo(
                        id = group.track.id,
                        title = group.track.title
                    )
                )
            }
            _fetchError.value = null
        } else {
            Log.e("WearOS", "Data is null")
        }
        _isLoading.value = false
    }

    fun setError(error: String?) {
        Log.e("WearOS", "Error occurred: $error")
        _groupsListState.value = listOf()
        _fetchError.value = error
        _isLoading.value = false
    }

    fun checkIfTrackExists(trackID: Int): Boolean {
        if (trackID == -1) {
            Log.d("WearOS", "Track ID is -1, skipping check")
            return false
        }
        val exists = MutableLiveData<Boolean>()
        viewModelScope.launch {
            exists.value = db.trackDao().getTrackById(trackID) != null
        }
        return exists.value == true  // because it can be null
    }

    fun downloadTrack(trackId: Int, context: Context) {
        viewModelScope.launch {

            _downloadState.value = DownloadState.InProgress

            var trackDetail: Track? = null
            getTrack(
                id = trackId,
                onSuccess = { track ->
                    if (track != null) {
                        trackDetail = track
                    } else {
                        _downloadState.value = DownloadState.Error(message = "Failed to download track")
                    }
                },
                onError = { errorMessage ->
                    _downloadState.value = DownloadState.Error(message = "Failed to download track")
                },
            )

            if (trackDetail == null) {
                Log.e("DownloadTrack", "Track not found for ID: $trackId")
                _downloadState.value = DownloadState.Error(message = "Track not found")
                return@launch
            }

            val filename = "${trackDetail.id}.gpx"

            downloadGpx(
                gpxID = trackDetail.id,
                onError = {
                    Log.e("DownloadTrack", "Error occurred: $it")
                    _downloadState.value = DownloadState.Error(message = "Failed to download track")
                },
                onSuccess = { fileContent ->
                    // Save the track to the database
                    viewModelScope.launch {
                        db.trackDao().insertTrack(
                            com.ontrek.wear.data.Track(
                                id = trackDetail.id,
                                title = trackDetail.title,
                                filename = filename,
                                uploadedAt = java.time.OffsetDateTime.parse(trackDetail.upload_date)
                                    .toInstant()
                                    .toEpochMilli(),
                                size = trackDetail.size,
                                downloadedAt = System.currentTimeMillis()
                            )
                        )
                    }
                    saveFile(fileContent, filename, context)
                    Log.d("DownloadTrack", "File downloaded successfully: ${trackDetail.title}")
                    _downloadState.value = DownloadState.Completed
                }
            )
        }
    }

    fun saveFile(fileContent: ByteArray, filename: String, context: Context) {
        context.openFileOutput(filename, Context.MODE_PRIVATE).use {
            it.write(fileContent)
        }
    }

    class Factory(private val db: AppDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GroupSelectionViewModel::class.java)) {
                return GroupSelectionViewModel(db) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}