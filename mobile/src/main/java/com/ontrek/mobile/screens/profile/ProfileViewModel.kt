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
import com.ontrek.shared.data.Profile

class ProfileViewModel : ViewModel() {
    private val _userProfile = MutableStateFlow<UserProfileState>(UserProfileState.Loading)
    val userProfile: StateFlow<UserProfileState> = _userProfile.asStateFlow()

    private val _imageProfile = MutableStateFlow<UserImageState>(UserImageState.Loading)
    val imageProfile: StateFlow<UserImageState> = _imageProfile.asStateFlow()

    private val _connectionStatusWaer = MutableStateFlow<ConnectionState>(ConnectionState.Success(false))
    val connectionStatus: StateFlow<ConnectionState> = _connectionStatusWaer.asStateFlow()

    private val _msgToast = MutableStateFlow("")
    val msgToast: StateFlow<String> = _msgToast.asStateFlow()

    fun fetchUserProfile() {
        viewModelScope.launch {
            _userProfile.value = UserProfileState.Loading
            _imageProfile.value = UserImageState.Loading
            try {
                getProfile(
                    onSuccess = { response ->
                        _userProfile.value = UserProfileState.Success(
                            userProfile = response ?: Profile(
                                username = "",
                                email = "",
                                id = ""
                            )
                        )
                        getImageProfile(
                            id = response?.id ?: "0",
                            onSuccess = { imageBytes ->
                                _imageProfile.value = UserImageState.Success(imageBytes)
                            },
                            onError = { error ->
                                _imageProfile.value = UserImageState.Error("Error fetching image: $error")
                            }
                        )
                    },
                    onError = { error ->
                        _userProfile.value = UserProfileState.Error("Error fetching profile: $error")
                        _imageProfile.value = UserImageState.Error("Error fetching image: $error")
                    }
                )

            } catch (e: Exception) {
                Log.e("ProfileView", "Error fetching user profile", e)
            }
        }
    }

    fun deleteProfile(clearToken: () -> Unit) {
        viewModelScope.launch {
            _userProfile.value = UserProfileState.Loading
            _imageProfile.value = UserImageState.Loading
            deleteProfile(
                onSuccess = {
                    _msgToast.value = "Profile deleted successfully"
                    clearToken()
                },
                onError = { error ->
                    _msgToast.value = "Error to delete profile: $error"
                }
            )
        }
    }

    fun sendAuthToWearable(context: Context, token: String, userID: String) {
        viewModelScope.launch {
            _connectionStatusWaer.value = ConnectionState.Loading
            try {
                val putDataMapReq = PutDataMapRequest.create("/auth").apply {
                    dataMap.putString("token", token)
                    dataMap.putLong("timestamp", System.currentTimeMillis())
                    dataMap.putString("user", userID)
                }
                val request = putDataMapReq.asPutDataRequest().setUrgent()

                Wearable.getDataClient(context).putDataItem(request)
                    .addOnSuccessListener {
                        _connectionStatusWaer.value = ConnectionState.Success(true)
                        _msgToast.value = "Connected to wearable successfully"
                    }
                    .addOnFailureListener {
                        _connectionStatusWaer.value = ConnectionState.Error("Failed to connect to wearable")
                        _msgToast.value = "Failed to connect to wearable"
                    }
            } catch (e: Exception) {
                _msgToast.value = "Error connecting to wearable: ${e.message}"
            }
        }
    }

    fun updateProfileImage(image: ByteArray, filename: String) {
        try {
            viewModelScope.launch {
                _imageProfile.value = UserImageState.Loading
                uploadImageProfile(
                    imageBytes = image,
                    filename = filename,
                    onSuccess = {
                        _msgToast.value = "Profile image updated successfully"
                        viewModelScope.launch {
                            _imageProfile.value = UserImageState.Success(image.copyOf())
                        }
                    },
                    onError = { error ->
                        _msgToast.value = "Error updating profile image: $error"
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
            _userProfile.value = UserProfileState.Loading
            try {
                _msgToast.value = "Log out successfully"
                clearToken()
            } catch (e: Exception) {
                _msgToast.value = "Error during logout: ${e.message}"
            }
        }
    }

    fun clearMsgToast() {
        _msgToast.value = ""
    }

    fun setMsgToast(message: String) {
        _msgToast.value = message
    }

    sealed class UserProfileState {
        data class Success(val userProfile: Profile) : UserProfileState()
        data class Error(val message: String) : UserProfileState()
        object Loading : UserProfileState()
    }

    sealed class UserImageState {
        data class Success(val imageBytes: ByteArray) : UserImageState()
        data class Error(val message: String) : UserImageState()
        object Loading : UserImageState()
    }

    sealed class ConnectionState {
        data class Success(val isConnected: Boolean) : ConnectionState()
        data class Error(val message: String) : ConnectionState()
        object Loading : ConnectionState()
    }
}