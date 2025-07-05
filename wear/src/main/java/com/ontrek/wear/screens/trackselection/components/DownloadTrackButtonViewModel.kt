package com.ontrek.wear.screens.trackselection.components

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.ontrek.shared.api.gpx.downloadGpx
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DownloadTrackButtonViewModel : ViewModel() {

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error


    fun downloadTrack(token: String, trackID: Int, context: Context) {
        Log.d("DownloadTrack", "Dowloading GPX")
        downloadGpx(
            token = token,
            gpxID = trackID,
            onError = ::setError,
            onSuccess = { fileContent, filename ->
                Log.d("DownloadTrack", "File downloaded successfully: $filename")
                saveFile(fileContent, filename, context)
            }
        )
        _isLoading.value = true
    }

    fun saveFile(fileContent: ByteArray, filename: String, context: Context) {
        context.openFileOutput(filename, Context.MODE_PRIVATE).use {
            it.write(fileContent)
        }
        _isLoading.value = false
    }

    fun setError(error: String?) {
        Log.e("DownloadTrack", "Error occurred: $error")
        _error.value = error
        _isLoading.value = false
    }
}