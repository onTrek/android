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
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.ontrek.shared.api.profile.deleteProfile
import com.ontrek.shared.api.profile.getImageProfile
import com.ontrek.shared.api.profile.getProfile
import com.ontrek.shared.api.profile.uploadImageProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update

data class UserProfile(
    val username: String = "Update...",
    val email: String = "Update...",
    val userId: String = "Update...",
    var imageProfile: ByteArray = ByteArray(0)
)

class ProfileViewModel : ViewModel() {

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isLoadingConnection = MutableStateFlow(false)
    val isLoadingConnection = _isLoadingConnection.asStateFlow()

    private val _isLoadingDeleteProfile = MutableStateFlow(false)
    val isLoadingDeleteProfile = _isLoadingDeleteProfile.asStateFlow()

    private val _isLoadingImage = MutableStateFlow(false)
    val isLoadingImage = _isLoadingImage.asStateFlow()

    private val _connectionStatus = MutableStateFlow(false)
    val connectionStatus: StateFlow<Boolean> = _connectionStatus.asStateFlow()

    private val _msgToast = MutableStateFlow("")
    val msgToastFlow: StateFlow<String> = _msgToast.asStateFlow()

    fun fetchUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _isLoadingImage.value = true
            try {
                getProfile(
                    onSuccess = { response ->
                        _userProfile.update {
                            UserProfile(
                                username = response?.username ?: "",
                                email = response?.email ?: "",
                                userId = response?.id ?: "",
                            )
                        }
                        getImageProfile(
                            id = _userProfile.value.userId,
                            onSuccess = { imageBytes ->
                                _userProfile.update { it.copy(imageProfile = imageBytes) }
                                _isLoadingImage.value = false
                            },
                            onError = { error ->
                                _userProfile.update { it.copy(imageProfile = ByteArray(0)) }
                                _isLoadingImage.value = false
                            }
                        )
                        _isLoading.value = false
                    },
                    onError = { error ->
                        _userProfile.update {
                            UserProfile(
                                username = "Error",
                                email = "Error",
                                userId = "Error"
                            )
                        }
                        _isLoadingImage.value = false
                        _isLoading.value = false
                    }
                )

            } catch (e: Exception) {
                Log.e("ProfileView", "Error fetching user profile", e)
            }
        }
    }

    fun fetchDeleteProfile(clearToken: () -> Unit) {
        viewModelScope.launch {
            _isLoadingDeleteProfile.value = true

            try {
                 deleteProfile(
                     onSuccess = {
                         _userProfile.update { UserProfile(
                             username = "",
                             email = "",
                             userId = ""
                         ) }
                         _msgToast.value = "Profile deleted successfully"
                         clearToken()
                     },
                     onError = { error ->
                         Log.e("ProfileViewModel", "Error deleting profile: $error")
                         _msgToast.value = "Impossible to delete profile: $error"
                     }
                 )
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error deleting profile", e)
            } finally {
                delay(500)
                _isLoadingDeleteProfile.value = false
            }
        }
    }

    fun sendAuthToWearable(context: Context, token: String) {
        viewModelScope.launch {
            _isLoadingConnection.value = true
            try {
                val putDataMapReq = PutDataMapRequest.create("/auth").apply {
                    dataMap.putString("token", token)
                    dataMap.putLong("timestamp", System.currentTimeMillis())
                }
                val request = putDataMapReq.asPutDataRequest().setUrgent()

                Wearable.getDataClient(context).putDataItem(request)
                    .addOnSuccessListener {
                        Log.d("WATCH_CONNECTION", "Connessione riuscita con il wearable")
                        _connectionStatus.value = true
                        _msgToast.value = "Connected to wearable successfully"
                    }
                    .addOnFailureListener {
                        Log.e("WATCH_CONNECTION", "Fallita connessione con il wearable", it)
                        _connectionStatus.value = false
                        _msgToast.value = "Failed to connect to wearable"
                    }
            } catch (e: Exception) {
                Log.e("WATCH_CONNECTION", "Errore durante la connessione al wearable", e)
                _connectionStatus.value = false
                _msgToast.value = "Error connecting to wearable: ${e.message}"
            } finally {
                delay(500)
                _isLoadingConnection.value = false
            }
        }
    }

    fun updateProfileImage(image: ByteArray, filename: String) {
        try {
            viewModelScope.launch {
                _isLoadingImage.value = true
                uploadImageProfile(
                    imageBytes = image,
                    filename = filename,
                    onSuccess = {
                        Log.d("ProfileViewModel", "Profile image updated successfully")
                        _msgToast.value = "Profile image updated successfully"
                        viewModelScope.launch {
                            _userProfile.update { it.copy(imageProfile = image.copyOf()) }
                        }
                        _isLoadingImage.value = false
                    },
                    onError = { error ->
                        Log.e("ProfileViewModel", "Error updating profile image: $error")
                        _msgToast.value = "Error updating profile image: $error"
                        _isLoadingImage.value = false
                    }
                )
            }
        } catch (e: Exception) {
            Log.e("ProfileViewModel", "Error updating profile image", e)
            _msgToast.value = "Error updating profile image: ${e.message}"
        }
    }

    fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) {
                        name = it.getString(index)
                    }
                }
            }
        }
        if (name == null) {
            name = uri.path?.substringAfterLast('/')
        }
        return name
    }

    fun logout(clearToken: () -> Unit) {
        viewModelScope.launch {
            try {
                clearToken()
                _userProfile.value = UserProfile() // Reset user profile
                _msgToast.value = "Logged out successfully"
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error during logout", e)
                _msgToast.value = "Error during logout: ${e.message}"
            }
        }
    }
    fun resetMsgToast() {
        viewModelScope.launch {
            _msgToast.value = ""
        }
    }
}