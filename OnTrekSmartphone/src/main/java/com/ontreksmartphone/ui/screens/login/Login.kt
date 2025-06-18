package com.ontreksmartphone.ui.screens.login

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ontreksmartphone.ui.screens.Screen
import com.ontreksmartphone.ui.theme.OnTrekSmartwatchTheme

@Composable
fun LoginScreen(
    navController: NavHostController
) {
    var name = "Gioele"
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { innerPadding ->
           //Button for login action
            Button(
                onClick = {navController.navigate(route = Screen.HomeScreen.route + "?text=${name}") },
                modifier = Modifier.padding(innerPadding)
            ) {
                Text(text = "Login")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    OnTrekSmartwatchTheme {
        LoginScreen(rememberNavController())
    }
}