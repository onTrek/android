package com.ontrek.mobile.screens

sealed class Screen(val route: String) {
        object Connection : Screen("ConnectionScreen")
        object Friends : Screen("FriendsScreen")
        object Tracks : Screen("TracksScreen")
        object Hikes : Screen("HikesScreen")
    }

