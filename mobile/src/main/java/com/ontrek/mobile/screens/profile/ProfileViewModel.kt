package com.ontrek.mobile.screens.profile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.Context
import android.util.Log

data class UserProfile(
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val userId: String = ""
)

class ProfileViewModel : ViewModel() {

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        fetchUserProfile()
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Qui dovresti fare la chiamata API reale
                // Per ora simuliamo una risposta
                _userProfile.value = UserProfile(
                    name = "Nome e Cognome",
                    username = "gioele_username",
                    email = "gioele.rossi@example.com",
                    userId = "3e530eef-2f77-418e-89e7-d82537c9109a"
                )
            } catch (e: Exception) {
                Log.e("ConnectionViewModel", "Error fetching user profile", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchDeleteProfile() {
        Log.d("rofileViewModel", "Deleting profile for user: ${userProfile.value.userId}")
        viewModelScope.launch {
            try {
                // Simulazione di una chiamata API per eliminare il profilo
                // Qui dovresti implementare la logica reale per eliminare il profilo
                Log.d("ProfileViewModel", "Profile deleted successfully")
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error deleting profile", e)
            }
        }
    }

    fun sendAuthToWearable(context: Context) {
        val putDataMapReq = PutDataMapRequest.create("/auth").apply {
            dataMap.putString("token", userProfile.value.userId)
            dataMap.putLong("timestamp", System.currentTimeMillis())
        }
        val request = putDataMapReq.asPutDataRequest().setUrgent()
        Wearable.getDataClient(context).putDataItem(request)
            .addOnSuccessListener { Log.d("WATCH_CONNECTION", "Started Activity") }
            .addOnFailureListener { Log.e("WATCH_CONNECTION", "Failed to send data", it) }
    }
}