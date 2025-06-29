package com.ontrek.mobile.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ontrek.mobile.data.PreferencesViewModel
import com.ontrek.mobile.screens.connection.ConnectionToWear
import com.ontrek.mobile.screens.friends.FriendsScreen
import com.ontrek.mobile.screens.group.GroupScreen
import com.ontrek.mobile.screens.track.TrackScreen

@Composable
fun NavigationStack(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    // Initialize the preferences view model to access data store
    val preferencesViewModel: PreferencesViewModel =
        viewModel(factory = PreferencesViewModel.Factory)

    NavHost(navController = navController, startDestination = Screen.ConnectionScreen.route) {
        composable(route = Screen.ConnectionScreen.route) {
            ConnectionToWear { }
        }
        composable(route = Screen.TracksScreen.route) {
            TrackScreen()
        }
        composable(route = Screen.GroupsScreen.route) {
            GroupScreen()
        }
        composable(route = Screen.FriendsScreen.route) {
            FriendsScreen()
        }
    }
}