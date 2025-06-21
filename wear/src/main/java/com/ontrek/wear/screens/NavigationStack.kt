// NavigationStack.kt
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ontrek.wear.MainViewModel
import com.ontrek.wear.screens.Screen
import com.ontrek.wear.screens.home.TrackSelectionScreen
import com.ontrek.wear.screens.track.TrackScreen

@Composable
fun NavigationStack(mainViewModel: MainViewModel, modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.MainScreen.route) {
        composable(route = Screen.MainScreen.route) {
            TrackSelectionScreen(
                navController = navController,
                mainViewModel = mainViewModel,
                modifier = modifier
            )
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
            TrackScreen(
                text = it.arguments?.getString("text").toString(),
                modifier = modifier
            )
        }
    }
}