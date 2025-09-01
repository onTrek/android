package com.ontrek.wear.screens.sos

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.outlined.Dangerous
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.FilledTonalIconButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButtonColors
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.curvedText
import androidx.wear.compose.material3.touchTargetAwareSize
import com.ontrek.wear.screens.track.TrackScreenViewModel
import com.ontrek.wear.screens.track.components.FriendRadar
import com.ontrek.wear.utils.sensors.CompassSensor
import com.ontrek.wear.utils.sensors.GpsSensor


@Composable
fun SOSScreen(
    navController: NavHostController,
    sessionID: String,
    currentUserId: String
) {
    val context = LocalContext.current
    val sosViewModel = remember { TrackScreenViewModel(currentUserId) }

    var showDialog by remember { mutableStateOf(false) }

    val textColor = MaterialTheme.colorScheme.onErrorContainer

    val gpsSensor = remember { GpsSensor(context) }

    val compassSensor = remember { CompassSensor(context) }

    val isInitialized by sosViewModel.hasBeenNearTheTrack.collectAsStateWithLifecycle()

    val accuracy by compassSensor.accuracy.collectAsStateWithLifecycle()

    val direction by compassSensor.direction.collectAsStateWithLifecycle()

    val currentLocation by gpsSensor.location.collectAsStateWithLifecycle()

    val membersLocation by sosViewModel.membersLocation.collectAsStateWithLifecycle()

    var oldDirection by remember { mutableStateOf<Float?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            val threadSafeCurrentLocation = currentLocation
            if (threadSafeCurrentLocation == null) {
                Log.d("GPS_LOCATION", "Location not available")
                kotlinx.coroutines.delay(500)
                continue
            }
            sosViewModel.sendCurrentLocation(threadSafeCurrentLocation, sessionID, true)
            sosViewModel.getMembersLocation(sessionID)

            kotlinx.coroutines.delay(3000)
        }
    }

    DisposableEffect(compassSensor, gpsSensor) {
        // Avvia la lettura dei dati dai sensori
        compassSensor.start()
        gpsSensor.start()

        // Pulisce le risorse quando il componente viene rimosso dalla composizione
        onDispose {
            compassSensor.stop()
            gpsSensor.stop()
        }
    }

    LaunchedEffect(direction) {
        if (accuracy < 3) return@LaunchedEffect
        sosViewModel.elaborateDirection(direction)
        if (oldDirection != direction) {
            oldDirection = direction
        }
    }

    LaunchedEffect(currentLocation) {
        val threadSafeCurrentLocation = currentLocation
        if (threadSafeCurrentLocation == null) {
            Log.d("GPS_LOCATION", "Location not available")
            return@LaunchedEffect
        }
        if (isInitialized == null) {
            sosViewModel.checkTrackDistanceAndInitialize(threadSafeCurrentLocation, direction)
        } else {
            sosViewModel.elaboratePosition(threadSafeCurrentLocation)
        }
    }

    ScreenScaffold(
        modifier = Modifier.background(MaterialTheme.colorScheme.errorContainer),
        timeText = {
            TimeText(
                backgroundColor = MaterialTheme.colorScheme.onError.copy(alpha = 0.4f)
            ) { time ->
                curvedText(
                    text = if (membersLocation.any { it.going_to == currentUserId }) "Help coming!" else "SOS sent!",
//                    CurvedModifier.weight(1f),
                    overflow = TextOverflow.Ellipsis,
                    color = textColor,
                )
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            currentLocation?.let { userLocation ->
                FriendRadar(
                    newDirection = direction,
                    oldDirection = if (oldDirection != null) oldDirection!! else 0.0f,
                    userLocation = userLocation,
                    members = membersLocation.filter {
                        Log.d("userId", "Member ID: ${it.user.id} | Current user ID: $currentUserId")
                        it.user.id != currentUserId
                    }.filter { it.accuracy != -1.0 }.filter { it.going_to == currentUserId},
                    modifier = Modifier.fillMaxSize(),
                    radarColor = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.6f)
                )
            }
            Icon(
                imageVector = Icons.Filled.MyLocation,
                contentDescription = "Location",
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            FilledTonalIconButton(
                onClick = { showDialog = true },
                colors = IconButtonColors(
                    containerColor = MaterialTheme.colorScheme.onError,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                    disabledContainerColor = MaterialTheme.colorScheme.errorContainer,
                    disabledContentColor = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .touchTargetAwareSize(25.dp),
            ) {

                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.fillMaxSize(0.6f)
                )
            }
            DismissSOSDialog(
                showDialog = showDialog,
                onConfirm = { closeScreen(navController) },
                onDismiss = { showDialog = false }
            )
        }
    }
}

@Composable
fun DismissSOSDialog(
    showDialog: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        visible = showDialog,
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Outlined.Dangerous,
                contentDescription = "Dismiss SOS",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 30.dp)
            )
        },
        title = {
            Text(
                text = "Dismiss SOS?",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.titleMedium,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
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
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.padding(end = 4.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Confirm",
                )
            }
        }
    ) {}
}

fun closeScreen(navController: NavHostController) {
    // call api to remove SOS
    navController.popBackStack()
}