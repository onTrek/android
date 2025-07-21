package com.ontrek.mobile.screens.hike.groupDetails

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ontrek.shared.api.hikes.changeGPXInGroup
import com.ontrek.shared.api.hikes.deleteGroup
import com.ontrek.shared.api.hikes.getGroupInfo
import com.ontrek.shared.api.track.getTracks
import com.ontrek.shared.data.GroupInfoResponseDoc
import com.ontrek.shared.data.MemberInfo
import com.ontrek.shared.data.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GroupDetailsViewModel : ViewModel() {

    private val _groupState = MutableStateFlow<GroupState>(GroupState.Loading)
    val groupState: StateFlow<GroupState> = _groupState

    private val _membersState = MutableStateFlow<List<MemberInfo>>(emptyList())
    val membersState: StateFlow<List<MemberInfo>> = _membersState

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks

    private val _msgToast = MutableStateFlow("")
    val msgToast: StateFlow<String> = _msgToast

    fun loadGroupDetails(groupId: Int, token: String) {
        Log.d("GroupDetailsViewModel", "Loading group details for groupId: $groupId")
        _groupState.value = GroupState.Loading
        viewModelScope.launch {
            getGroupInfo(
                id = groupId,
                onSuccess = { groupInfo ->
                    if (groupInfo != null) {
                        _groupState.value = GroupState.Success(groupInfo)
                        _membersState.value = groupInfo.members.map { member ->
                            // Converti GroupMember in MemberInfo (è un'approssimazione perché non abbiamo tutti i dati)
                            MemberInfo(
                                user = com.ontrek.shared.data.UserMinimal(
                                    id = member.id,
                                    username = member.username
                                ),
                                accuracy = 0.0,
                                altitude = 0.0,
                                going_to = "",
                                help_request = false,
                                latitude = 0.0,
                                longitude = 0.0,
                                time_stamp = ""
                            )
                        }
                    } else {
                        _groupState.value = GroupState.Error("Gruppo non trovato")
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

    fun changeTrack(groupId: Int, trackId: Int, token: String) {
        viewModelScope.launch {
            changeGPXInGroup(
                id = groupId,
                trackId = trackId,
                onSuccess = { _ ->
                    _msgToast.value = "Track changed successfully"
                    loadGroupDetails(groupId, token)
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
            com.ontrek.shared.api.hikes.removeMemberFromGroup(
                id = groupId,
                userID = userId,
                token = token,
                onSuccess = {
                    _msgToast.value = "Member removed successfully"
                    loadGroupDetails(groupId, token)
                },
                onError = { error ->
                    _msgToast.value = "Error removing member: $error"
                }
            )
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