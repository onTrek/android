package com.ontrek.mobile.screens.track.detail

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.ontrek.shared.api.track.getMapTrack
import com.ontrek.shared.api.track.getTrack
import com.ontrek.shared.data.Track
import com.ontrek.shared.api.track.deleteTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TrackDetailViewModel : ViewModel() {
    private val _trackDetailState = MutableStateFlow<TrackDetailState>(TrackDetailState.Loading)
    val trackDetailState: StateFlow<TrackDetailState> = _trackDetailState

    private val _imageState = MutableStateFlow<ImageState>(ImageState.Loading)
    val imageState: StateFlow<ImageState> = _imageState

    private val _msgToast = MutableStateFlow<String>("")
    val msgToast: StateFlow<String> = _msgToast


    // Funzione per caricare i dettagli della traccia
    fun loadTrackDetails(trackId: Int) {
        viewModelScope.launch {
            _trackDetailState.value = TrackDetailState.Loading

            getTrack(
                id = trackId,
                onSuccess = { track ->
                    if (track != null) {
                        _trackDetailState.value = TrackDetailState.Success(track)
                    } else {
                        _trackDetailState.value = TrackDetailState.Error("Track not found")
                    }
                },
                onError = { errorMessage ->
                    Log.e("TrackDetailViewModel", "Error loading track: $errorMessage")
                    _trackDetailState.value = TrackDetailState.Error(errorMessage)
                },
            )
        }
    }

    // Funzione per caricare l'immagine della traccia
    fun loadTrackImage(trackId: Int) {
        viewModelScope.launch {
            _imageState.value = ImageState.Loading

            getMapTrack(
                id = trackId,
                onSuccess = { responseBody ->
                    // Usa un thread IO per leggere la risposta
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            Log.d("TrackDetailViewModel", "Image loaded successfully")
                            val imageBytes = responseBody?.bytes()

                            // Torna al main thread per aggiornare l'UI
                            withContext(Dispatchers.Main) {
                                if (imageBytes != null) {
                                    _imageState.value = ImageState.SuccessBinary(imageBytes)
                                } else {
                                    _imageState.value = ImageState.Error("Error in image loading: response body is null")
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                _imageState.value = ImageState.Error(e.message ?: "Errore sconosciuto")
                            }
                        }
                    }
                },
                onError = { errorMessage ->
                    _imageState.value = ImageState.Error(errorMessage)
                }
            )
        }
    }

    fun deleteTrack(trackId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _trackDetailState.value = TrackDetailState.Loading
            deleteTrack(
                id = trackId,
                onSuccess = { _ ->
                    _msgToast.value = "Track deleted successfully"
                    onSuccess()
                },
                onError = { errorMsg ->
                    _msgToast.value = errorMsg
                },
            )
        }
    }

    fun sendStartToWearable(context: Context, trackId: Int, trackName: String) {
        viewModelScope.launch {
            try {
                val putDataMapReq = PutDataMapRequest.create("/track-start").apply {
                    dataMap.putInt("trackId", trackId)
                    dataMap.putInt("sessionId", -1)
                    dataMap.putLong("timestamp", System.currentTimeMillis())
                    dataMap.putString("trackName", trackName)
                }
                val request = putDataMapReq.asPutDataRequest().setUrgent()

                Wearable.getDataClient(context).putDataItem(request)
                    .addOnSuccessListener {
                        _msgToast.value = "Loading track on wearable"
                    }
                    .addOnFailureListener {
                        _msgToast.value = "Failed to connect to wearable"
                    }
            } catch (e: Exception) {
                _msgToast.value = "Error connecting to wearable: ${e.message}"
            }
        }
    }

    // Stati per i dettagli della traccia (carino, così mi consigliava chatGPT e funziona... godo)
    sealed class TrackDetailState {
        object Loading : TrackDetailState()
        data class Success(val track: Track) : TrackDetailState()
        data class Error(val message: String) : TrackDetailState()
    }

    // Stati per l'immagine
    sealed class ImageState {
        object Loading : ImageState()
        data class SuccessBinary(val imageBytes: ByteArray) : ImageState() {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false
                other as SuccessBinary
                return imageBytes.contentEquals(other.imageBytes)
            }
            override fun hashCode(): Int = imageBytes.contentHashCode()
        }
        data class Error(val message: String) : ImageState()
    }
}