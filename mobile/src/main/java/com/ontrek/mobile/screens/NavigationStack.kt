package com.ontrek.mobile.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ontrek.mobile.data.PreferencesViewModel
import com.ontrek.mobile.screens.connection.ConnectionToWear
import com.ontrek.mobile.screens.friends.FriendsScreen
import com.ontrek.mobile.screens.group.GroupScreen
import com.ontrek.mobile.screens.track.TrackScreen
import com.ontrek.mobile.utils.components.BottomNavBar

@Composable
fun NavigationStack(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { /* Placeholder for top bar */ },
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        NavigationHost(
            navController = navController,
            modifier = modifier.padding(innerPadding)
        )
    }
}

@Composable
fun NavigationHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    preferencesViewModel: PreferencesViewModel = viewModel(factory = PreferencesViewModel.Factory),
) {
    NavHost(
        navController = navController,
        startDestination = Screen.GroupsScreen.route,
        modifier = modifier,
    ) {
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
