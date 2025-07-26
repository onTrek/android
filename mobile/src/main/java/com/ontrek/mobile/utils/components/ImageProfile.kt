package com.ontrek.mobile.utils.components

import androidx.compose.runtime.*
import coil.compose.AsyncImage
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ontrek.mobile.R

@Composable
fun ImageProfile(
    userID: String,
) {
    var imageUrl by remember { mutableStateOf<String?>(null) }

    // Simulazione di caricamento asincrono dell'URL dell'immagine
    LaunchedEffect(userID) {
        // Sostituisci questa logica con la tua funzione per ottenere l'URL dell'immagine
        imageUrl = getImageUrlForUser(userID)
    }

    if (imageUrl != null) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Immagine profilo utente",
            placeholder = painterResource(id = R.drawable.ic_launcher_background),
            error = painterResource(id = R.drawable.ic_launcher_background),
            modifier = Modifier.clip(androidx.compose.foundation.shape.CircleShape).size(50.dp)
        )
    } else {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = "Immagine profilo predefinita",
            modifier = Modifier.clip(androidx.compose.foundation.shape.CircleShape).size(50.dp)
        )
    }
}

// Funzione fittizia per ottenere l'URL dell'immagine dell'utente
suspend fun getImageUrlForUser(userID: String): String? {
    // Implementa qui la logica reale (API, database, ecc.)
    // Per ora ritorna null o un URL di esempio
    return null
}