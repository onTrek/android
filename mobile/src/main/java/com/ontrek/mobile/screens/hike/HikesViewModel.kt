package com.ontrek.mobile.screens.hike

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ontrek.shared.api.hikes.getGroups
import com.ontrek.shared.data.GroupDoc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HikesViewModel : ViewModel() {
    private val _groups = MutableStateFlow<List<GroupDoc>>(emptyList())
    val groups: StateFlow<List<GroupDoc>> = _groups

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _msgToast = MutableStateFlow("")
    val msgToast: StateFlow<String> = _msgToast

    fun loadGroups(token: String) {
        _isLoading.value = true
        viewModelScope.launch {
            getGroups(
                onSuccess = { groupsList ->
                    _groups.value = groupsList ?: emptyList()
                    _isLoading.value = false
                },
                onError = { error ->
                    _msgToast.value = error
                    _isLoading.value = false
                },
                token = token
            )
        }
    }

    fun resetMsgToast() {
        _msgToast.value = ""
    }
}