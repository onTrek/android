package com.ontrek.mobile.screens

sealed class Screen(val route: String) {
    object LoginScreen : Screen("LoginScreen")
    object SignUpScreen : Screen("SignUpScreen")

    object TracksScreen : Screen("TracksScreen")
    object TrackDetailsScreen : Screen("TrackDetailsScreen")
    object AddTrackScreen : Screen("AddTrackScreen")

    object FriendsScreen : Screen("FriendsScreen")

    object SessionsScreen : Screen("SessionsScreen")
    object SessionDetailsScreen : Screen("SessionDetailsScreen")

}