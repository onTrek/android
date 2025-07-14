package com.ontrek.mobile.screens.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ontrek.mobile.screens.friends.tabs.RequestItem
import com.ontrek.shared.api.friends.getFriendRequests
import com.ontrek.shared.api.friends.getFriends
import com.ontrek.shared.data.FriendRequest
import com.ontrek.shared.data.Friend
import kotlinx.coroutines.delay
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

    // Carica le richieste di amicizia
    fun loadFriendRequests(token: String) {
        viewModelScope.launch {
            _requestsState.value = RequestsState.Loading
            delay(500)
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
    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isEmpty()) {
            _searchState.value = SearchState.Initial
            return
        }

        searchUsers(query)
    }

    // Cerca utenti in base alla query
    private fun searchUsers(query: String) {
        viewModelScope.launch {
            _searchState.value = SearchState.Loading
            delay(500) // Simulazione chiamata API

            // Dati di esempio
            if (query.length < 2) {
                _searchState.value = SearchState.Initial
                return@launch
            }

            val usersList = listOf(
                Friend(5, "Marco Rossi"),
                Friend(6, "Sara Bianchi"),
                Friend(7, "Elena Verdi")
            ).filter { it.username.contains(query, ignoreCase = true) }

            _searchState.value = if (usersList.isEmpty()) {
                SearchState.Empty
            } else {
                SearchState.Success(usersList)
            }
        }
    }

    // Invia richiesta di amicizia
    fun sendFriendRequest(userId: Int, token: String) {
        viewModelScope.launch {
            // Simulazione chiamata API
            delay(800)
        }
    }

    // Accetta richiesta di amicizia
    fun acceptFriendRequest(requestId: Int, token: String) {
        viewModelScope.launch {
            // Simulazione chiamata API
            delay(500)

            // Aggiorna le richieste rimuovendo quella accettata
            val currentRequests = (_requestsState.value as? RequestsState.Success)?.requests ?: return@launch
            val updatedRequests = currentRequests.filterNot { it.id == requestId }
            _requestsState.value = RequestsState.Success(updatedRequests)

            // Ricarica la lista degli amici
            loadFriends(token)
        }
    }

    // Rifiuta richiesta di amicizia
    fun rejectFriendRequest(requestId: Int, token: String) {
        viewModelScope.launch {
            // Simulazione chiamata API
            delay(500)

            // Aggiorna le richieste rimuovendo quella rifiutata
            val currentRequests = (_requestsState.value as? RequestsState.Success)?.requests ?: return@launch
            val updatedRequests = currentRequests.filterNot { it.id == requestId }
            _requestsState.value = RequestsState.Success(updatedRequests)
        }
    }

    // Elimina amicizia
    fun removeFriend(friendId: Int, token: String) {
        viewModelScope.launch {
            // Simulazione chiamata API
            delay(700)

            // Aggiorna la lista degli amici
            val currentFriends = (_friendsState.value as? FriendsState.Success)?.friends ?: return@launch
            val updatedFriends = currentFriends.filterNot { it.id == friendId }
            _friendsState.value = FriendsState.Success(updatedFriends)
        }
    }

    // Stati delle amicizie
    sealed class FriendsState {
        object Loading : FriendsState()
        data class Success(val friends: List<Friend>) : FriendsState()
        data class Error(val message: String) : FriendsState()
    }

    // Stati delle richieste di amicizia
    sealed class RequestsState {
        object Loading : RequestsState()
        data class Success(val requests: List<FriendRequest>) : RequestsState()
        data class Error(val message: String) : RequestsState()
    }

    // Stati della ricerca
    sealed class SearchState {
        object Initial : SearchState()
        object Loading : SearchState()
        object Empty : SearchState()
        data class Success(val users: List<Friend>) : SearchState()
        data class Error(val message: String) : SearchState()
    }

    fun resetMsgToast() {
        _msgToast.value = ""
    }
}