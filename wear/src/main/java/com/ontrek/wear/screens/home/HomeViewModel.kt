package com.ontrek.wear.screens.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ontrek.shared.data.Track
import com.ontrek.shared.api.track.fetchData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HomeViewModel: ViewModel() {

    private val _data = MutableStateFlow<List<Track>>(listOf<Track>())
    val trackListState : StateFlow<List<Track>> = _data
    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun fetchData(token: String) {
        Log.d("WearOS", "Fetching data with token: $token")
        _isLoading.value = true
        fetchData(::updateData, token)
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
}