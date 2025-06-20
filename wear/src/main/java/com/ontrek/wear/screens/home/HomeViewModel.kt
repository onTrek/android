package com.ontrek.wear.screens.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ontrek.shared.data.Track
import com.ontrek.shared.api.gpx.fetchData
import com.ontrek.shared.data.GpxResponse

class HomeViewModel: ViewModel() {

    init {
        fetchData(::updateData, "1b34ec48-c669-4ae3-ae6a-49641dd2bb2c") //fuck it we ball
    }

    private val _data = MutableLiveData<List<Track>>(listOf<Track>())
    val trackListState : LiveData<List<Track>> = _data

    fun updateData(data: GpxResponse?) {
        Log.d("WearOS", "Data updated: $data")
        if (data != null) {
            _data.value = data.gpx_files
        } else {
            Log.e("WearOS", "Data is null")
        }
    }
}