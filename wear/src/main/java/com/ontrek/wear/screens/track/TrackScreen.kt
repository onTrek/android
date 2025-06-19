package com.ontrek.wear.screens.track

import android.util.Log
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.ontrek.wear.screens.track.components.Arrow
import com.ontrek.wear.screens.track.components.ProgressBar
import com.ontrek.wear.screens.track.components.SosButton
import com.ontrek.wear.utils.data.sensors.CompassSensor

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

    // Layout principale della schermata
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text,
            color = MaterialTheme.colors.primary,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(10.dp)
        )

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