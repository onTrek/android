package com.ontrek.wear.screens.track

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.tooling.preview.devices.WearDevices
import com.ontrek.wear.R
import com.ontrek.wear.screens.track.components.Arrow
import com.ontrek.wear.screens.track.components.ProgressBar
import com.ontrek.wear.screens.track.components.SosButton
import com.ontrek.wear.theme.OnTrekTheme
import com.ontrek.wear.utils.media.GifRenderer
import com.ontrek.wear.utils.sensors.CompassSensor
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically

/**
 * Schermata che mostra una freccia che punta sempre verso Nord.
 * Utilizza i sensori di orientamento dello smartwatch per determinare la direzione.
 *
 * @param text Testo informativo da visualizzare nella schermata
 */
@Composable
fun TrackScreen(text: String, modifier: Modifier = Modifier) {
    // Ottiene il contesto corrente per accedere ai sensori del dispositivo
    val context = LocalContext.current

    // Inizializza il sensore della bussola e lo memorizza tra le composizioni
    val compassSensor = remember { CompassSensor(context) }

    // Raccoglie il valore corrente della direzione come stato osservabile
    val direction by compassSensor.direction.collectAsState()

    val accuracy by compassSensor.accuracy.collectAsState()

    // Gestisce il ciclo di vita del sensore: avvio all'ingresso nella composizione e arresto all'uscita
    DisposableEffect(compassSensor) {
        // Avvia la lettura dei dati dai sensori
        compassSensor.start()

        // Pulisce le risorse quando il componente viene rimosso dalla composizione
        onDispose {
            compassSensor.stop()
        }
    }

    val progress = 0.75f

    val info: String? = null

    ScreenScaffold(
        timeText = if (info.isNullOrBlank()) {
            {
                TimeText(
                    timeTextStyle = TextStyle(
                        color = MaterialTheme.colors.primary
                    ),
                    modifier = Modifier.padding(5.dp)
                )
            }
        } else null,
    ) {
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
            Box(
                contentAlignment = Alignment.Center,
                modifier = modifier.fillMaxSize()
            ) {


                if (info != null) {
                    Text(
                        info,
                        color = MaterialTheme.colors.primary,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(10.dp)
                    )
                }

                ProgressBar(
                    progress = progress
                )

                SosButton(
                    modifier = Modifier
                        .height(27.dp)
                        .fillMaxWidth(fraction = 0.6f)
                        .align(Alignment.BottomCenter),
                    onClick = {
                        Log.d("SOS", "SOS button pressed")
                    }
                )

                Arrow(
                    direction = direction,  // Angolo di rotazione basato sui dati del sensore
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(50.dp),  // Padding per evitare che la freccia tocchi i bordi dello schermo
                )
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
            color = MaterialTheme.colors.primary,
            textAlign = TextAlign.Center,
            fontSize = MaterialTheme.typography.title2.fontSize
        )
        GifRenderer(Modifier.fillMaxSize(0.6f), R.drawable.compass, R.drawable.compassplaceholder)
        Text(
            text = subMessage,
            modifier = Modifier.padding(horizontal = 10.dp),
            color = MaterialTheme.colors.primary,
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