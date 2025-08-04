package com.ontrek.wear.utils.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text


@Composable
fun PermissionRequester(
    context: Context,
    ambientModeEnabled: Boolean
) {
    ScreenScaffold {
        Box (
            modifier = Modifier.fillMaxSize().padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Column {
                Text(
                    text = if (ambientModeEnabled) "Location and notification permissions denied"
                        else "Always-On Screen is needed in order to use this app",
                    modifier = Modifier.fillMaxWidth(0.95f),
                    textAlign = TextAlign.Center,
                    style = androidx.wear.compose.material3.MaterialTheme.typography.titleMedium
                )
                CompactButton(
                    onClick = {
                        if (ambientModeEnabled) openAppSettings(context) else openAmbientModeSettings(context)
                    },
                    modifier = Modifier.fillMaxWidth(0.95f)
                ) {
                    Text(
                        text = if (ambientModeEnabled) "Grant Permissions" else "Open Settings",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

fun openAmbientModeSettings(context: Context) {
    val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS)
    context.startActivity(intent)
}

fun openAppSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    )
    context.startActivity(intent)
}