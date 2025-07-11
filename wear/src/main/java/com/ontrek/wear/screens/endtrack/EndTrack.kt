package com.ontrek.wear.screens.endtrack

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.tooling.preview.devices.WearDevices
import com.ontrek.wear.screens.Screen
import com.ontrek.wear.theme.OnTrekTheme

@Composable
fun EndTrack(
    modifier: Modifier,
    navController: NavController,
    trackName: String
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
                Text(
                    text = "Congratulations! \nYou completed the '$trackName' track!",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

                //TODO()
                //PERCHÈ È VIOLAAAAAAAAAA
                Button(
                    onClick = {
                        navController.navigate(Screen.MainScreen.route) {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Back to Home")
                }
        }
    }
}


@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun CalibrationPreview() {
    OnTrekTheme {
        EndTrack(
            modifier = Modifier.fillMaxSize(),
            navController = NavController(context = androidx.compose.ui.platform.LocalContext.current),
            trackName = "Trek di prova"
        )
    }
}