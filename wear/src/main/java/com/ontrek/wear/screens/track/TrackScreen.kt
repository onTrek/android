package com.ontrek.wear.screens.track

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.curvedText
import androidx.wear.tooling.preview.devices.WearDevices
import com.ontrek.wear.R
import com.ontrek.wear.screens.Screen
import com.ontrek.wear.screens.track.components.Arrow
import com.ontrek.wear.screens.track.components.SosButton
import com.ontrek.wear.theme.OnTrekTheme
import com.ontrek.wear.utils.components.Loading
import com.ontrek.wear.utils.media.GifRenderer
import com.ontrek.wear.utils.sensors.CompassSensor


private const val buttonSweepAngle = 60f

/**
 * Composable function that represents the Track screen.
 * This screen displays a compass arrow indicating the current direction, the progress bar of the track,
 * and a button to trigger an SOS signal.
 * @param navController The navigation controller to handle navigation actions.
 * @param trackID A string parameter that can be used to display additional information on the screen.
 * @param modifier A [Modifier] to be applied to the screen layout.
 */
@Composable
fun TrackScreen(navController: NavHostController, trackID: String, modifier: Modifier = Modifier) {
    // Ottiene il contesto corrente per accedere ai sensori del dispositivo
    val context = LocalContext.current

    // Inizializza il sensore della bussola e lo memorizza tra le composizioni
    val compassSensor = remember { CompassSensor(context) }

    val gpxViewModel = remember { TrackScreenViewModel() }

    // Raccoglie il valore corrente della direzione come stato osservabile
    val direction by compassSensor.direction.collectAsState()

    val accuracy by compassSensor.accuracy.collectAsState()

    val gpx by gpxViewModel.gpxData.collectAsState()


    // Gestisce il ciclo di vita del sensore: avvio all'ingresso nella composizione e arresto all'uscita
    DisposableEffect(compassSensor) {
        // Avvia la lettura dei dati dai sensori
        compassSensor.start()
        gpxViewModel.loadGpx(context, "$trackID.gpx")

        // Pulisce le risorse quando il componente viene rimosso dalla composizione
        onDispose {
            compassSensor.stop()
        }
    }

    val progress = 0.75f

    var alone = false
    val buttonWidth = if (alone) 0f else buttonSweepAngle
    var info: String? = null
    var infobackgroundColor: androidx.compose.ui.graphics.Color =
        MaterialTheme.colorScheme.primaryContainer
    var infotextColor: androidx.compose.ui.graphics.Color =
        MaterialTheme.colorScheme.onPrimaryContainer

    AnimatedVisibility(
        visible = gpx == null,
        enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(),
        exit = fadeOut(animationSpec = tween(1000)) + slideOutVertically()
    ) {
        Loading(Modifier.fillMaxSize())
    }
    AnimatedVisibility(
        visible = accuracy < 2 && gpx != null,
        enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(),
        exit = fadeOut(animationSpec = tween(1000)) + slideOutVertically()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier.fillMaxSize()
        ) {
            CompassCalibrationNotice(modifier)
        }
    }
    AnimatedVisibility(
        visible = accuracy >= 2 && gpx != null,
        enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(),
        exit = fadeOut(animationSpec = tween(1000)) + slideOutVertically()
    ) {
        ScreenScaffold(
        timeText = {
            TimeText(
                backgroundColor = infobackgroundColor,
                modifier = Modifier.padding(10.dp)
            ) { time ->
                curvedText(
                    text = if (info.isNullOrBlank()) time else info,
                    overflow = TextOverflow.Ellipsis,
                    color = infotextColor,
                )
            }
        },
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier.fillMaxSize()
        ) {

            CircularProgressIndicator(
                progress = { progress },
                startAngle = 90f + buttonWidth / 2,
                endAngle = 90f - buttonWidth / 2,
            )


            Arrow(
                direction = direction,  // Angolo di rotazione basato sui dati del sensore
//                    color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(50.dp),  // Padding per evitare che la freccia tocchi i bordi dello schermo
            )

            if (!alone) {
                SosButton(
                    sweepAngle = buttonSweepAngle,
                    onSosTriggered = {
                        navController.navigate(route = Screen.SOSScreen.route)
                    }
                )
            }
        }
    }
}
}

@Composable
fun CompassCalibrationNotice(
    modifier: Modifier = Modifier,
) {
    val message = "Low accuracy"
    val subMessage = "Tilt and move the device"

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(top = 15.dp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            fontSize = MaterialTheme.typography.titleMedium.fontSize
        )
        GifRenderer(Modifier.fillMaxSize(0.6f), R.drawable.compass, R.drawable.compassplaceholder)
        Text(
            text = subMessage,
            modifier = Modifier.padding(horizontal = 10.dp),
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun CalibrationPreview() {
    OnTrekTheme {
        CompassCalibrationNotice(
            modifier = Modifier.fillMaxSize()
        )
    }
}