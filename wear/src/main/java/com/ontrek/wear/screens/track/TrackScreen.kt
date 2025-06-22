package com.ontrek.wear.screens.track

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material3.ScreenScaffold
import com.ontrek.wear.screens.Screen
import com.ontrek.wear.screens.track.components.Arrow
import com.ontrek.wear.screens.track.components.ProgressBar
import com.ontrek.wear.screens.track.components.SosButton
import com.ontrek.wear.utils.sensors.CompassSensor

/**
 * Composable function that represents the Track screen.
 * This screen displays a compass arrow indicating the current direction, the progress bar of the track,
 * and a button to trigger an SOS signal.
 * @param navController The navigation controller to handle navigation actions.
 * @param text A string parameter that can be used to display additional information on the screen.
 * @param modifier A [Modifier] to be applied to the screen layout.
 */
@Composable
fun TrackScreen(navController: NavHostController, text: String, modifier: Modifier = Modifier) {
    // Ottiene il contesto corrente per accedere ai sensori del dispositivo
    val context = LocalContext.current

    // Inizializza il sensore della bussola e lo memorizza tra le composizioni
    val compassSensor = remember { CompassSensor(context) }

    // Raccoglie il valore corrente della direzione come stato osservabile
    val direction by compassSensor.direction.collectAsState()

    // Gestisce il ciclo di vita del sensore: avvio all'ingresso nella composizione e arresto all'uscita
    DisposableEffect(compassSensor) {
        // Avvia la lettura dei dati dai sensori
        compassSensor.start()

        // Pulisce le risorse quando il componente viene rimosso dalla composizione
        onDispose {
            compassSensor.stop()
        }
    }

    val progress = 0.75f

    val info: String? = null

    ScreenScaffold(
        timeText = if (info.isNullOrBlank()) {
            {
                TimeText(
                    timeTextStyle = TextStyle(
                        color = MaterialTheme.colors.primary
                    ),
                    modifier = Modifier.padding(5.dp)
                )
            }
        } else null,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier.fillMaxSize()
        ) {


            if (info != null) {
                Text(
                    info,
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(10.dp)
                )
            }

            ProgressBar(
                progress = progress
            )


            Arrow(
                direction = direction,  // Angolo di rotazione basato sui dati del sensore
                modifier = Modifier
                    .fillMaxSize()
                    .padding(50.dp),  // Padding per evitare che la freccia tocchi i bordi dello schermo
            )

            SosButton(
                onSosTriggered = {
                    navController.navigate(route = Screen.SOSScreen.route)
                }
            )
        }
    }
}
