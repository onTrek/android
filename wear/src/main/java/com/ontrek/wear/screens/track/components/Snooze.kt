package com.ontrek.wear.screens.track.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Dialog
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

@Composable
fun SnoozeDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onSelectTime: (Int) -> Unit
) {
    Dialog(
        visible = showDialog,
        onDismissRequest = onDismiss,
    ) {
        val scrollState = rememberScalingLazyListState()

        ScalingLazyColumn(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            state = scrollState
        ) {
            item {
                Text(
                    text = "Snooze for:",
                    modifier = Modifier.padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

            item {
                TimeButton(time = 5, onSelectTime = onSelectTime)
            }

            item {
                TimeButton(time = 10, onSelectTime = onSelectTime)
            }

            item {
                TimeButton(time = 30, onSelectTime = onSelectTime)
            }
        }
    }
}

@Composable
private fun TimeButton(
    time: Int,
    onSelectTime: (Int) -> Unit
) {
    Button(
        onClick = { onSelectTime(time) },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = "${time} minutes",
            style = MaterialTheme.typography.labelMedium
        )
    }
}
