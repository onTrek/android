package com.ontrek.mobile.screens.track.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ontrek.shared.api.track.getMapTrack
import com.ontrek.shared.data.Track
import com.ontrek.shared.data.TrackStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.format.DateTimeFormatter

class TrackDetailViewModel : ViewModel() {
    private val _trackDetailState = MutableStateFlow<TrackDetailState>(TrackDetailState.Loading)
    val trackDetailState: StateFlow<TrackDetailState> = _trackDetailState

    private val _imageState = MutableStateFlow<ImageState>(ImageState.Loading)
    val imageState: StateFlow<ImageState> = _imageState

    // Funzione per caricare i dettagli della traccia
    fun loadTrackDetails(trackId: String, token: String) {
        viewModelScope.launch {
            _trackDetailState.value = TrackDetailState.Loading

            // Simulazione di una chiamata API
            delay(1000)

            // Dati di esempio
            val sampleTrack = Track(
                id = trackId.toInt(),
                title = "Monte Faggeto",
                filename = "MonteBianco.gpx",
                upload_date = "2025-05-11T08:00:00Z",
                stats = TrackStats(
                    ascent = 1000.0,
                    descent = 1000.0,
                    duration = "06:30:00",
                    km = 15.0f,
                    max_altitude = 2500,
                    min_altitude = 1500
                )
            )

            _trackDetailState.value = TrackDetailState.Success(sampleTrack)
        }
    }

    // Funzione per caricare l'immagine della traccia
    fun loadTrackImage(trackId: String, token: String) {
        viewModelScope.launch {
            _imageState.value = ImageState.Loading

            // Simulazione di una chiamata API
            delay(2000)

            getMapTrack(
                id = trackId,
                token = token,
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
            // URL di esempio di un'immagine
            //_imageState.value = ImageState.Success("https://images.unsplash.com/photo-1551632811-561732d1e306")
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

    // Formatta la data per la visualizzazione
    fun formatDate(dateString: String): String {
        return try {
            val instant = Instant.parse(dateString)
            DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(java.time.ZoneId.systemDefault()).format(instant)
        } catch (e: Exception) {
            dateString
        }
    }

    // Formatta la durata per la visualizzazione
    fun formatDuration(duration: String): String {
        return try {
            val parts = duration.split(":")
            "${parts[0]}h ${parts[1]}m"
        } catch (e: Exception) {
            duration
        }
    }
}