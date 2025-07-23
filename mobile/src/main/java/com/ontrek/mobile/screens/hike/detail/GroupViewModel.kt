package com.ontrek.mobile.screens.hike.detail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.ontrek.shared.api.hikes.changeGPXInGroup
import com.ontrek.shared.api.hikes.deleteGroup
import com.ontrek.shared.api.hikes.getGroupInfo
import com.ontrek.shared.api.hikes.removeMemberFromGroup
import com.ontrek.shared.api.track.getTracks
import com.ontrek.shared.data.GroupInfoResponseDoc
import com.ontrek.shared.data.GroupMember
import com.ontrek.shared.data.Track
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

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks

    private val _msgToast = MutableStateFlow("")
    val msgToast: StateFlow<String> = _msgToast

    private var navController: NavController? = null
    fun setNavController(navController: NavController) {
        this.navController = navController
    }

    fun loadGroupDetails(groupId: Int, token: String) {
        _groupState.value = GroupState.Loading
        viewModelScope.launch {
            getGroupInfo(
                id = groupId,
                onSuccess = { groupInfo ->
                    if (groupInfo != null) {
                        _groupState.value = GroupState.Success(groupInfo)
                        _membersState.value = groupInfo.members
                    }
                    else {
                        _groupState.value = GroupState.Error("Group not found")
                    }
                },
                onError = { error ->
                    _groupState.value = GroupState.Error(error)
                },
                token = token
            )
        }
    }

    fun loadTracks(token: String) {
        viewModelScope.launch {
            getTracks(
                onSuccess = { trackList ->
                    _tracks.value = trackList ?: emptyList()
                },
                onError = { error ->
                    _msgToast.value = "Error loading tracks: $error"
                },
                token = token
            )
        }
    }

    fun deleteGroup(groupId: Int, token: String, onSuccess: () -> Unit) {
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
                token = token
            )
        }
    }

    fun changeTrack(groupId: Int, track: TrackInfo, token: String) {
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
                token = token
            )
        }
    }

    fun removeMember(groupId: Int, userId: String, token: String) {
        viewModelScope.launch {
            removeMemberFromGroup(
                groupID = groupId,
                userID = userId,
                token = token,
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

    fun leaveGroup(groupId: Int, token: String) {
        viewModelScope.launch {
            removeMemberFromGroup(
                groupID = groupId,
                token = token,
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

    fun addMember(userId: String, groupId: Int, token: String) {
        viewModelScope.launch {
            Log.d("GroupDetailsViewModel", "Adding member with ID: $userId to group ID: $groupId ")
        }
    }

    fun resetMsgToast() {
        _msgToast.value = ""
    }

    sealed class GroupState {
        object Loading : GroupState()
        data class Success(val groupInfo: GroupInfoResponseDoc) : GroupState()
        data class Error(val message: String) : GroupState()
    }
}