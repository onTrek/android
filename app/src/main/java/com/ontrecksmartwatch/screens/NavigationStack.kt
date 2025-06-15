// NavigationStack.kt
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ontrecksmartwatch.screens.Screen
import com.ontrecksmartwatch.screens.home.TrackSelectionScreen
import com.ontrecksmartwatch.screens.track.TrackScreen

@Composable
fun NavigationStack() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.MainScreen.route) {
        composable(route = Screen.MainScreen.route) {
            TrackSelectionScreen(navController = navController)
        }
        composable(
            route = Screen.TrackScreen.route + "?text={text}",
            arguments = listOf(
                navArgument("text") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) {
            TrackScreen(text = it.arguments?.getString("text").toString())
        }
    }
}