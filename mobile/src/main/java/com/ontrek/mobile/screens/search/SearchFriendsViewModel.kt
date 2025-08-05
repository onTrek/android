package com.ontrek.mobile.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ontrek.shared.api.friends.sendFriendRequest
import com.ontrek.shared.api.search.searchUsers
import com.ontrek.shared.data.UserMinimal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchFriendsViewModel : ViewModel() {
    private val _msgToast = MutableStateFlow("")
    val msgToast: StateFlow<String> = _msgToast

    private val _searchState = MutableStateFlow<SearchState>(SearchState.Empty)
    val searchState: StateFlow<SearchState> = _searchState


    // Cerca utenti in base alla query
    fun search(query: String) {
        viewModelScope.launch {
            _searchState.value = SearchState.Loading
            if (query.length < 3) {
                _searchState.value = SearchState.Empty
                return@launch
            }

            searchUsers(
                query = query,
                onSuccess = { users ->
                    _searchState.value = if (users.isNullOrEmpty()) {
                        SearchState.Empty
                    } else {
                        SearchState.Success(users)
                    }
                },
                onError = { error ->
                    _searchState.value = SearchState.Error(error)
                }
            )
        }
    }

    fun sendRequest(userID: String) {
        viewModelScope.launch {
            sendFriendRequest(
                id = userID,
                onSuccess = { message ->
                    _msgToast.value = message
                    // Aggiorna lo stato dell'utente nella lista dei risultati
                    val currentState = _searchState.value
                    if (currentState is SearchState.Success) {
                        val updatedUsers = currentState.users.map { user ->
                            if (user.id == userID) {
                                user.copy(state = -1)  // Cambia state da 0 a -1 per questo utente
                            } else {
                                user
                            }
                        }
                        _searchState.value = SearchState.Success(updatedUsers)
                    }
                },
                onError = { error ->
                    _msgToast.value = error
                }
            )
        }
    }

    // Stati della ricerca
    sealed class SearchState {
        object Loading : SearchState()
        object Empty : SearchState()
        data class Success(val users: List<UserMinimal>) : SearchState()
        data class Error(val message: String) : SearchState()
    }

    fun clearMsgToast() {
        _msgToast.value = ""
    }
}