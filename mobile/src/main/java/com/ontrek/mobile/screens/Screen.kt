package com.ontrek.mobile.screens

sealed class Screen(val route: String) {
    object AuthScreen : Screen("AuthScreen")
    object ConnectionScreen : Screen("ConnectionScreen")
    object FriendsScreen : Screen("FriendsScreen")
    object TracksScreen : Screen("TracksScreen")
    object GroupsScreen : Screen("GroupsScreen")
}