package com.ontrek.mobile.screens.connection

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ontrek.mobile.Greeting
import com.ontrek.mobile.utils.components.BottomNavBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionToWear(navController : NavHostController, login : (Context) -> Unit) {
    val context = LocalContext.current


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Friends Screen",
                    )
                },
            )
        },
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        Column (modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Greeting(
                name = "Gioele",
                modifier = Modifier.padding(10.dp)
            )
            OutlinedButton(
                onClick = {
                    login(context)
                },
                modifier = Modifier
                    .fillMaxWidth(0.95f)
            ) {
                Text("Login to Wear")
            }
        }
    }

}