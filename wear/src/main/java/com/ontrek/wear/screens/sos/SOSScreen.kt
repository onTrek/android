package com.ontrek.wear.screens.sos

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.FilledTonalIconButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.IconButtonColors
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.curvedText
import androidx.wear.compose.material3.touchTargetAwareSize
import androidx.wear.tooling.preview.devices.WearDevices
import com.ontrek.wear.theme.OnTrekTheme


@Composable
fun SOSScreen(navController: NavHostController) {
    var showDialog by remember { mutableStateOf(false) }
    val textColor = MaterialTheme.colorScheme.onErrorContainer

    ScreenScaffold(
        modifier = Modifier.background(MaterialTheme.colorScheme.errorContainer),
        timeText = {
            TimeText(
                backgroundColor = MaterialTheme.colorScheme.onError.copy(alpha = 0.8f)
            ) { time ->
                curvedText(
                    text = "Help coming!",  // TODO: SOS send (if no one responded yet)
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
            Icon(
                imageVector = Icons.Filled.MyLocation,
                contentDescription = "Location",
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            FilledTonalIconButton (
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

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    OnTrekTheme {
        SOSScreen(rememberNavController())
    }
}