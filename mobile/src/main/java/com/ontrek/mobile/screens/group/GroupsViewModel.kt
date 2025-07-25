package com.ontrek.mobile.screens.group

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.ontrek.mobile.screens.Screen
import com.ontrek.shared.api.groups.createGroup
import com.ontrek.shared.api.groups.getGroups
import com.ontrek.shared.data.GroupDoc
import com.ontrek.shared.data.GroupIDCreation
import com.ontrek.shared.data.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GroupsViewModel : ViewModel() {
    sealed class GroupsState {
        object Loading : GroupsState()
        data class Success(val groups: List<GroupDoc>) : GroupsState()
        data class Error(val message: String) : GroupsState()
    }

    sealed class TrackState {
        object Loading : TrackState()
        data class Success(val tracks: List<Track>) : TrackState()
        data class Error(val message: String) : TrackState()
    }

    private val _listGroup = MutableStateFlow<GroupsState>(GroupsState.Loading)
    val listGroup: StateFlow<GroupsState> = _listGroup

    private val _msgToast = MutableStateFlow("")
    val msgToast: StateFlow<String> = _msgToast


    fun loadGroups() {
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
            )
        }
    }

    fun addGroup(
        description: String,
        navController: NavHostController
    ) {
        viewModelScope.launch {
            createGroup(
                group = GroupIDCreation(description = description),
                onSuccess = { groupId ->
                    _msgToast.value = "Group created successfully"
                    Log.d("GroupsViewModel", "Group created with ID: $groupId")
                    navController.navigate(Screen.GroupDetails.createRoute(groupId?.group_id ?: 0))
                },
                onError = { error ->
                    _msgToast.value = error
                },
            )
        }
    }

    fun clearMsgToast() {
        _msgToast.value = ""
    }

}