package com.ontrek.wear.utils.media

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.tooling.preview.devices.WearDevices
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.request.ImageRequest
import com.ontrek.wear.R
import com.ontrek.wear.theme.OnTrekTheme

@Composable
fun GifRenderer(modifier: Modifier = Modifier, image: Any = R.drawable.compass, placeholder: Int = R.drawable.compassplaceholder) {
    val context = LocalContext.current

    val imageLoader = ImageLoader.Builder(context)
        .components {
            add(GifDecoder.Factory())
        }
        .build()

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(image)
            .placeholder(placeholder)
            .build(),
        contentDescription = "GIF",
        imageLoader = imageLoader,
        modifier = modifier
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun CalibrationPreview() {
    OnTrekTheme {
        GifRenderer(
            modifier = Modifier.fillMaxSize()
        )
    }
}