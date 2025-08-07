package com.ontrek.mobile.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.ontrek.mobile.data.PreferencesViewModel
import com.ontrek.mobile.screens.profile.ProfileScreen
import com.ontrek.mobile.screens.group.GroupsScreen
import com.ontrek.mobile.screens.group.detail.GroupDetailsScreen
import com.ontrek.mobile.screens.search.SearchFriendsScreen
import com.ontrek.mobile.screens.track.TrackScreen
import com.ontrek.mobile.screens.track.detail.TrackDetailScreen

@Composable
fun NavigationStack(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val preferencesViewModel: PreferencesViewModel =
        viewModel(factory = PreferencesViewModel.Factory)

    NavHost(
        navController = navController,
        startDestination = TopLevelScreen.Groups.route,
        modifier = modifier,
    ) {

        navigation(route = TopLevelScreen.Profile.route, startDestination = Screen.Profile.route) {
            composable(route = Screen.Profile.route) {
                ProfileScreen(
                    navController = navController,
                    token = preferencesViewModel.tokenState.value ?: "",
                    clearToken = {
                        preferencesViewModel.clearToken()
                    },
                )
            }
        }

        navigation(route = TopLevelScreen.Tracks.route, startDestination = Screen.Tracks.route) {
            composable(route = Screen.Tracks.route) {
                TrackScreen(navController)
            }
            composable(route = Screen.TrackDetail.route) { backStackEntry ->
                val trackId = backStackEntry.arguments?.getString("trackId") ?: "0"
                TrackDetailScreen(
                    trackId = trackId.toInt(),
                    navController = navController,
                    currentUser = preferencesViewModel.currentUserState.value ?: "",
                )
            }
        }

        navigation(route = TopLevelScreen.Groups.route, startDestination = Screen.Groups.route) {
            composable(route = Screen.Groups.route) {
                GroupsScreen(
                    navController = navController,
                )
            }
            composable(route = Screen.GroupDetails.route) { backStackEntry ->
                val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
                GroupDetailsScreen(
                    groupId = groupId.toIntOrNull() ?: 0,
                    navController = navController,
                    currentUser = preferencesViewModel.currentUserState.value ?: "",
                )
            }
        }

        navigation(route = TopLevelScreen.Search.route, startDestination = Screen.Search.route) {
            composable(route = Screen.Search.route) {
                SearchFriendsScreen(
                    navController = navController
                )
            }
        }
    }
}