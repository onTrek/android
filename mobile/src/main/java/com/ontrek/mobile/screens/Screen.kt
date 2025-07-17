package com.ontrek.mobile.screens

sealed class Screen(val route: String) {
        object Profile : Screen("ProfileScreen")
        object Friends : Screen("FriendsScreen")
        object Tracks : Screen("TracksScreen")
        object Hikes : Screen("HikesScreen")

        object Group : Screen("GroupDetailsScreen/{groupId}") {
            fun createRoute(groupId: Int) = "GroupDetailsScreen/$groupId"
        }
        object TrackDetail : Screen("TrackDetailScreen/{trackId}") {
            fun createRoute(trackId: String) = "TrackDetailScreen/$trackId"
        }
    }

