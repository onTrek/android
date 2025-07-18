package com.ontrek.mobile.screens.hike

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ontrek.shared.api.hikes.getGroups
import com.ontrek.shared.data.File
import com.ontrek.shared.data.GroupDoc
import com.ontrek.shared.data.GroupIDCreation
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

    fun createGroup(
        description: String,
        trackId: Int,
        token: String,
        onSuccess: (GroupIDCreation) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val group = GroupIDCreation(
                    description = description,
                    file_id = 1,
                )
                onSuccess(group)
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error")
            }
        }
    }

    fun resetMsgToast() {
        _msgToast.value = ""
    }
}