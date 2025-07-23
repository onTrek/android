package com.ontrek.mobile.screens

sealed class Screen(val route: String) {
    object Profile : Screen("ProfileScreen")
    object Friends : Screen("FriendsScreen")
    object Tracks : Screen("TracksScreen")
    object Hikes : Screen("HikesScreen")
    object TrackDetail : Screen("TrackDetailScreen/{trackId}") {
        fun createRoute(trackId: String) = "TrackDetailScreen/$trackId"
    }

    object GroupDetails : Screen("GroupDetailsScreen/{groupId}") {
        fun createRoute(groupId: Int) = "GroupDetailsScreen/$groupId"
    }
}


sealed class TopLevelScreen(route: String, val title: String) : Screen(route) {
    object Profile : TopLevelScreen("ProfileSection", "Profile")
    object Friends : TopLevelScreen("FriendsSection", "Friends")
    object Tracks : TopLevelScreen("TracksSection", "Tracks")
    object Hikes : TopLevelScreen("HikesSection", "Groups")

}

