package com.ontrek.mobile.utils.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Hiking
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ontrek.mobile.screens.TopLevelScreen


data class BottomNavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val hasNews: Boolean = false,
    val badgeCount: Int? = null,
    val route: String = title,
) {
    constructor(
        topLevelScreen: TopLevelScreen,
        selectedIcon: ImageVector,
        unselectedIcon: ImageVector
    ) : this(
        title = topLevelScreen.title,
        route = topLevelScreen.route,
        selectedIcon = selectedIcon,
        unselectedIcon = selectedIcon,
    )
}

@Composable
fun BottomNavBar(navController: NavController) {

    val topLevelRoutes = listOf(

        BottomNavItem(
            topLevelScreen = TopLevelScreen.Hikes,
            selectedIcon = Icons.Filled.Hiking,
            unselectedIcon = Icons.Outlined.Hiking,
        ),
        BottomNavItem(
            topLevelScreen = TopLevelScreen.Tracks,
            selectedIcon = Icons.Filled.Route,
            unselectedIcon = Icons.Outlined.Route,
        ),
        BottomNavItem(
            topLevelScreen = TopLevelScreen.Friends,
            selectedIcon = Icons.Filled.Group,
            unselectedIcon = Icons.Outlined.Group,
        ),
        BottomNavItem(
            topLevelScreen = TopLevelScreen.Profile,
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person,
        ),
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        topLevelRoutes.forEach { topLevelRoute ->
            val selected =
                currentDestination?.hierarchy?.any { it.route == topLevelRoute.route } == true
            NavigationBarItem(
                icon = {
                    BadgedBox(
                        badge = {
                            if (topLevelRoute.badgeCount != null) {
                                Badge { Text(text = topLevelRoute.badgeCount.toString()) }
                            } else if (topLevelRoute.hasNews) {
                                Badge()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (selected) topLevelRoute.selectedIcon else topLevelRoute.unselectedIcon,
                            contentDescription = topLevelRoute.title
                        )
                    }
                },
                label = { Text(topLevelRoute.title) },
                selected = selected,
                onClick = {
                    navController.navigate(topLevelRoute.route) {
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}
