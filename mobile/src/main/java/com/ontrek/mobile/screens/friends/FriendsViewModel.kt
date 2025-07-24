package com.ontrek.mobile.screens.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ontrek.shared.api.friends.acceptFriendRequest
import com.ontrek.shared.api.friends.deleteFriend
import com.ontrek.shared.api.friends.deleteFriendRequest
import com.ontrek.shared.api.friends.getFriendRequests
import com.ontrek.shared.api.friends.getFriends
import com.ontrek.shared.api.friends.getSentFriendRequest
import com.ontrek.shared.api.friends.sendFriendRequest
import com.ontrek.shared.api.search.searchUsers
import com.ontrek.shared.data.FriendRequest
import com.ontrek.shared.data.UserMinimal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FriendsViewModel : ViewModel() {

    // Stato degli amici
    private val _friendsState = MutableStateFlow<FriendsState>(FriendsState.Loading)
    val friendsState: StateFlow<FriendsState> = _friendsState

    // Stato delle richieste di amicizia
    private val _requestsState = MutableStateFlow<RequestsState>(RequestsState.Loading)
    val requestsState: StateFlow<RequestsState> = _requestsState

    // Stato della ricerca utenti
    private val _searchState = MutableStateFlow<SearchState>(SearchState.Initial)
    val searchState: StateFlow<SearchState> = _searchState

    // Query di ricerca
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _msgToast = MutableStateFlow("")
    val msgToast: StateFlow<String> = _msgToast

    private val _sentFriendRequests = MutableStateFlow<SentRequestsState>(SentRequestsState.Loading)
    val sentFriendRequests: StateFlow<SentRequestsState> = _sentFriendRequests

    private val _isCharge = MutableStateFlow(false)
    val isCharge: StateFlow<Boolean> = _isCharge

    // Carica la lista degli amici
    fun loadFriends(token: String) {
        viewModelScope.launch {
            _friendsState.value = FriendsState.Loading
            getFriends(
                token = token,
                onSuccess = { friends ->
                    _friendsState.value = FriendsState.Success(friends ?: emptyList())
                },
                onError = { error ->
                    _friendsState.value = FriendsState.Error(error)
                }
            )
        }
    }

    fun loadSentFriendRequests(token: String) {
        viewModelScope.launch {
            _sentFriendRequests.value = SentRequestsState.Loading
            getSentFriendRequest(
                token = token,
                onSuccess = { requests ->
                    _sentFriendRequests.value = SentRequestsState.Success(requests ?: emptyList())
                },
                onError = { error ->
                    _sentFriendRequests.value = SentRequestsState.Error(error)
                }
            )
        }
    }

    // Carica le richieste di amicizia
    fun loadFriendRequests(token: String) {
        viewModelScope.launch {
            _requestsState.value = RequestsState.Loading
            getFriendRequests(
                token = token,
                onSuccess = { requests ->
                    _requestsState.value = RequestsState.Success(requests ?: emptyList())
                },
                onError = { error ->
                    _requestsState.value = RequestsState.Error(error)
                }
            )
        }
    }

    // Aggiorna la query di ricerca
    fun onSearchQueryChange(query: String, token: String) {
        _searchQuery.value = query
        if (query.isEmpty()) {
            _searchState.value = SearchState.Initial
            return
        }

        search(query, token)
    }

    // Cerca utenti in base alla query
    private fun search(query: String, token: String) {
        viewModelScope.launch {
            _searchState.value = SearchState.Loading

            // Se la query è troppo corta, ritorna subito
            if (query.length < 2) {
                _searchState.value = SearchState.Initial
                return@launch
            }

            // Chiamata all'API reale
            searchUsers(
                token = token,
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
    // Invia richiesta di amicizia
    fun sendFriendRequest(user: UserMinimal, token: String) {
        viewModelScope.launch {
            // Simulazione chiamata API
            sendFriendRequest(
                token = token,
                id = user.id,
                onSuccess = { message ->
                    _msgToast.value = message
                    addSentFriendRequest(
                        FriendRequest(
                            id = user.id,
                            username = user.username,
                            date = System.currentTimeMillis().toString()
                        )
                    )
                    setIsCharge()
                },
                onError = { error ->
                    _msgToast.value = error
                }
            )
        }
    }

    // Accetta richiesta di amicizia
    fun acceptRequest(user: FriendRequest, token: String) {
        viewModelScope.launch {
            // Simulazione chiamata API
            acceptFriendRequest(
                token = token,
                id = user.id,
                onSuccess = { message ->
                    _msgToast.value = message
                    removeRequestFromList(user.id)
                    val friend = UserMinimal(
                        id = user.id,
                        username = user.username,
                    )
                    addFriendToList(friend)
                },
                onError = { error ->
                    _msgToast.value = error
                }
            )
        }
    }

    // Rifiuta richiesta di amicizia
    fun rejectFriendRequest(requestId: String, token: String) {
        viewModelScope.launch {
            // Simulazione chiamata API
            deleteFriendRequest(
                token = token,
                id = requestId,
                onSuccess = { message ->
                    _msgToast.value = message
                    removeRequestFromList(requestId)
                },
                onError = { error ->
                    _msgToast.value = error
                }
            )
        }
    }

    // Elimina amicizia
    fun removeFriend(friendId: String, token: String) {
        viewModelScope.launch {
            // Simulazione chiamata API
            deleteFriend(
                token = token,
                id = friendId,
                onSuccess = { message ->
                    _msgToast.value = message
                    removeFriendFromList(friendId)
                },
                onError = { error ->
                    _msgToast.value = error
                }
            )
        }
    }

    fun addSentFriendRequest(request: FriendRequest) {
        viewModelScope.launch {
            _sentFriendRequests.value = when (val currentState = _sentFriendRequests.value) {
                is SentRequestsState.Success -> {
                    val updatedRequests = currentState.requests.toMutableList().apply { add(request) }
                    SentRequestsState.Success(updatedRequests)
                }
                else -> currentState
            }
        }
    }

    fun addFriendToList(friend: UserMinimal) {
        viewModelScope.launch {
            _friendsState.value = when (val currentState = _friendsState.value) {
                is FriendsState.Success -> {
                    val updatedFriends = currentState.friends.toMutableList().apply { add(friend) }
                    FriendsState.Success(updatedFriends)
                }
                else -> currentState
            }
        }
    }

    fun removeFriendFromList(friendId: String) {
        viewModelScope.launch {
            _friendsState.value = when (val currentState = _friendsState.value) {
                is FriendsState.Success -> {
                    val updatedFriends = currentState.friends.filter { it.id != friendId }
                    FriendsState.Success(updatedFriends)
                }
                else -> currentState
            }
        }
    }

    fun removeRequestFromList(requestId: String) {
        viewModelScope.launch {
            _requestsState.value = when (val currentState = _requestsState.value) {
                is RequestsState.Success -> {
                    val updatedRequests = currentState.requests.filter { it.id != requestId }
                    RequestsState.Success(updatedRequests)
                }
                else -> currentState
            }
        }
    }

    // Stati delle amicizie
    sealed class FriendsState {
        object Loading : FriendsState()
        data class Success(val friends: List<UserMinimal>) : FriendsState()
        data class Error(val message: String) : FriendsState()
    }

    // Stati delle richieste di amicizia
    sealed class RequestsState {
        object Loading : RequestsState()
        data class Success(val requests: List<FriendRequest>) : RequestsState()
        data class Error(val message: String) : RequestsState()
    }

    sealed class SentRequestsState {
        object Loading : SentRequestsState()
        data class Success(val requests: List<FriendRequest>) : SentRequestsState()
        data class Error(val message: String) : SentRequestsState()
    }

    // Stati della ricerca
    sealed class SearchState {
        object Initial : SearchState()
        object Loading : SearchState()
        object Empty : SearchState()
        data class Success(val users: List<UserMinimal>) : SearchState()
        data class Error(val message: String) : SearchState()
    }

    fun resetMsgToast() {
        _msgToast.value = ""
    }

    fun setIsCharge() {
        _isCharge.value = !_isCharge.value
    }
}