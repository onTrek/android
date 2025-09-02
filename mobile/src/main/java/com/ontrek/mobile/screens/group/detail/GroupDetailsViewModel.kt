package com.ontrek.mobile.screens.group.detail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.ontrek.mobile.screens.group.GroupsViewModel.TrackState
import com.ontrek.shared.api.groups.addMemberInGroup
import com.ontrek.shared.api.groups.changeGPXInGroup
import com.ontrek.shared.api.groups.deleteGroup
import com.ontrek.shared.api.groups.getGroupInfo
import com.ontrek.shared.api.groups.removeMemberFromGroup
import com.ontrek.shared.api.track.getTracks
import com.ontrek.shared.data.GroupInfoResponseDoc
import com.ontrek.shared.data.GroupMember
import com.ontrek.shared.data.TrackInfo
import com.ontrek.shared.data.UserMinimal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GroupDetailsViewModel : ViewModel() {

    private val _groupState = MutableStateFlow<GroupState>(GroupState.Loading)
    val groupState: StateFlow<GroupState> = _groupState

    private val _membersState = MutableStateFlow<List<GroupMember>>(emptyList())
    val membersState: StateFlow<List<GroupMember>> = _membersState

    private val _tracks = MutableStateFlow<TrackState>(TrackState.Loading)
    val tracks: StateFlow<TrackState> = _tracks

    private val _msgToast = MutableStateFlow("")
    val msgToast: StateFlow<String> = _msgToast

    private var navController: NavController? = null
    fun setNavController(navController: NavController) {
        this.navController = navController
    }

    fun loadGroupDetails(groupId: Int) {
        _groupState.value = GroupState.Loading
        viewModelScope.launch {
            getGroupInfo(
                id = groupId,
                onSuccess = { groupInfo ->
                    if (groupInfo != null) {
                        _groupState.value = GroupState.Success(groupInfo)
                        _membersState.value = groupInfo.members
                    } else {
                        _groupState.value = GroupState.Error("Group not found")
                    }
                },
                onError = { error ->
                    _groupState.value = GroupState.Error(error)
                },
            )
        }
    }

    fun loadTracks() {
        _tracks.value = TrackState.Loading
        viewModelScope.launch {
            getTracks(
                onSuccess = { trackList ->
                    _tracks.value = TrackState.Success(trackList ?: emptyList())
                },
                onError = { error ->
                    _tracks.value = TrackState.Error(error)
                    _msgToast.value = "Error loading tracks: $error"
                },
            )
        }
    }

    fun deleteGroup(groupId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            deleteGroup(
                id = groupId,
                onSuccess = { _ ->
                    _msgToast.value = "Group deleted successfully"
                    onSuccess()
                },
                onError = { error ->
                    _msgToast.value = "Error deleting group: $error"
                },
            )
        }
    }

    fun changeTrack(groupId: Int, track: TrackInfo) {
        viewModelScope.launch {
            changeGPXInGroup(
                id = groupId,
                trackId = track.id,
                onSuccess = { _ ->
                    _msgToast.value = "Track changed successfully"
                    _groupState.value = GroupState.Success(
                        (groupState.value as? GroupState.Success)?.groupInfo?.copy(
                            track = track
                        ) ?: GroupInfoResponseDoc(
                            description = "",
                            members = emptyList(),
                            created_at = "",
                            created_by = UserMinimal(id = "", username = ""),
                            track = track,
                        )
                    )
                },
                onError = { error ->
                    _msgToast.value = "Error changing track: $error"
                },
            )
        }
    }

    fun removeMember(groupId: Int, userId: String, ) {
        viewModelScope.launch {
            removeMemberFromGroup(
                groupID = groupId,
                userID = userId,
                onSuccess = {
                    _msgToast.value = "Member removed successfully"
                    deleteMemberInTheList(userId)
                },
                onError = { error ->
                    _msgToast.value = "Error removing member: $error"
                }
            )
        }
    }

    private fun deleteMemberInTheList(userId: String) {
        _membersState.value = _membersState.value.filter { it.id != userId }
    }

    fun leaveGroup(groupId: Int, ) {
        viewModelScope.launch {
            removeMemberFromGroup(
                groupID = groupId,
                onSuccess = {
                    _msgToast.value = "You have left the group successfully"
                    navController?.navigateUp()
                },
                onError = { error ->
                    _msgToast.value = "Error leaving group: $error"
                }
            )
        }
    }

    fun addMember(userId: String, groupId: Int) {
        viewModelScope.launch {
            if (_membersState.value.any { it.id == userId }) {
                _msgToast.value = "User is already a member of the group"
                return@launch
            }
            addMemberInGroup(
                userID = userId,
                groupID = groupId,
                onSuccess = { newMember ->
                    _msgToast.value = "Member added successfully"
                    _membersState.value = _membersState.value + listOfNotNull(newMember)
                },
                onError = { error ->
                    _msgToast.value = "Error adding member: $error"
                }
            )
        }
    }

    fun resetMsgToast() {
        _msgToast.value = ""
    }

    fun sendStartToWearable(context: Context, trackId: Int, trackName: String, sessionId: Int) {
        viewModelScope.launch {
            try {
                val putDataMapReq = PutDataMapRequest.create("/track-start").apply {
                    dataMap.putInt("trackId", trackId)
                    dataMap.putInt("sessionId", sessionId)
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

    sealed class GroupState {
        object Loading : GroupState()
        data class Success(val groupInfo: GroupInfoResponseDoc) : GroupState()
        data class Error(val message: String) : GroupState()
    }
}