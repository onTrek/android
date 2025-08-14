package com.ontrek.wear.screens.track.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.ontrek.wear.R
import com.ontrek.wear.theme.OnTrekTheme
import com.ontrek.wear.utils.media.GifRenderer


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

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun CalibrationPreview() {
    OnTrekTheme {
        CompassCalibrationNotice(
            modifier = Modifier.fillMaxSize()
        )
    }
}
