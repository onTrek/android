package com.ontrek.wear.screens.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ontrek.shared.data.Track
import com.ontrek.shared.api.track.fetchData

class HomeViewModel: ViewModel() {

    private val _data = MutableLiveData<List<Track>>(listOf<Track>())
    val trackListState : LiveData<List<Track>> = _data

    fun fetchData(token: String) {
        Log.d("WearOS", "Fetching data with token: $token")
        fetchData(::updateData, token)
    }

    fun updateData(data: List<Track>?) {
        Log.d("WearOS", "Data updated: $data")
        if (data != null) {
            _data.value = data
        } else {
            Log.e("WearOS", "Data is null")
        }
    }
}