package com.ontreksmartphone.ui.screens

sealed class Screen(val route: String) {
    object HomeScreen : Screen("HomeScreen")
    object LoginScreen : Screen("LoginScreen")
    //object OtherScreen : Screen("OtherScreen") // Placeholder for other screens
}