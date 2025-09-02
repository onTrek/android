package com.ontrek.wear.screens

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ontrek.wear.MainActivity
import com.ontrek.wear.data.DatabaseProvider
import com.ontrek.wear.data.PreferencesViewModel
import com.ontrek.wear.screens.groupselection.GroupSelectionScreen
import com.ontrek.wear.screens.homepage.Homepage
import com.ontrek.wear.screens.sos.SOSScreen
import com.ontrek.wear.screens.track.TrackScreen
import com.ontrek.wear.screens.trackselection.TrackSelectionScreen

@Composable
fun NavigationStack(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    // Initialize the preferences view model to access data store
    val preferencesViewModel: PreferencesViewModel =
        viewModel(factory = PreferencesViewModel.Factory)

    val db = DatabaseProvider.getDatabase(LocalContext.current.applicationContext)

    val context = LocalContext.current
    val activity = LocalActivity.current as MainActivity
    val trackToStart by activity.trackToStart.collectAsStateWithLifecycle()

    LaunchedEffect(trackToStart) {
        trackToStart?.let {
            val trackId = it.first
            val sessionId = it.second
            val trackName = it.third

            Log.d("StartTrack", "Track to start: $trackId, session: $sessionId, name: $trackName")

            // check if track exists in db
            if (db.trackDao().getTrackById(trackToStart!!.first) != null) {
                navController.navigate(
                    Screen.TrackScreen.route + "?trackID=$trackId&trackName=$trackName&sessionID=${sessionId ?: ""}"
                )
            } else {
                Toast.makeText(context, "First download the track", Toast.LENGTH_SHORT).show()
                navController.navigate(if (sessionId != null) Screen.GroupSelectionScreen.route else Screen.TrackSelectionScreen.route)
            }

            activity.resetTrackToStart()
        }
    }

    NavHost(navController = navController, startDestination = Screen.MainScreen.route) {
        composable(route = Screen.MainScreen.route) {
            Homepage(onNavigateToTracks = {
                navController.navigate(Screen.TrackSelectionScreen.route)
            }, onNavigateToGroups = {
                navController.navigate(Screen.GroupSelectionScreen.route)
            }, onLogout = {
                preferencesViewModel.clearToken()
            })
        }
        composable(route = Screen.TrackSelectionScreen.route) {
            TrackSelectionScreen(
                navController = navController,
            )
        }
        composable(route = Screen.GroupSelectionScreen.route) {
            GroupSelectionScreen(
                navigateToTrack = { trackID, trackName, sessionID ->
                    navController.navigate(
                        Screen.TrackScreen.route + "?trackID=$trackID&trackName=$trackName&sessionID=$sessionID"
                    )
                })
        }
        composable(
            route = Screen.TrackScreen.route + "?trackID={trackID}&trackName={trackName}&sessionID={sessionID}",
            arguments = listOf(navArgument("trackID") {
                type = NavType.StringType
                nullable = false
            }, navArgument("trackName") {
                type = NavType.StringType
                nullable = false
                defaultValue = ""
            }, navArgument("sessionID") {
                type = NavType.StringType
                nullable = true
                defaultValue = ""
            })
        ) {

            TrackScreen(
                navController = navController,
                trackID = it.arguments?.getString("trackID").toString(),
                trackName = it.arguments?.getString("trackName").toString(),
                sessionID = it.arguments?.getString("sessionID").toString(),
                currentUserId = preferencesViewModel.currentUserState.value ?: "",
                modifier = modifier
            )
        }
        composable(
            route = Screen.SOSScreen.route + "?sessionID={sessionID}&currentUserId={currentUserId}",
            arguments = listOf(navArgument("sessionID") {
                type = NavType.StringType
                nullable = false
            }, navArgument("currentUserId") {
                type = NavType.StringType
                nullable = false
            })
        ) {
            SOSScreen(
                navController = navController,
                sessionID = it.arguments?.getString("sessionID").toString(),
                currentUserId = preferencesViewModel.currentUserState.value ?: ""
            )
        }
    }
}