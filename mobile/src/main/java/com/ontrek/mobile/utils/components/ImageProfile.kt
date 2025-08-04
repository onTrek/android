package com.ontrek.mobile.utils.components

import androidx.compose.runtime.*
import coil.compose.AsyncImage
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ontrek.shared.api.profile.getImageProfile
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Composable
fun ImageProfile(
    userID: String,
) {
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(userID) {
        loading = true
        imageUrl = getImageUrlForUser(userID)
        loading = false
    }

    if (imageUrl != null && !loading) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Image Profile",
            placeholder = null,
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier.clip(androidx.compose.foundation.shape.CircleShape).size(50.dp)
        )
    } else if (loading) {
        CircularProgressIndicator()
    } else {
        Icon(
            imageVector = Icons.Rounded.Person,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        )
    }
}

suspend fun getImageUrlForUser(userID: String): String? {
    return suspendCancellableCoroutine { continuation ->
        getImageProfile(
            id = userID,
            onSuccess = { imageBytes ->
                val base64Image = android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT)
                val imageUrl = "data:image/jpeg;base64,$base64Image"
                continuation.resume(imageUrl)
            },
            onError = { errorMessage ->
                println("Error fetching image: $errorMessage")
                continuation.resume(null)
            }
        )
    }
}