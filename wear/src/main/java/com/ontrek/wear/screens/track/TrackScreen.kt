package com.ontrek.wear.screens.track

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.curvedText
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.tooling.preview.devices.WearDevices
import com.ontrek.wear.R
import com.ontrek.wear.screens.Screen
import com.ontrek.wear.screens.track.components.Arrow
import com.ontrek.wear.screens.track.components.EndTrack
import com.ontrek.wear.screens.track.components.SosButton
import com.ontrek.wear.theme.OnTrekTheme
import com.ontrek.wear.utils.components.ErrorScreen
import com.ontrek.wear.utils.components.Loading
import com.ontrek.wear.utils.components.WarningScreen
import com.ontrek.wear.utils.functions.calculateFontSize
import com.ontrek.wear.utils.media.GifRenderer
import com.ontrek.wear.utils.sensors.CompassSensor
import com.ontrek.wear.utils.sensors.GpsSensor
import kotlin.apply


private const val buttonSweepAngle = 60f

private const val NOTIFICATION_CHANNEL_ID = "track_navigation_channel"
private const val NOTIFICATION_ID = 1001

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
fun TrackScreen(
    navController: NavHostController,
    trackID: String,
    trackName: String,
    sessionID: String,
    modifier: Modifier = Modifier
) {
    // Ottiene il contesto corrente per accedere ai sensori del dispositivo
    val context = LocalContext.current
    val applicationContext = context.applicationContext

    // Inizializza il sensore della bussola e lo memorizza tra le composizioni
    val compassSensor = remember { CompassSensor(context) }
    // Inizializza il sensore GPS
    val gpsSensor = remember { GpsSensor(context) }
    // Contiene il file GPX caricato
    val gpxViewModel = remember { TrackScreenViewModel() }

    // Raccoglie l'accuratezza del sensore GPS come stato osservabile
    val gpsAccuracy by gpsSensor.accuracy.collectAsStateWithLifecycle()
    val isGpsAccuracyLow = {
        gpsAccuracy > trackPointThreshold
    }
    val gpsAccuracyText = "Low GPS signal"
    val vibrator = getSystemService(context, android.os.Vibrator::class.java)

    // Raccoglie il valore corrente della direzione come stato osservabile
    val direction by compassSensor.direction.collectAsStateWithLifecycle()
    // Raccoglie l'accuratezza del sensore della bussola come stato osservabile
    val accuracy by compassSensor.accuracy.collectAsStateWithLifecycle()
    // Raccoglie la necessità di vibrare quando l'accuratezza torna alta
    val vibrationNeeded by compassSensor.vibrationNeeded.collectAsStateWithLifecycle()

    // Raccoglie la lista dei punti del tracciato dal ViewModel
    val trackPoints by gpxViewModel.trackPointListState.collectAsStateWithLifecycle()
    // Raccoglie la lunghezza totale del tracciato come stato osservabile
    //val totalLength by gpxViewModel.totalLengthState.collectAsStateWithLifecycle()
    // Raccoglie eventuali errori di parsing del file GPX come stato osservabile
    val parsingError by gpxViewModel.parsingErrorState.collectAsStateWithLifecycle()
    // Raccoglie lo stato di vicinanza al tracciato come stato osservabile
    val isNearTrack by gpxViewModel.isNearTrackState.collectAsStateWithLifecycle()
    // Raccoglie l'indice del punto più vicino al tracciato come stato osservabile
    //val nearestTrackPoint by gpxViewModel.nearestTrackPointState.collectAsStateWithLifecycle()
    // Raccoglie l'angolo della freccia come stato osservabile
    val arrowDirection by gpxViewModel.arrowDirectionState.collectAsStateWithLifecycle()
    // Raccoglie la posizione corrente come stato osservabile
    val currentLocation by gpsSensor.location.collectAsStateWithLifecycle()
    // Raccoglie se l'utente è sul tracciato come stato osservabile
    val onTrak by gpxViewModel.onTrackState.collectAsStateWithLifecycle()
    // Raccoglie il progresso lungo il tracciato come stato osservabile
    val progress by gpxViewModel.progressState.collectAsStateWithLifecycle()
    // Raccoglie la distanza dal tracciato come stato osservabile
    val distanceFromTrack by gpxViewModel.distanceFromTrack.collectAsStateWithLifecycle()
    // Raccoglie la distanza minima per la notifica come stato osservabile
    val distanceNotification by gpxViewModel.notifyOffTrack.collectAsStateWithLifecycle()

    var isSosButtonPressed by remember { mutableStateOf(false) }

    var showEndTrackDialog by remember { mutableStateOf(false) }
    var trackCompleted by remember { mutableStateOf(false) }


    // Create PendingIntent to return to the app
    val pendingIntent = remember {
        val intent = Intent(applicationContext, context.javaClass).apply {
            action = Intent.ACTION_MAIN
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // Create notification builder
    val notificationBuilder = remember {
        NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.hiking)
            .setContentTitle("OnTrek")
            .setContentText(trackName)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_NAVIGATION)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
    }

    val ongoingActivity = remember {

        OngoingActivity.Builder(applicationContext, NOTIFICATION_ID, notificationBuilder)
            .setStaticIcon(R.drawable.hiking)
            .build()
    }

    // Get the NotificationManager
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val channel = NotificationChannel(
        NOTIFICATION_CHANNEL_ID,
        "Track Navigation",
        NotificationManager.IMPORTANCE_HIGH
    ).apply {
        description = "Shows ongoing track navigation information"
        setShowBadge(true)
    }
    notificationManager.createNotificationChannel(channel)

    DisposableEffect(Unit) {
        Log.d("NOTIFICATION_BUILDER", "Creating notification for ongoing track navigation")

        // Apply the ongoing activity to the notification
        ongoingActivity.apply(applicationContext)
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())

        onDispose {
            Log.d("NOTIFICATION_BUILDER", "Destroying notification for ongoing track navigation")
            // Cancel the notification to stop the ongoing activity
            notificationManager.cancel(NOTIFICATION_ID)
        }
    }

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

    LaunchedEffect(progress) {
        if (progress == 1f && !trackCompleted) {
            Log.d("GPS_TRACK", "Track completed")
            showEndTrackDialog = true
            trackCompleted = true
        }
    }

    LaunchedEffect(onTrak) {
        Log.d("GPS_TRACK", "OnTrak state changed: $onTrak")
    }

    LaunchedEffect(accuracy) {
        if (accuracy == 3 && vibrationNeeded) {
            vibrator?.vibrate(
                android.os.VibrationEffect.createOneShot(
                    300,
                    android.os.VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
            compassSensor.setVibrationNeeded(false)
        } else if (accuracy < 3) {
            compassSensor.setVibrationNeeded(true)
        }
    }

    LaunchedEffect(distanceNotification) {
        val longArray = longArrayOf(300, 300, 300, 300, 300)
        val vibrationPattern = intArrayOf(255, 0, 255, 0, 255)
        if (distanceNotification) {
            vibrator?.vibrate(
                android.os.VibrationEffect.createWaveform(
                    longArray,
                    vibrationPattern,
                    -1
                )
            )
        }
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
    val isOffTrack = distanceFromTrack?.let { it > notificationTrackDistanceThreshold } == true
    val infobackgroundColor: androidx.compose.ui.graphics.Color =
        if (isGpsAccuracyLow() || isOffTrack) MaterialTheme.colorScheme.errorContainer else if (progress == 1f) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
    val infotextColor: androidx.compose.ui.graphics.Color =
        if (isGpsAccuracyLow() || isOffTrack) MaterialTheme.colorScheme.onErrorContainer else if (progress == 1f) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

    if (!parsingError.isEmpty()) {
        ErrorScreen(
            "Error while parsing the GPX file: $parsingError",
            Modifier.fillMaxSize(),
            null,
            null
        )
    } else if (isNearTrack != null && isNearTrack != true) {
        WarningScreen(
            "You are too distant from the selected track",
            Modifier.fillMaxSize(),
            null,
            null
        )
    } else if (trackPoints.isEmpty() || isNearTrack == null) {
        Loading(Modifier.fillMaxSize())
    } else {
        AnimatedVisibility(
            visible = accuracy < 3,
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
            visible = accuracy == 3,
            enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(),
            exit = fadeOut(animationSpec = tween(1000)) + slideOutVertically()
        ) {
            ScreenScaffold(
                timeText = {
                    if (!isSosButtonPressed) {
                        TimeText(
                            backgroundColor = infobackgroundColor,
                            modifier = Modifier.padding(10.dp)
                        ) { time ->
                            val displayText = when {
                                isOffTrack -> "Off track!"
                                progress == 1f -> "Track Completed"
                                isGpsAccuracyLow() -> gpsAccuracyText
                                else -> time
                            }
                            val dynamicFontSize = calculateFontSize(displayText)
                            curvedText(
                                text = displayText,
                                overflow = TextOverflow.Ellipsis,
                                color = infotextColor,
                                fontSize = dynamicFontSize
                            )
                        }
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
                        distanceFraction = distanceFromTrack?.let {
                            (it / notificationTrackDistanceThreshold).toFloat().coerceIn(0f, 1f)
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(50.dp),  // Padding per evitare che la freccia tocchi i bordi dello schermo
                    )

                    if (!alone) {
                        SosButton(
                            sweepAngle = buttonSweepAngle,
                            onSosTriggered = {
                                navController.navigate(route = Screen.SOSScreen.route)
                            },
                            onPressStateChanged = { pressed: Boolean ->
                                isSosButtonPressed = pressed
                            },
                        )
                    }
                }
                OffTrackDialog(
                    showDialog = distanceNotification,
                    onConfirm = { gpxViewModel.dismissOffTrackNotification() },
                    onSnooze = {
//                        gpxViewModel.snoozeOffTrack()
                        gpxViewModel.dismissOffTrackNotification()
                    }
                )
            }
            EndTrack(
                visible = showEndTrackDialog,
                onDismiss = { showEndTrackDialog = false },
                onConfirm = {
                    // Navigate to the end track screen with the track name
                    navController.navigate(Screen.MainScreen.route) {
                        // Clear the back stack to prevent going back to the track screen
                        popUpTo(Screen.TrackScreen.route) { inclusive = true }
                    }
                },
                trackName = trackName
            )
        }
    }
}

@Composable
fun CompassCalibrationNotice(
    modifier: Modifier = Modifier,
) {
    val message = "Low accuracy"
    val subMessage = "Tilt and move the device until it vibrates"

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(top = 10.dp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            fontSize = MaterialTheme.typography.titleMedium.fontSize
        )
        GifRenderer(Modifier.fillMaxSize(0.5f), R.drawable.compass, R.drawable.compassplaceholder)
        Text(
            text = subMessage,
            modifier = Modifier.padding(horizontal = 15.dp),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun OffTrackDialog(
    showDialog: Boolean,
    onConfirm: () -> Unit,
    onSnooze: () -> Unit
) {
    AlertDialog(
        visible = showDialog,
        onDismissRequest = onConfirm,
        icon = {
            Icon(
                imageVector = Icons.Outlined.Warning,
                contentDescription = "Off Track",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 30.dp)
            )
        },
        title = {
            Text(
                text = "You are getting off track!",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.padding(start = 4.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Confirm",
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onSnooze,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(end = 4.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Snooze,
                    contentDescription = "Snooze",
                )
            }
        }
    ) {
        item {
            Text(
                text = "Get back on track or snooze the notification.",
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
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