package com.ontrek.mobile.screens.connection

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ontrek.mobile.Greeting

@Composable
fun ConnectionToWear(login : (Context) -> Unit) {
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column (modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
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