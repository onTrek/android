package com.ontrecksmartwatch.screens.track

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.wear.compose.material.Text
import com.ontrecksmartwatch.utils.data.sensors.CompassSensor

/**
 * Schermata che mostra una freccia che punta sempre verso Nord.
 * Utilizza i sensori di orientamento dello smartwatch per determinare la direzione.
 *
 * @param text Testo informativo da visualizzare nella schermata
 */
@Composable
fun TrackScreen(text: String) {
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

    // Layout principale della schermata
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        // Etichetta che indica il punto cardinale di riferimento
        Text("Nord")

        // Componente freccia che ruota in base alla direzione rilevata dai sensori
        Arrow(
            direction = direction,  // Angolo di rotazione basato sui dati del sensore
            modifier = Modifier.padding(16.dp)
        )

        // Visualizza l'angolo corrente in gradi rispetto al Nord
        Text("${direction.toInt()}°")

        // Visualizza il testo informativo passato come parametro
        Text(text)
    }
}