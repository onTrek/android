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
import java.io.File

data class GroupUI(
    val group_id: Int,
    val description: String,
    val created_at: String,
    val created_by: String,
    val member_number: Int,
    val track: TrackInfo,
    val downloadState: DownloadState,
)

sealed class DownloadState {
    object NotStarted : DownloadState()
    object InProgress : DownloadState()
    object Completed : DownloadState()
}

class GroupSelectionViewModel(private val db: AppDatabase) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _fetchError = MutableStateFlow<String?>(null)
    val fetchError: StateFlow<String?> = _fetchError

    private val _groupsListState = MutableStateFlow<List<GroupUI>>(listOf())
    val groupListState: StateFlow<List<GroupUI>> = _groupsListState

    private val _downloadError = MutableStateFlow<String?>(null)
    val downloadError: StateFlow<String?> = _downloadError


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
        viewModelScope.launch {
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
                        ),
                        downloadState = if (checkIfTrackExists(group.track.id)) {
                            DownloadState.Completed
                        } else {
                            DownloadState.NotStarted
                        }
                    )
                }
                _fetchError.value = null
            } else {
                Log.e("WearOS", "Data is null")
            }
            _isLoading.value = false
        }
    }

    fun setError(error: String?) {
        Log.e("WearOS", "Error occurred: $error")
        _groupsListState.value = listOf()
        _fetchError.value = error
        _isLoading.value = false
    }

    private suspend fun checkIfTrackExists(trackID: Int): Boolean {
        if (trackID == -1) {
            Log.d("GroupTrack", "Track ID is -1, skipping check")
            return false
        }
        val exists = MutableLiveData<Boolean>()
        exists.value = db.trackDao().getTrackById(trackID) != null

        Log.d("GroupTrack", "Track exists: ${exists.value} for ID: $trackID")
        return exists.value == true  // because it can be null
    }

    fun deleteTrack(groupIndex: Int, context: Context) {
        val group = _groupsListState.value[groupIndex]
        if (group.track.id == -1) {
            Log.d("DeleteTrack", "No track associated with this group.")
            return
        }
        Log.d("DeleteTrack", "Deleting track: ${group.track.title}")

        viewModelScope.launch {
            try {
                db.trackDao().deleteTrackById(group.track.id)

                File(context.filesDir, "${group.track.id}.gpx").delete()

                updateDownloadState(groupIndex, DownloadState.NotStarted)
            } catch (e: Exception) {
                Log.e("DeleteTrack", "Error deleting track: ${e.message}")
            }
        }
    }

    fun downloadTrack(groupIndex: Int, trackId: Int, context: Context) {
        viewModelScope.launch {
            // Update the download state to InProgress
            updateDownloadState(groupIndex, DownloadState.InProgress)


            // get the track details and wait for completion
            val trackDetail = getTrackSuspending(groupIndex, trackId)

            // Check if we got the track details successfully
            if (trackDetail == null) {
                Log.e("DownloadTrack", "Track not found for ID: $trackId")
                updateDownloadState(groupIndex, DownloadState.NotStarted)
                _downloadError.value = "Track not found"
                return@launch
            }

            // download the GPX file
            val filename = "${trackDetail.id}.gpx"
            downloadGpxSuspending(groupIndex, trackDetail.id, filename, trackDetail, context)
        }
    }

    private suspend fun getTrackSuspending(groupIndex: Int, trackId: Int): Track? {
        return kotlin.coroutines.suspendCoroutine { continuation ->
            getTrack(
                id = trackId,
                onSuccess = { track ->
                    Log.d("DownloadTrack", "Track downloaded successfully: $track")
                    continuation.resumeWith(Result.success(track))
                },
                onError = { errorMessage ->
                    Log.e("DownloadTrack", "Error getting track: $errorMessage")
                    updateDownloadState(groupIndex, DownloadState.NotStarted)
                    _downloadError.value = "Failed to get track details"
                    continuation.resumeWith(Result.success(null))
                }
            )
        }
    }

    private suspend fun downloadGpxSuspending(groupIndex: Int, gpxId: Int, filename: String, trackDetail: Track, context: Context) {
        return kotlin.coroutines.suspendCoroutine { continuation ->
            downloadGpx(
                gpxID = gpxId,
                onError = { errorMessage ->
                    Log.e("DownloadTrack", "Error occurred: $errorMessage")
                    updateDownloadState(groupIndex, DownloadState.NotStarted)
                    _downloadError.value = "Failed to download GPX file"
                    continuation.resumeWith(Result.success(Unit))
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
                    updateDownloadState(groupIndex, DownloadState.Completed)
                    continuation.resumeWith(Result.success(Unit))
                }
            )
        }
    }

    fun saveFile(fileContent: ByteArray, filename: String, context: Context) {
        context.openFileOutput(filename, Context.MODE_PRIVATE).use {
            it.write(fileContent)
        }
    }

    private fun updateDownloadState(groupIndex: Int, newState: DownloadState) {
        _groupsListState.value = _groupsListState.value.mapIndexed { index, groupUI ->
            if (index == groupIndex) {
                groupUI.copy(downloadState = newState)
            } else {
                groupUI
            }
        }
    }

    fun clearDownloadError() {
        _downloadError.value = null
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