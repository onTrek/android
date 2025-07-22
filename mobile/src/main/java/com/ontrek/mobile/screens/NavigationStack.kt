package com.ontrek.mobile.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
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
        startDestination = TopLevelScreen.Hikes.route,
        modifier = modifier,
    ) {

        navigation(route = TopLevelScreen.Profile.route, startDestination = Screen.Profile.route) {
            composable(route = Screen.Profile.route) {
                Profile(navController, preferencesViewModel.tokenState)
            }
        }

        navigation(route = TopLevelScreen.Tracks.route, startDestination = Screen.Tracks.route) {
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
        }

        navigation(route = TopLevelScreen.Hikes.route, startDestination = Screen.Hikes.route) {
            composable(route = Screen.Hikes.route) {
                GroupScreen(navController)
            }
        }

        navigation(route = TopLevelScreen.Friends.route, startDestination = Screen.Friends.route) {
            composable(route = Screen.Friends.route) {
                FriendsScreen(
                    token = preferencesViewModel.tokenState.value ?: "",
                    navController = navController
                )
            }
        }
    }
}