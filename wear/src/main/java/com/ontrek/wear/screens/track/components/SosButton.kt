package com.ontrek.wear.screens.track.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.ontrek.wear.theme.OnTrekSmartwatchTheme

@Composable
fun SosButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(
            topStart = 100.dp,
            topEnd = 100.dp,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        ),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color.Red
        ),
        modifier = modifier
    ) {
        Text(
            text = "SOS",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.button
        )
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun SosButtonPreview() {
    OnTrekSmartwatchTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            SosButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                onClick = {}
            )
        }
    }
}
