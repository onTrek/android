package com.ontrek.mobile.screens

sealed class Screen(val route: String) {
        object Profile : Screen("ProfileScreen")
        object Friends : Screen("FriendsScreen")
        object Tracks : Screen("TracksScreen")
        object Hikes : Screen("HikesScreen")
    }

