package com.ontrek.mobile.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ontrek.mobile.data.PreferencesViewModel
import com.ontrek.mobile.screens.profile.Profile
import com.ontrek.mobile.screens.friends.FriendsScreen
import com.ontrek.mobile.screens.hike.GroupScreen
import com.ontrek.mobile.screens.track.TrackScreen
import com.ontrek.mobile.screens.track.detail.TrackDetailScreen

@Composable
fun NavigationStack(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val preferencesViewModel: PreferencesViewModel =
        viewModel(factory = PreferencesViewModel.Factory)
    NavHost(
        navController = navController,
        startDestination = Screen.Hikes.route,
        modifier = modifier,
    ) {
        composable(route = Screen.Profile.route) {
            Profile(navController, preferencesViewModel.tokenState)
        }
        composable(route = Screen.Tracks.route) {
            TrackScreen(navController, token = preferencesViewModel.tokenState.value ?: "")
        }
        composable(route = Screen.TrackDetail.route) { backStackEntry ->
            val trackId = backStackEntry.arguments?.getString("trackId") ?: ""
            TrackDetailScreen(
                trackId = trackId,
                navController = navController,
                token = preferencesViewModel.tokenState.value ?: ""
            )
        }
        composable(route = Screen.Hikes.route) {
            GroupScreen(navController)
        }
        composable(route = Screen.Friends.route) {
            FriendsScreen(navController)
        }
    }
}