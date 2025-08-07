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
import com.ontrek.mobile.screens.profile.ProfileViewModel.RequestsState.Companion.count
import com.ontrek.shared.api.friends.acceptFriendRequest
import com.ontrek.shared.api.friends.deleteFriend
import com.ontrek.shared.api.friends.deleteFriendRequest
import com.ontrek.shared.api.friends.getFriendRequests
import com.ontrek.shared.api.friends.getFriends
import com.ontrek.shared.api.profile.deleteProfile
import com.ontrek.shared.api.profile.getImageProfile
import com.ontrek.shared.api.profile.getProfile
import com.ontrek.shared.api.profile.uploadImageProfile
import com.ontrek.shared.data.FriendRequest
import com.ontrek.shared.data.Profile
import com.ontrek.shared.data.UserMinimal

class ProfileViewModel : ViewModel() {
    private val _userProfile = MutableStateFlow<UserProfileState>(UserProfileState.Loading)
    val userProfile: StateFlow<UserProfileState> = _userProfile.asStateFlow()

    private val _imageProfile = MutableStateFlow<UserImageState>(UserImageState.Loading)
    val imageProfile: StateFlow<UserImageState> = _imageProfile.asStateFlow()

    private val _connectionStatusWaer = MutableStateFlow<ConnectionState>(ConnectionState.Success(false))
    val connectionStatus: StateFlow<ConnectionState> = _connectionStatusWaer.asStateFlow()

    private val _friendsState = MutableStateFlow<FriendsState>(FriendsState.Loading)
    val friendsState: StateFlow<FriendsState> = _friendsState.asStateFlow()

    private val _requestsState = MutableStateFlow<RequestsState>(RequestsState.Loading)
    val requestsState: StateFlow<RequestsState> = _requestsState.asStateFlow()

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

    fun sendAuthToWearable(context: Context, token: String) {
        viewModelScope.launch {
            _connectionStatusWaer.value = ConnectionState.Loading
            try {
                val putDataMapReq = PutDataMapRequest.create("/auth").apply {
                    dataMap.putString("token", token)
                    dataMap.putLong("timestamp", System.currentTimeMillis())
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

    // -------- Friends Management --------
    fun fetchFriends() {
        viewModelScope.launch {
            _friendsState.value = FriendsState.Loading
            getFriends(
                onSuccess = { friends ->
                    if (friends.isNullOrEmpty()) {
                        _friendsState.value = FriendsState.Empty
                    } else {
                        _friendsState.value = FriendsState.Success(friends)
                    }
                },
                onError = { error ->
                    _friendsState.value = FriendsState.Error(error)
                }
            )
        }
    }

    fun removeFriend(friendId: String) {
        viewModelScope.launch {
            // Simulazione chiamata API
            deleteFriend(
                id = friendId,
                onSuccess = { message ->
                    _msgToast.value = message
                    removeFriendFromList(friendId)
                },
                onError = { error ->
                    _msgToast.value = error
                }
            )
        }
    }

    private fun removeFriendFromList(friendId: String) {
        viewModelScope.launch {
            _friendsState.value = when (val currentState = _friendsState.value) {
                is FriendsState.Success -> {
                    val updatedFriends = currentState.friends.filter { it.id != friendId }
                    FriendsState.Success(updatedFriends)
                }
                else -> currentState
            }
        }
    }

    private fun addFriendToList(friend: UserMinimal) {
        viewModelScope.launch {
            _friendsState.value = when (val currentState = _friendsState.value) {
                is FriendsState.Success -> {
                    val updatedFriends = currentState.friends.toMutableList().apply { add(friend) }
                    FriendsState.Success(updatedFriends)
                }
                else -> currentState
            }
        }
    }


    // -------- Friend Requests Management --------
    fun loadFriendRequests() {
        viewModelScope.launch {
            _requestsState.value = RequestsState.Loading
            getFriendRequests(
                onSuccess = { requests ->
                    if (requests.isNullOrEmpty()) {
                        _requestsState.value = RequestsState.Empty
                    } else {
                        _requestsState.value = RequestsState.Success(requests)
                    }
                },
                onError = { error ->
                    _requestsState.value = RequestsState.Error(error)
                }
            )
        }
    }

    // Accetta richiesta di amicizia
    fun acceptRequest(user: FriendRequest) {
        viewModelScope.launch {
            // Simulazione chiamata API
            acceptFriendRequest(
                id = user.id,
                onSuccess = { message ->
                    _msgToast.value = message
                    removeRequestFromList(user.id)
                    val friend = UserMinimal(
                        id = user.id,
                        username = user.username,
                    )
                    addFriendToList(friend)
                },
                onError = { error ->
                    _msgToast.value = error
                }
            )
        }
    }

    // Rifiuta richiesta di amicizia
    fun rejectFriendRequest(requestId: String) {
        viewModelScope.launch {
            // Simulazione chiamata API
            deleteFriendRequest(
                id = requestId,
                onSuccess = { message ->
                    _msgToast.value = message
                    removeRequestFromList(requestId)
                },
                onError = { error ->
                    _msgToast.value = error
                }
            )
        }
    }

    private fun removeRequestFromList(requestId: String) {
        viewModelScope.launch {
            _requestsState.value = when (val currentState = _requestsState.value) {
                is RequestsState.Success -> {
                    val updatedRequests = currentState.requests.filter { it.id != requestId }
                    RequestsState.Success(updatedRequests)
                }
                else -> currentState
            }

            if (_requestsState.value is RequestsState.Success) {
                val count = (_requestsState.value as RequestsState.Success).count
                if (count == 0) {
                    _requestsState.value = RequestsState.Empty
                }
            }
        }
    }

    fun clearMsgToast() {
        _msgToast.value = ""
    }

    fun setMsgToast(msg: String) {
        _msgToast.value = msg
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

    sealed class FriendsState {
        data class Success(val friends: List<UserMinimal>) : FriendsState()
        data class Error(val message: String) : FriendsState()
        object Loading : FriendsState()
        object Empty : FriendsState()
    }

    sealed class RequestsState {
        data class Success(val requests: List<FriendRequest>) : RequestsState()
        data class Error(val message: String) : RequestsState()
        object Loading : RequestsState()
        object Empty : RequestsState()

        companion object {
            val RequestsState.count: Int
                get() = when (this) {
                    is Success -> requests.size
                    else -> 0
                }
        }
    }
}