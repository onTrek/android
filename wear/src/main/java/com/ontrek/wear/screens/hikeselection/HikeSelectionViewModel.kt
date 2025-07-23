package com.ontrek.wear.screens.hikeselection

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ontrek.shared.api.hikes.getGroups
import com.ontrek.shared.data.Hikes
import com.ontrek.shared.data.TrackInfo
import com.ontrek.wear.data.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HikeUI(
    val group_id: Int,
    val description: String,
    val created_at: String,
    val created_by: String,
    val member_number: Int,
    val track: TrackInfo
)

class HikeSelectionViewModel(private val db: AppDatabase) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _fetchError = MutableStateFlow<String?>(null)
    val fetchError: StateFlow<String?> = _fetchError

    private val _availableHikesListState = MutableStateFlow<List<HikeUI>>(listOf())
    val availableHikesListState: StateFlow<List<HikeUI>> = _availableHikesListState

    private val _isLoadingHikes = MutableStateFlow(false)
    val isLoadingHikes: StateFlow<Boolean> = _isLoadingHikes

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess

    private val _downloadSuccess = MutableStateFlow(false)
    val downloadSuccess: StateFlow<Boolean> = _downloadSuccess


    fun fetchHikesList(token: String) {
        Log.d("WearOS", "Fetching data with token: $token")
        _isLoading.value = true


        viewModelScope.launch {
            getGroups(
                token = token,
                onSuccess =  ::updateHikes,
                onError = ::setError
            )
        }
    }

    fun updateHikes(data: List<Hikes>?) {
        Log.d("WearOS", "Data updated: $data")
        if (data != null) {
            _availableHikesListState.value = data.map { hike ->
                HikeUI(
                    group_id = hike.group_id,
                    description = hike.description,
                    created_at = hike.created_at,
                    created_by = hike.created_by,
                    member_number = hike.member_number,
                    track = TrackInfo(
                        id = hike.track.id,
                        title = hike.track.title
                    )
                )
            }
            _fetchError.value = null
            _updateSuccess.value = true
        } else {
            Log.e("WearOS", "Data is null")
        }
        _isLoadingHikes.value = false
        _isLoading.value = false
    }

    fun setError(error: String?) {
        Log.e("WearOS", "Error occurred: $error")
        _availableHikesListState.value = listOf()
        _fetchError.value = error
        _isLoadingHikes.value = false
        _isLoading.value = false
    }

    class Factory(private val db: AppDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HikeSelectionViewModel::class.java)) {
                return HikeSelectionViewModel(db) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}