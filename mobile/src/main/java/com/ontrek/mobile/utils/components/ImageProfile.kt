package com.ontrek.mobile.utils.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.ontrek.shared.api.profile.getImageProfile
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Composable
fun ImageProfile(
    userID: String,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
    size: Int = 50
) {
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(userID) {
        loading = true
        val base64 = getImageUrlForUser(userID)
        imageBitmap = base64?.let { convertBase64ToImageBitmap(it) }
        loading = false
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .border(
                width = 5.dp,
                color = color.copy(alpha = 0.2f),
                shape = CircleShape
            )
            .background(color.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        when {
            loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(size.dp * 0.6f),
                    strokeWidth = 2.dp
                )
            }

            imageBitmap != null -> {
                Image(
                    bitmap = imageBitmap!!,
                    contentDescription = "Immagine profilo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            else -> {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(size.dp * 0.6f)
                )
            }
        }
    }
}

fun convertBase64ToImageBitmap(base64String: String): ImageBitmap {
    val pureBase64 = if (base64String.contains(",")) {
        base64String.split(",")[1]
    } else {
        base64String
    }
    val decodedBytes = android.util.Base64.decode(pureBase64, android.util.Base64.DEFAULT)
    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    return bitmap.asImageBitmap()
}

suspend fun getImageUrlForUser(userID: String): String? {
    return suspendCancellableCoroutine { continuation ->
        getImageProfile(
            id = userID,
            onSuccess = { imageBytes ->
                val base64Image =
                    android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT)
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