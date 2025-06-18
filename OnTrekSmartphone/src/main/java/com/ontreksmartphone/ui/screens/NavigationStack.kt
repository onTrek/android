// NavigationStack.kt
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ontreksmartphone.ui.screens.Screen
import com.ontreksmartphone.ui.screens.home.HomeScreen
import com.ontreksmartphone.ui.screens.login.LoginScreen

@Composable
fun NavigationStack(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.LoginScreen.route) {
        composable(route = Screen.LoginScreen.route) {
            LoginScreen(
                navController = navController
            )
        }
        composable(
            route = Screen.HomeScreen.route + "?text={text}",
            arguments = listOf(
                navArgument("text") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) {
            HomeScreen(
                name = it.arguments?.getString("text").toString(),
                modifier = modifier
            )
        }
    }
}