package com.ontrek.wear.screens.trackselection

import android.util.Log
import androidx.lifecycle.ViewModel
import com.ontrek.shared.api.track.fetchData
import com.ontrek.shared.data.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TrackSelectionViewModel : ViewModel() {

    private val _data = MutableStateFlow<List<Track>>(listOf<Track>())
    val trackListState: StateFlow<List<Track>> = _data

    private val _isLoading = MutableStateFlow<Boolean>(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchData(token: String) {
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
}