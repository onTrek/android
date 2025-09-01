package com.ontrek.wear.screens.track

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.curvedText
import androidx.wear.ongoing.OngoingActivity
import com.ontrek.wear.MainActivity
import com.ontrek.wear.R
import com.ontrek.wear.screens.Screen
import com.ontrek.wear.screens.track.components.Arrow
import com.ontrek.wear.screens.track.components.CompassCalibrationNotice
import com.ontrek.wear.screens.track.components.DistantFromTrackDialog
import com.ontrek.wear.screens.track.components.EndTrack
import com.ontrek.wear.screens.track.components.FollowButton
import com.ontrek.wear.screens.track.components.FriendRadar
import com.ontrek.wear.screens.track.components.OffTrackDialog
import com.ontrek.wear.screens.track.components.SnoozeDialog
import com.ontrek.wear.screens.track.components.SosButton
import com.ontrek.wear.screens.track.components.SosFriendDialog
import com.ontrek.wear.screens.track.components.StopSosDialog
import com.ontrek.wear.utils.components.ErrorScreen
import com.ontrek.wear.utils.components.Loading
import com.ontrek.wear.utils.functions.calculateFontSize
import com.ontrek.wear.utils.functions.getContrastingTextColor
import com.ontrek.wear.utils.functions.getReadableDistance
import com.ontrek.wear.utils.sensors.CompassSensor
import com.ontrek.wear.utils.sensors.GpsSensor


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
    currentUserId: String,
    modifier: Modifier = Modifier
) {
    // Ottiene il contesto corrente per accedere ai sensori del dispositivo
    val context = LocalContext.current
    val applicationContext = context.applicationContext
    val activity = LocalActivity.current as MainActivity
    val isAmbientMode by activity.isInAmbientMode.collectAsStateWithLifecycle()

    // Inizializza il sensore della bussola e lo memorizza tra le composizioni
    val compassSensor = remember { CompassSensor(context) }
    // Inizializza il sensore GPS
    val gpsSensor = remember { GpsSensor(context) }
    // Contiene il file GPX caricato

    val gpxViewModel = remember { TrackScreenViewModel(currentUserId) }

    // Raccoglie l'accuratezza del sensore GPS come stato osservabile
    val gpsAccuracy by gpsSensor.accuracy.collectAsStateWithLifecycle()
    val isGpsAccuracyLow = {
        gpsAccuracy > trackPointThreshold
    }
    val gpsAccuracyText = "Low GPS signal"
    val vibrator = getSystemService(context, Vibrator::class.java)

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
    // Raccoglie l'informazione se siamo stati vicini al tracciato come stato osservabile
    val hasBeenNearTheTrack by gpxViewModel.hasBeenNearTheTrack.collectAsStateWithLifecycle()
    // Raccoglie se stiamo all'inizio o alla fine del tracciato come stato osservabile
    val isOffTrack by gpxViewModel.isOffTrack.collectAsStateWithLifecycle()
    // Raccoglie l'angolo della freccia come stato osservabile
    val arrowDirection by gpxViewModel.arrowDirectionState.collectAsStateWithLifecycle()
    // Raccoglie la posizione corrente come stato osservabile
    val currentLocation by gpsSensor.location.collectAsStateWithLifecycle()
    // Raccoglie il progresso lungo il tracciato come stato osservabile
    val progress by gpxViewModel.progressState.collectAsStateWithLifecycle()
    // Raccoglie la distanza dal tracciato come stato osservabile
    val distanceFromTrack by gpxViewModel.distanceFromTrack.collectAsStateWithLifecycle()
    // Raccoglie la distanza in linea d'aria
    val distanceAirLine by gpxViewModel.distanceAirLine.collectAsStateWithLifecycle()
    // Raccoglie la distanza ancora da percorrere
    val remainingDistance by gpxViewModel.remainingDistance.collectAsStateWithLifecycle()
    // Raccoglie la distanza minima per la notifica come stato osservabile
    val notifyOffTrackModalOpen by gpxViewModel.notifyOffTrack.collectAsStateWithLifecycle()
    // Raccoglie i membri della sessione come stato osservabile
    val membersLocation by gpxViewModel.membersLocation.collectAsStateWithLifecycle()
    // Raccoglie la lista delle richieste di aiuto come stato osservabile
    val listHelpRequest by gpxViewModel.listHelpRequestState.collectAsStateWithLifecycle()
    val followingUser by gpxViewModel.followingUser.collectAsStateWithLifecycle()
    val notifyOnTrackAgain by gpxViewModel.notifyOnTrackAgain.collectAsStateWithLifecycle()
    val showStopDialog by gpxViewModel.showStopDialog.collectAsStateWithLifecycle()

    val alone = sessionID.isEmpty() //if session ID is empty, we are alone in the track
    var isSosButtonPressed by remember { mutableStateOf(false) }
    var showEndTrackDialog by remember { mutableStateOf(false) }
    var trackCompleted by remember { mutableStateOf(false) }
    var snoozeModalOpen by remember { mutableStateOf(false) }
    var distantAtStartupModalOpen by remember { mutableStateOf(false) }
    var oldDirection by remember { mutableStateOf<Float?>(null) }
    var followingCompleted by remember { mutableStateOf(false) }
    var isSosNotTriggered by remember { mutableStateOf(true) }

    val showDialogForMember = remember { mutableStateMapOf<String, Boolean>() }


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

    LaunchedEffect(Unit) {
        while (true) {
            val threadSafeCurrentLocation = currentLocation
            if (threadSafeCurrentLocation == null) {
                Log.d("GPS_LOCATION", "Location not available")
                kotlinx.coroutines.delay(500)
                continue
            }
            if (!alone) {
                gpxViewModel.sendCurrentLocation(threadSafeCurrentLocation, sessionID)
                gpxViewModel.getMembersLocation(sessionID)
            }
            kotlinx.coroutines.delay(3000)
        }
    }

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

    DisposableEffect(isAmbientMode) {
        val layoutParams = activity.window.attributes

        if (isAmbientMode) {
            Log.d("AMBIENT_MODE", "Entering ambient mode")
            layoutParams.screenBrightness = 0.0f
            if (accuracy == 3) {
                Log.d("AMBIENT_MODE", "Stopping compass sensor")
                compassSensor.stop()
            }
        } else {
            Log.d("AMBIENT_MODE", "Exiting ambient mode")
            layoutParams.screenBrightness = 1.0f // Set brightness to maximum (1.0f)
            if (accuracy == 3) {
                compassSensor.start()
            }
        }

        activity.window.attributes = layoutParams

        onDispose {
            layoutParams.screenBrightness = -1.0f //system default brightness
            activity.window.attributes = layoutParams
        }
    }

    LaunchedEffect(progress) {
        if (progress == 1f) {
            if (!trackCompleted || followingUser != null) {
                Log.d("GPS_TRACK", "Track complete")
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(100, 100, 500),
                        intArrayOf(100, 0, 100),
                        -1 // -1 means no repeat
                    )
                )
            }

            if (followingUser == null) {
                showEndTrackDialog = true
                trackCompleted = true
            } else {
                gpxViewModel.stopFollowing(true)
                followingCompleted = true
            }
        }
    }

    LaunchedEffect(accuracy) {
        if (accuracy == 3 && vibrationNeeded) {
            vibrator?.vibrate(
                VibrationEffect.createOneShot(
                    300,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
            compassSensor.setVibrationNeeded(false)
        } else if (accuracy < 3) {
            compassSensor.setVibrationNeeded(true)
        }
    }

    LaunchedEffect(notifyOffTrackModalOpen) {
        if (notifyOffTrackModalOpen) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(300),
                        intArrayOf(255),
                        0
                    )
                )

        } else {
            vibrator?.cancel()
        }
    }

    LaunchedEffect(direction) {
        if (accuracy < 3) return@LaunchedEffect
        if (oldDirection != direction) {
            oldDirection = direction
        }
        gpxViewModel.elaborateDirection(direction)
    }

    LaunchedEffect(currentLocation) {
        val threadSafeCurrentLocation = currentLocation

        if (threadSafeCurrentLocation == null) {
            Log.d("GPS_LOCATION", "Location not available")
            return@LaunchedEffect
        }

        if (hasBeenNearTheTrack == null) {
            // Startup function
            gpxViewModel.checkTrackDistanceAndInitialize(threadSafeCurrentLocation, direction)
        } else {
            // If we are near the track, we can proceed to elaborate the position
            gpxViewModel.elaboratePosition(threadSafeCurrentLocation)
        }
    }

    LaunchedEffect(hasBeenNearTheTrack) {
        val threadSafeDistanceAirLine = distanceAirLine
        if (hasBeenNearTheTrack == false && threadSafeDistanceAirLine != null && threadSafeDistanceAirLine > 100) {
            distantAtStartupModalOpen = true
            vibrator?.vibrate(
                VibrationEffect.createOneShot(
                    300,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        }
    }

    LaunchedEffect(listHelpRequest) {
        var shouldVibrate = false
        showDialogForMember.keys.forEach { key ->
            if (listHelpRequest.none { it.user.id == key }) {
                showDialogForMember.remove(key)
            }
        }
        listHelpRequest.forEach { member ->
            val key = member.user.id
            if (showDialogForMember[key] == null) shouldVibrate = true
            showDialogForMember.getOrPut(key) { true }
        }
        if (shouldVibrate && listHelpRequest.isNotEmpty()) {
            vibrator?.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(100, 100, 100, 100, 100, 100, 500),
                    intArrayOf(255, 0, 255, 0, 255, 0, 255),
                    -1 // -1 means no repeat
                )
            )
        }
    }

    LaunchedEffect(notifyOnTrackAgain) {
        if (notifyOnTrackAgain) {
            vibrator?.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(100, 300),
                    intArrayOf(100, 100),
                    -1 // -1 means no repeat
                )
            )
            gpxViewModel.cancelOnTrackAgainNotification()
        }
    }

    LaunchedEffect(followingUser) {
        val threadSafeLocation = currentLocation
        if (followingUser == null && threadSafeLocation != null) {
            gpxViewModel.resumeTrack(threadSafeLocation)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            vibrator?.cancel()
            if (isSosNotTriggered) gpxViewModel.deleteLocation(sessionID)
        }
    }


    val threadSafeFollowingUser = followingUser
    val buttonWidth = if (alone) 0f else buttonSweepAngle
    val infoBackgroundColor: Color = when {
        threadSafeFollowingUser != null -> threadSafeFollowingUser.color
        isGpsAccuracyLow() || isOffTrack || hasBeenNearTheTrack == false -> MaterialTheme.colorScheme.errorContainer
        notifyOnTrackAgain || trackCompleted -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainer
    }
    val infoTextColor: Color = when {
        threadSafeFollowingUser != null -> getContrastingTextColor(threadSafeFollowingUser.color)
        isGpsAccuracyLow() || isOffTrack || hasBeenNearTheTrack == false -> MaterialTheme.colorScheme.onErrorContainer
        notifyOnTrackAgain || trackCompleted -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
}

    if (parsingError.isNotEmpty()) {
        ErrorScreen(
            "Error while parsing the GPX file: $parsingError",
            Modifier.fillMaxSize(),
            null,
            null
        )
    } else if (trackPoints.isEmpty() || hasBeenNearTheTrack == null) {
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
                            backgroundColor = infoBackgroundColor,
                            modifier = Modifier.padding(10.dp)
                        ) { time ->
                            val displayText = when {
                                threadSafeFollowingUser != null -> "${getReadableDistance(remainingDistance.toDouble())} away"
                                isOffTrack || !hasBeenNearTheTrack!! -> "${getReadableDistance(distanceAirLine ?: 0.0)} away"
                                notifyOnTrackAgain -> "OnTrek!"
                                trackCompleted -> "Track Complete"
                                isGpsAccuracyLow() -> gpsAccuracyText
                                else -> time
                            }
                            val dynamicFontSize = calculateFontSize(displayText)
                            curvedText(
                                text = displayText,
                                overflow = TextOverflow.Ellipsis,
                                color = infoTextColor,
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
                    if (hasBeenNearTheTrack == true && !isOffTrack)
                        CircularProgressIndicator(
                            progress = { progress },
                            startAngle = 90f + buttonWidth / 2,
                            endAngle = 90f - buttonWidth / 2,
                        )

                    if (!alone) {
                        currentLocation?.let { userLocation ->
                            FriendRadar(
                                newDirection = direction,
                                oldDirection = if (oldDirection != null) oldDirection!! else 0.0f,
                                userLocation = userLocation,
                                members = membersLocation.filter { it.user.id != currentUserId }
                                    .filter { it.accuracy != -1.0 },
                                modifier = Modifier.fillMaxSize(),
                                onUserClick = { memberId ->
                                    showDialogForMember[memberId] = true
                                }
                            )
                        }
                    }

                    Arrow(
                        direction = arrowDirection,
                        distanceFromTrack = distanceFromTrack ?: 0.0,
                        isOffTrack = isOffTrack || hasBeenNearTheTrack == false,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(50.dp),  // Padding per evitare che la freccia tocchi i bordi dello schermo
                    )

                    if (!alone) {
                        if (threadSafeFollowingUser != null) {
                            FollowButton(
                                username = threadSafeFollowingUser.username,
                                userColor = threadSafeFollowingUser.color,
                                sweepAngle = buttonSweepAngle,
                                stopFollow = { gpxViewModel.stopFollowing()}
                            )
                        } else {
                            SosButton(
                                sweepAngle = buttonSweepAngle,
                                onSosTriggered = {
                                    Log.d("SOS_BUTTON", "SOS button pressed")
                                    isSosNotTriggered = false
                                    val threadSafeCurrentLocation = currentLocation
                                    if (threadSafeCurrentLocation != null) {
                                        gpxViewModel.sendCurrentLocation(
                                            threadSafeCurrentLocation,
                                            sessionID,
                                            true
                                        )
                                    }
                                    navController.navigate(route = Screen.SOSScreen.route + "?sessionID=$sessionID&currentUserId=$currentUserId")
                                },
                                onPressStateChanged = { pressed: Boolean ->
                                    isSosButtonPressed = pressed
                                },
                            )
                        }
                    }
                }
                OffTrackDialog(
                    showDialog = notifyOffTrackModalOpen,
                    onConfirm = {
                        gpxViewModel.snoozeOffTrackNotification()
                        Toast.makeText(context, "Snoozed for 1m", Toast.LENGTH_SHORT).show()
                    },
                    onSnooze = {
                        snoozeModalOpen = true
                    }
                )
                SnoozeDialog(
                    showDialog = snoozeModalOpen,
                    onDismiss = {
                        snoozeModalOpen = false
                    },
                    onSelectTime = { time ->
                        gpxViewModel.snoozeOffTrackNotification(time)
                        snoozeModalOpen = false
                    }
                )

                listHelpRequest.forEach { member ->
                    SosFriendDialog(
                        showDialog = (showDialogForMember[member.user.id] == true) && !alone,
                        onDismiss = {
                            showDialogForMember[member.user.id] = false
                        },
                        onConfirm = {
                            showDialogForMember[member.user.id] = false
                            gpxViewModel.sendCurrentLocation(
                                currentLocation = currentLocation!!,
                                sessionId = sessionID,
                                goingTo = member
                            )
                        },
                        member = member,
                    )
                }
            }
            StopSosDialog(
                username = showStopDialog,
                end = followingCompleted,
                visible = showStopDialog.isNotEmpty(),
                onDismiss = {
                    gpxViewModel.setShowStopDialog()
                    followingCompleted = false
                },
            )
            DistantFromTrackDialog(
                showDialog = distantAtStartupModalOpen,
                onConfirm = {
                    distantAtStartupModalOpen = false
                },
                onClose = {
                    navController.navigate(Screen.MainScreen.route) {
                        popUpTo(Screen.TrackScreen.route) { inclusive = true }
                    }
                },
                metersAway = distanceAirLine?.toInt() ?: 0
            )
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