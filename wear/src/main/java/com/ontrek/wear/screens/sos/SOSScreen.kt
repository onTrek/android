package com.ontrek.wear.screens.sos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.outlined.Dangerous
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.OutlinedButton
import androidx.wear.compose.material.Text
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.IconButton
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.tooling.preview.devices.WearDevices
import com.ontrek.wear.theme.OnTrekTheme
import com.ontrek.wear.utils.components.CurvedText


@Composable
fun SOSScreen(navController: NavHostController) {
    var showDialog by remember { mutableStateOf(false) }

    ScreenScaffold {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Red),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            CurvedText(
                anchor = 270f,
                color = Color.White,
                text = "Help is on the way!",
                fontSize = 16,
                modifier = Modifier.padding(5.dp)
            )
            Icon(
                imageVector = Icons.Filled.MyLocation,
                contentDescription = "Location",
                tint = Color.White
            )
            IconButton(
                onClick = { showDialog = true },
                modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = Color.White
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
                tint = Color.Red
            )
        },
        title = {
            Text(
                text = "Dismiss SOS?",
                color = Color.Red,
            )
        },
        text = {
            Text(
                text = "Are you sure you want to dismiss the SOS?",
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Red,
                    contentColor = Color.White
                )
            ) { Text("Yes") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("No") }
        }
    )
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