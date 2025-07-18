package com.ontrek.mobile.screens.hike

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ontrek.shared.api.hikes.getGroups
import com.ontrek.shared.data.File
import com.ontrek.shared.data.GroupDoc
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
                    val groups = groupsList ?: emptyList()
                    // Verifica se aggiungere un gruppo di test
                    val finalGroups = if (groups.isEmpty()) {
                        Log.d("HikesViewModel", "No groups found, adding a test group")
                        listOf(createTestGroup())
                    } else {
                        groups
                    }

                    _listGroup.value = GroupsState.Success(finalGroups)
                    Log.d("HikesViewModel", "Groups loaded: ${finalGroups.size}")
                },
                onError = { error ->
                    _listGroup.value = GroupsState.Error(error)
                    _msgToast.value = error
                },
                token = token
            )
        }
    }

    private fun createTestGroup(): GroupDoc {
        return GroupDoc(
            created_at = "2023-10-01T12:00:00Z",
            created_by = "test_user",
            description = "Test Group",
            group_id = 0,
            file = File(
                file_id = 0,
                filename = "test_image.jpg",
            ),
        )
    }

    fun resetMsgToast() {
        _msgToast.value = ""
    }
}