package com.ontrek.mobile.screens.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    // Carica la lista degli amici
    fun loadFriends(token: String) {
        viewModelScope.launch {
            _friendsState.value = FriendsState.Loading
            delay(1000) // Simulazione chiamata API

            // Dati di esempio
            val friendsList = listOf(
                Friend(1, "Marco Rossi", "marco.rossi", "https://randomuser.me/api/portraits/men/1.jpg"),
                Friend(2, "Giulia Bianchi", "giulia.b", "https://randomuser.me/api/portraits/women/2.jpg"),
                Friend(3, "Luca Verdi", "luca.verdi", "https://randomuser.me/api/portraits/men/3.jpg"),
                Friend(4, "Anna Neri", "anna_neri", "https://randomuser.me/api/portraits/women/4.jpg")
            )

            _friendsState.value = FriendsState.Success(friendsList)
        }
    }

    // Carica le richieste di amicizia
    fun loadFriendRequests(token: String) {
        viewModelScope.launch {
            _requestsState.value = RequestsState.Loading
            delay(800) // Simulazione chiamata API

            // Dati di esempio
            val requestsList = listOf(
                FriendRequest(5, "Paolo Bruno", "paolo.bruno", "https://randomuser.me/api/portraits/men/5.jpg", System.currentTimeMillis() - 3600000),
                FriendRequest(6, "Sofia Gialli", "sofia.gialli", "https://randomuser.me/api/portraits/women/6.jpg", System.currentTimeMillis() - 86400000)
            )

            _requestsState.value = RequestsState.Success(requestsList)
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
                User(7, "Mario $query", "mario.$query", "https://randomuser.me/api/portraits/men/7.jpg"),
                User(8, "Laura $query", "laura.$query", "https://randomuser.me/api/portraits/women/8.jpg")
            )

            _searchState.value = if (usersList.isEmpty()) {
                SearchState.Empty
            } else {
                SearchState.Success(usersList)
            }
        }
    }

    // Invia richiesta di amicizia
    fun sendFriendRequest(userId: Int, token: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            // Simulazione chiamata API
            delay(800)
            onSuccess()
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

    // Modelli di dati
    data class Friend(val id: Int, val name: String, val username: String, val avatarUrl: String)
    data class User(val id: Int, val name: String, val username: String, val avatarUrl: String)
    data class FriendRequest(val id: Int, val name: String, val username: String, val avatarUrl: String, val timestamp: Long)

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
        data class Success(val users: List<User>) : SearchState()
        data class Error(val message: String) : SearchState()
    }
}