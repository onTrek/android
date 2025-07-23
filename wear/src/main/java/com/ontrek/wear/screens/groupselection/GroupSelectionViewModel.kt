package com.ontrek.wear.screens.groupselection

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ontrek.shared.api.hikes.getGroups
import com.ontrek.shared.data.GroupDoc
import com.ontrek.shared.data.TrackInfo
import com.ontrek.wear.data.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class GroupUI(
    val group_id: Int,
    val description: String,
    val created_at: String,
    val created_by: String,
    val member_number: Int,
    val track: TrackInfo
)

class GroupSelectionViewModel(private val db: AppDatabase) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _fetchError = MutableStateFlow<String?>(null)
    val fetchError: StateFlow<String?> = _fetchError

    private val _groupsListState = MutableStateFlow<List<GroupUI>>(listOf())
    val groupListState: StateFlow<List<GroupUI>> = _groupsListState


    fun fetchGroupsList(token: String) {
        Log.d("WearOS", "Fetching data with token: $token")
        _isLoading.value = true


        viewModelScope.launch {
            getGroups(
                token = token,
                onSuccess = ::updateGroups,
                onError = ::setError
            )
        }
    }

    fun updateGroups(data: List<GroupDoc>?) {
        Log.d("WearOS", "Data updated: $data")
        if (data != null) {
            _groupsListState.value = data.map { group ->
                GroupUI(
                    group_id = group.group_id,
                    description = group.description,
                    created_at = group.created_at,
                    created_by = group.created_by,
                    member_number = group.members_number,
                    track = TrackInfo(
                        id = group.track.id,
                        title = group.track.title
                    )
                )
            }
            _fetchError.value = null
        } else {
            Log.e("WearOS", "Data is null")
        }
        _isLoading.value = false
    }

    fun setError(error: String?) {
        Log.e("WearOS", "Error occurred: $error")
        _groupsListState.value = listOf()
        _fetchError.value = error
        _isLoading.value = false
    }

    class Factory(private val db: AppDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GroupSelectionViewModel::class.java)) {
                return GroupSelectionViewModel(db) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}