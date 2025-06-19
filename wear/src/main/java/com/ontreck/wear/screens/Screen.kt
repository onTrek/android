package com.ontreck.wear.screens

sealed class Screen(val route: String) {
    object MainScreen : Screen("HomeScreen")
    object TrackScreen : Screen("TrackScreen")
}