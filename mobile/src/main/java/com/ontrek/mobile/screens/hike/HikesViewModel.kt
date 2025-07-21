package com.ontrek.mobile.screens.hike

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ontrek.shared.api.hikes.createGroup
import com.ontrek.shared.api.hikes.getGroups
import com.ontrek.shared.api.track.getTracks
import com.ontrek.shared.data.GroupDoc
import com.ontrek.shared.data.GroupIDCreation
import com.ontrek.shared.data.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HikesViewModel : ViewModel() {
    sealed class GroupsState {
        object Loading : GroupsState()
        data class Success(val groups: List<GroupDoc>) : GroupsState()
        data class Error(val message: String) : GroupsState()
    }
    
    private val _listGroup = MutableStateFlow<GroupsState>(GroupsState.Success(emptyList()))
    val listGroup: StateFlow<GroupsState> = _listGroup

    private val _msgToast = MutableStateFlow("")
    val msgToast: StateFlow<String> = _msgToast

    private val _isCharged = MutableStateFlow(false)
    val isCharged: StateFlow<Boolean> = _isCharged

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks

    fun loadGroups(token: String) {
        _listGroup.value = GroupsState.Loading
        viewModelScope.launch {
            getGroups(
                onSuccess = { groupsList ->
                    _listGroup.value = GroupsState.Success(groupsList ?: emptyList())
                },
                onError = { error ->
                    _listGroup.value = GroupsState.Error(error)
                    _msgToast.value = error
                },
                token = token
            )
        }
    }

    fun loadTracks(token: String) {
        _tracks.value = emptyList()
        viewModelScope.launch {
            getTracks(
                onSuccess = { tracksList ->
                    _tracks.value = tracksList ?: emptyList()
                },
                onError = { error ->
                    _msgToast.value = error
                },
                token = token
            )
        }
    }

    fun addGroup(
        description: String,
        trackId: Int,
        token: String,
    ) {
        viewModelScope.launch {
            createGroup(
                group = GroupIDCreation(description = description, file_id = trackId),
                onSuccess = { groupId ->
                    _msgToast.value = "Group created successfully"
                    loadGroups(token)
                },
                onError = { error ->
                    _msgToast.value = error
                },
                token = token
            )
        }
    }

    fun resetMsgToast() {
        _msgToast.value = ""
    }

    fun setCharged() {
        _isCharged.value = !_isCharged.value
    }
}