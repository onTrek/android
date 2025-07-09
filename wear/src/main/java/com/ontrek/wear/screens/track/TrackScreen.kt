package com.ontrek.wear.screens.track

import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
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
import com.ontrek.wear.utils.components.ErrorScreen
import com.ontrek.wear.utils.components.Loading
import com.ontrek.wear.utils.components.WarningScreen
import com.ontrek.wear.utils.media.GifRenderer
import com.ontrek.wear.utils.sensors.CompassSensor
import com.ontrek.wear.utils.sensors.GpsSensor


private const val buttonSweepAngle = 60f

/**
 * Composable function that represents the Track screen.
 * This screen displays a compass arrow indicating the current direction, the progress bar of the track,
 * and a button to trigger an SOS signal.
 * @param navController The navigation controller to handle navigation actions.
 * @param trackID A string parameter that can be used to display additional information on the screen.
 * @param sessionID A string parameter representing the session ID, which can be used to fetch friends data or other session-related information.
 * @param modifier A [Modifier] to be applied to the screen layout.
 */
@Composable
fun TrackScreen(navController: NavHostController, trackID: String, sessionID: String, modifier: Modifier = Modifier) {
    // Ottiene il contesto corrente per accedere ai sensori del dispositivo
    val context = LocalContext.current

    // Inizializza il sensore della bussola e lo memorizza tra le composizioni
    val compassSensor = remember { CompassSensor(context) }
    // Inizializza il sensore GPS
    val gpsSensor = remember { GpsSensor(context) }
    // Contiene il file GPX caricato
    val gpxViewModel = remember { TrackScreenViewModel() }

    // Raccoglie il valore corrente della direzione come stato osservabile
    val direction by compassSensor.direction.collectAsState()
    // Raccoglie l'accuratezza del sensore della bussola come stato osservabile
    val accuracy by compassSensor.accuracy.collectAsState()

    // Raccoglie la lista dei punti del tracciato dal ViewModel
    val trackPoints by gpxViewModel.trackPointListState.collectAsState()
    // Raccoglie la lunghezza totale del tracciato come stato osservabile
    //val totalLength by gpxViewModel.totalLengthState.collectAsState()
    // Raccoglie eventuali errori di parsing del file GPX come stato osservabile
    val parsingError by gpxViewModel.parsingErrorState.collectAsState()
    // Raccoglie lo stato di vicinanza al tracciato come stato osservabile
    val isNearTrack by gpxViewModel.isNearTrackState.collectAsState()
    // Raccoglie l'indice del punto più vicino al tracciato come stato osservabile
    //val nearestTrackPoint by gpxViewModel.nearestTrackPointState.collectAsState()
    // Raccoglie l'angolo della freccia come stato osservabile
    val arrowDirection by gpxViewModel.arrowDirectionState.collectAsState()
    // Raccoglie la posizione corrente come stato osservabile
    val currentLocation by gpsSensor.location.collectAsState()
    // Raccoglie se l'utente è sul tracciato come stato osservabile
    val onTrak by gpxViewModel.onTrakState.collectAsState()
    // Raccoglie il progresso lungo il tracciato come stato osservabile
    val progress by gpxViewModel.progressState.collectAsState()


    // Gestisce il ciclo di vita del sensore: avvio all'ingresso nella composizione e arresto all'uscita
    DisposableEffect(compassSensor, gpsSensor) {
        // Avvia la lettura dei dati dai sensori
        compassSensor.start()
        gpsSensor.start()
        gpxViewModel.loadGpx(context, "$trackID.gpx")

        // Pulisce le risorse quando il componente viene rimosso dalla composizione
        onDispose {
            compassSensor.stop()
            gpsSensor.stop()
            gpxViewModel.reset()
        }
    }

    LaunchedEffect(onTrak) {
        Log.d("GPS_TRACK", "OnTrak state changed: $onTrak")
    }

    LaunchedEffect(direction) {
        if (accuracy < 2) return@LaunchedEffect
        gpxViewModel.elaborateDirection(direction)
    }

    LaunchedEffect(currentLocation) {
        val threadSafeCurrentLocation = currentLocation

        if (threadSafeCurrentLocation == null) {
            Log.d("GPS_LOCATION", "Location not available")
            return@LaunchedEffect
        }

        Log.d("GPS_LOCATION_POSITION", "Current Location: ${threadSafeCurrentLocation.latitude}, ${threadSafeCurrentLocation.longitude}; accuracy: ${threadSafeCurrentLocation.accuracy}")
        if (threadSafeCurrentLocation.hasAltitude()) {
            Log.d("GPS_LOCATION_ALTITUDE", "Current Altitude: ${threadSafeCurrentLocation.altitude}")
        } else {
            Log.d("GPS_LOCATION_ALTITUDE", "Current Altitude: Not available")
        }

        if (isNearTrack == null || isNearTrack == false) {
            // Startup function
            gpxViewModel.checkTrackDistanceAndInitialize(threadSafeCurrentLocation, direction)
        } else if (isNearTrack == true) {
            // If we are near the track, we can proceed to elaborate the position
            gpxViewModel.elaboratePosition(threadSafeCurrentLocation)
        }
    }

    val alone = sessionID.isEmpty() //if session ID is empty, we are alone in the track
    val buttonWidth = if (alone) 0f else buttonSweepAngle
    var info: String? = null
    var infobackgroundColor: androidx.compose.ui.graphics.Color =
        MaterialTheme.colorScheme.primaryContainer
    var infotextColor: androidx.compose.ui.graphics.Color =
        MaterialTheme.colorScheme.onPrimaryContainer

    if (!parsingError.isEmpty()) {
        ErrorScreen("Error while parsing the GPX file: $parsingError", Modifier.fillMaxSize(),null, null)
    } else if (isNearTrack != null && isNearTrack != true) {
        WarningScreen("You are too distant from the selected track", Modifier.fillMaxSize(),null, null)
    } else if (trackPoints.isEmpty() || isNearTrack == null) {
        Loading(Modifier.fillMaxSize())
    } else {
        AnimatedVisibility(
            visible = accuracy < 2,
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
            visible = accuracy >= 2,
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
                        direction = arrowDirection,
//                      color = MaterialTheme.colorScheme.primaryContainer,
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