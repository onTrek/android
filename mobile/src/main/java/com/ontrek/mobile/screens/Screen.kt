package com.ontrek.mobile.screens

sealed class Screen(val route: String) {
    object Profile : Screen("ProfileScreen")
    object Search : Screen("SearchScreen")
    object Tracks : Screen("TracksScreen")
    object Groups : Screen("GroupsScreen")
    object TrackDetail : Screen("TrackDetailScreen/{trackId}") {
        fun createRoute(trackId: String) = "TrackDetailScreen/$trackId"
    }

    object GroupDetails : Screen("GroupDetailsScreen/{groupId}") {
        fun createRoute(groupId: Int) = "GroupDetailsScreen/$groupId"
    }
}


sealed class TopLevelScreen(route: String, val title: String) : Screen(route) {
    object Profile : TopLevelScreen("ProfileSection", "Profile")
    object Search : TopLevelScreen("SearchSection", "Search")
    object Tracks : TopLevelScreen("TracksSection", "Tracks")
    object Groups : TopLevelScreen("GroupsSection", "Groups")

}
