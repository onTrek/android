package com.ontrek.wear.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.OutlinedButton
import androidx.wear.compose.material.Text
import androidx.wear.compose.material3.ScreenScaffold
import com.ontrek.wear.data.PreferencesViewModel

@Composable
fun Login(
    preferencesViewModel: PreferencesViewModel = viewModel(factory = PreferencesViewModel.Factory)
) {
    ScreenScaffold(
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Login,
                contentDescription = "Warning Icon",
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "In order to login, please open the OnTrek app on your phone.",
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            //Only for debug purposes
            OutlinedButton(
                onClick = {
                    preferencesViewModel.saveToken("3e530eef-2f77-418e-89e7-d82537c9109a") //fuck it we ball
                },
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(5.dp),
            ) {
                Text("Skip Login")
            }
        }
    }
}